extern crate alloc;

use alloc::collections::BTreeMap;
use alloc::string::String;
use alloc::{string::ToString, vec, vec::Vec};

use casper_contract::contract_api::runtime::revert;
use casper_contract::{
    contract_api::{self, runtime, storage},
    ext_ffi,
    unwrap_or_revert::UnwrapOrRevert,
};
use casper_types::{
    api_error,
    bytesrepr::{self, FromBytes, ToBytes},
    ApiError, CLTyped, Key, URef,
};

use crate::error::DidCoreError;
use common::constants::{ATTRIBUTES, DELEGATES, OWNERS};

// used to make sure entry point can be invoked only by the identity owner
pub(crate) fn enforce_only_identity_owner_can_invoke(identity: Key) {
    let owner = get_identity_owner(identity);
    if owner != Key::Account(runtime::get_caller()) {
        revert(DidCoreError::InvalidAccount)
    }
}

pub(crate) fn change_owner_internal(identity: &str, new_owner: Key) {
    upsert_dictionary_value_from_key(OWNERS, identity, new_owner)
}

pub(crate) fn get_identity_owner(identity: Key) -> Key {
    match get_dictionary_value_from_key::<Key>(OWNERS, &key_to_str(&identity)) {
        Some(owner) => owner,
        None => identity,
    }
}

// used to store delegate data. Delegate has a type. Each identity can have multiple types. Each type can have multiple delegates.
pub(crate) fn add_delegate_internal(
    identity: String,
    delegate_type: String,
    delegate: String,
    validity: u64,
) {
    let value: u64 = u64::from(runtime::get_blocktime()) + validity;

    let mut types_for_identity = get_dictionary_value_from_key::<
        BTreeMap<String, BTreeMap<String, u64>>,
    >(DELEGATES, &identity)
    .unwrap_or_default();

    let mut delegates_for_type = BTreeMap::clone(
        types_for_identity
            .get(&delegate_type)
            .unwrap_or(&BTreeMap::new()),
    );

    delegates_for_type.insert(delegate, value);
    types_for_identity.insert(delegate_type, delegates_for_type);

    upsert_dictionary_value_from_key(DELEGATES, &identity, types_for_identity);
}

// used to revoke delegate, revert if delegate (for specific identity and delegate_type) doesn't exist
pub(crate) fn revoke_delegate_internal(identity: String, delegate_type: String, delegate: String) {
    let mut types_for_identity = get_dictionary_value_from_key::<
        BTreeMap<String, BTreeMap<String, u64>>,
    >(DELEGATES, &identity)
    .unwrap_or_revert_with(DidCoreError::MissingIdentity);

    let delegates_for_type_origin = types_for_identity
        .get(&delegate_type)
        .unwrap_or_revert_with(DidCoreError::MissingDelegateType);

    let mut delegates_for_type = BTreeMap::clone(delegates_for_type_origin);

    let exists = delegates_for_type.contains_key(&delegate);
    if exists {
        delegates_for_type.remove(&delegate);
        types_for_identity.insert(delegate_type, delegates_for_type);
        upsert_dictionary_value_from_key(DELEGATES, &identity, types_for_identity);
    } else {
        revert(DidCoreError::MissingDelegate)
    }
}

// used to store attributes. One identity can have multiple attributes. The attribute is represented as a key/value par
pub(crate) fn set_attribute_internal(identity: String, attribute_name: String, attribute_value: String) {
    let mut attributes_for_identity =
        get_dictionary_value_from_key::<BTreeMap<String, String>>(ATTRIBUTES, &identity)
            .unwrap_or_default();
    attributes_for_identity.insert(attribute_name, attribute_value);
    upsert_dictionary_value_from_key(ATTRIBUTES, &identity, attributes_for_identity);
}

// used to revoke attribute,  revert if attribute (for specific identity) doesn't exist
pub(crate) fn revoke_attribute_internal(identity: String, attribute_name: String) {
    let mut attributes_for_identity =
        get_dictionary_value_from_key::<BTreeMap<String, String>>(ATTRIBUTES, &identity)
            .unwrap_or_revert_with(DidCoreError::MissingIdentity);

    let exists = attributes_for_identity.contains_key(&attribute_name);
    if exists {
        attributes_for_identity.remove(&attribute_name);
        upsert_dictionary_value_from_key(ATTRIBUTES, &identity, attributes_for_identity);
    } else {
        revert(DidCoreError::MissingAttribute)
    }
}

pub(crate) fn upsert_dictionary_value_from_key<T: CLTyped + FromBytes + ToBytes>(
    dictionary_name: &str,
    key: &str,
    value: T,
) {
    let seed_uref = get_uref(
        dictionary_name,
        DidCoreError::MissingStorageUref,
        DidCoreError::InvalidStorageUref,
    );
    storage::dictionary_put(seed_uref, key, Some(value));
}

pub(crate) fn get_dictionary_value_from_key<T: CLTyped + FromBytes>(
    dictionary_name: &str,
    key: &str,
) -> Option<T> {
    let seed_uref = get_uref(
        dictionary_name,
        DidCoreError::MissingStorageUref,
        DidCoreError::InvalidStorageUref,
    );

    match storage::dictionary_get::<Option<T>>(seed_uref, key) {
        Ok(maybe_value) => match maybe_value {
            None => None,
            Some(option) => option,
        },
        Err(error) => revert(error),
    }
}

pub(crate) fn get_named_arg_size(name: &str) -> Option<usize> {
    let mut arg_size: usize = 0;
    let ret = unsafe {
        ext_ffi::casper_get_named_arg_size(
            name.as_bytes().as_ptr(),
            name.len(),
            &mut arg_size as *mut usize,
        )
    };
    match api_error::result_from(ret) {
        Ok(_) => Some(arg_size),
        Err(ApiError::MissingArgument) => None,
        Err(e) => revert(e),
    }
}

pub(crate) fn get_named_arg_with_user_errors<T: FromBytes>(
    name: &str,
    missing: DidCoreError,
    invalid: DidCoreError,
) -> Result<T, DidCoreError> {
    let arg_size = get_named_arg_size(name).ok_or(missing)?;
    let arg_bytes = if arg_size > 0 {
        let res = {
            let data_non_null_ptr = contract_api::alloc_bytes(arg_size);
            let ret = unsafe {
                ext_ffi::casper_get_named_arg(
                    name.as_bytes().as_ptr(),
                    name.len(),
                    data_non_null_ptr.as_ptr(),
                    arg_size,
                )
            };
            let data =
                unsafe { Vec::from_raw_parts(data_non_null_ptr.as_ptr(), arg_size, arg_size) };
            api_error::result_from(ret).map(|_| data)
        };
        // Assumed to be safe as `get_named_arg_size` checks the argument already
        res.unwrap_or_revert_with(DidCoreError::FailedToGetArgBytes)
    } else {
        // Avoids allocation with 0 bytes and a call to get_named_arg
        Vec::new()
    };

    bytesrepr::deserialize(arg_bytes).map_err(|_| invalid)
}

pub(crate) fn get_uref(name: &str, missing: DidCoreError, invalid: DidCoreError) -> URef {
    let key = get_key_with_user_errors(name, missing, invalid);
    key.into_uref()
        .unwrap_or_revert_with(DidCoreError::UnexpectedKeyVariant)
}

pub(crate) fn named_uref_exists(name: &str) -> bool {
    let (name_ptr, name_size, _bytes) = to_ptr(name);
    let mut key_bytes = vec![0u8; Key::max_serialized_length()];
    let mut total_bytes: usize = 0;
    let ret = unsafe {
        ext_ffi::casper_get_key(
            name_ptr,
            name_size,
            key_bytes.as_mut_ptr(),
            key_bytes.len(),
            &mut total_bytes as *mut usize,
        )
    };

    api_error::result_from(ret).is_ok()
}

pub(crate) fn get_key_with_user_errors(
    name: &str,
    missing: DidCoreError,
    invalid: DidCoreError,
) -> Key {
    let (name_ptr, name_size, _bytes) = to_ptr(name);
    let mut key_bytes = vec![0u8; Key::max_serialized_length()];
    let mut total_bytes: usize = 0;
    let ret = unsafe {
        ext_ffi::casper_get_key(
            name_ptr,
            name_size,
            key_bytes.as_mut_ptr(),
            key_bytes.len(),
            &mut total_bytes as *mut usize,
        )
    };
    match api_error::result_from(ret) {
        Ok(_) => {}
        Err(ApiError::MissingKey) => revert(missing),
        Err(e) => revert(e),
    }
    key_bytes.truncate(total_bytes);

    bytesrepr::deserialize(key_bytes).unwrap_or_revert_with(invalid)
}

pub(crate) fn to_ptr<T: ToBytes>(t: T) -> (*const u8, usize, Vec<u8>) {
    let bytes = t.into_bytes().unwrap_or_revert();
    let ptr = bytes.as_ptr();
    let size = bytes.len();
    (ptr, size, bytes)
}

pub fn key_to_str(key: &Key) -> String {
    match key {
        Key::Account(account) => account.to_string(),
        _ => revert(ApiError::UnexpectedKeyVariant),
    }
}
