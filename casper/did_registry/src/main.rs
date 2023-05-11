#![feature(unwrap_infallible)]
#![no_std]
#![no_main]

extern crate alloc;

use alloc::{
    format,
    string::String,
    vec,
};

use casper_contract::{
    contract_api::{
        runtime,
        storage::{self},
    },
    unwrap_or_revert::UnwrapOrRevert,
};
use casper_types::{
    CLType, EntryPoint, EntryPointAccess, EntryPoints, EntryPointType, Key, Parameter,
    runtime_args, RuntimeArgs
};

use common::constants::*;
use utils::*;
use error::DidCoreError;

#[cfg(not(target_arch = "wasm32"))]
compile_error!("target arch should be wasm32: compile with '--target wasm32-unknown-unknown'");

mod utils;
pub mod error;

#[no_mangle]
pub extern "C" fn change_owner() {
    let identity = get_named_arg_with_user_errors::<Key>(
        IDENTITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let new_owner = get_named_arg_with_user_errors::<Key>(
        NEW_OWNER,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    enforce_only_identity_owner_can_invoke(identity);

    change_owner_internal(&key_to_str(&identity), new_owner)
}

#[no_mangle]
pub extern "C" fn add_delegate() {
    let identity = get_named_arg_with_user_errors::<Key>(
        IDENTITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let delegate_type = get_named_arg_with_user_errors::<String>(
        DELEGATE_TYPE,
        DidCoreError::MissingDelegateType,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let delegate = get_named_arg_with_user_errors::<Key>(
        DELEGATE,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let validity = get_named_arg_with_user_errors::<u64>(
        VALIDITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    enforce_only_identity_owner_can_invoke(identity);
    add_delegate_internal(
        key_to_str(&identity),
        delegate_type,
        key_to_str(&delegate),
        validity,
    );
}

#[no_mangle]
pub extern "C" fn revoke_delegate() {
    let identity = get_named_arg_with_user_errors::<Key>(
        IDENTITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let delegate_type = get_named_arg_with_user_errors::<String>(
        DELEGATE_TYPE,
        DidCoreError::MissingDelegateType,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let delegate = get_named_arg_with_user_errors::<Key>(
        DELEGATE,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    enforce_only_identity_owner_can_invoke(identity);
    revoke_delegate_internal(
        key_to_str(&identity),
        delegate_type,
        key_to_str(&delegate),
    )
}

#[no_mangle]
pub extern "C" fn set_attribute() {
    let identity = get_named_arg_with_user_errors::<Key>(
        IDENTITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let attribute_name = get_named_arg_with_user_errors::<String>(
        ATTRIBUTE_NAME,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let attribute_value = get_named_arg_with_user_errors::<String>(
        ATTRIBUTE_VALUE,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    enforce_only_identity_owner_can_invoke(identity);
    set_attribute_internal(
        key_to_str(&identity),
        attribute_name,
        attribute_value,
    );
}

#[no_mangle]
pub extern "C" fn revoke_attribute() {
    let identity = get_named_arg_with_user_errors::<Key>(
        IDENTITY,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    let attribute_name = get_named_arg_with_user_errors::<String>(
        ATTRIBUTE_NAME,
        DidCoreError::MissingAccountHash,
        DidCoreError::InvalidAccountHash,
    )
    .unwrap_or_revert();

    enforce_only_identity_owner_can_invoke(identity);
    revoke_attribute_internal(key_to_str(&identity), attribute_name);
}

#[no_mangle]
pub extern "C" fn init() {
    // We only allow the init() entrypoint to be called once.
    // If DID_NAME uref already exists we revert since this implies that
    // the init() entrypoint has already been called.
    if named_uref_exists(DID_NAME) {
        runtime::revert(DidCoreError::ContractAlreadyInitialized);
    }

    // Start collecting the runtime arguments.
    let did_name: String = get_named_arg_with_user_errors(
        DID_NAME,
        DidCoreError::MissingCollectionName,
        DidCoreError::InvalidCollectionName,
    )
    .unwrap_or_revert();

    runtime::put_key(DID_NAME, storage::new_uref(did_name).into());

    // Create the data dictionaries to store essential values, topically.
    storage::new_dictionary(OWNERS).unwrap_or_revert_with(DidCoreError::FailedToCreateDictionary);
    storage::new_dictionary(ATTRIBUTES)
        .unwrap_or_revert_with(DidCoreError::FailedToCreateDictionary);
    storage::new_dictionary(DELEGATES)
        .unwrap_or_revert_with(DidCoreError::FailedToCreateDictionary);
}

#[no_mangle]
pub extern "C" fn call() {
    let did_name: String = get_named_arg_with_user_errors(
        DID_NAME,
        DidCoreError::MissingCollectionName,
        DidCoreError::InvalidCollectionName,
    )
    .unwrap_or_revert();

    let entry_points = build_entry_points();

    let (contract_hash, _contract_version) = storage::new_contract(
        entry_points,
        None,
        Some(format!("{}_package_hash", did_name)),
        Some(format!("{}_access_uref", did_name)),
    );

    runtime::put_key(&format!("{}_contract_hash", did_name), contract_hash.into());

    runtime::put_key(
        &format!("{}_contract_hash_wrapped", did_name),
        storage::new_uref(contract_hash).into(),
    );

    runtime::call_contract::<()>(
        contract_hash,
        ENTRY_POINT_INIT,
        runtime_args! {
             DID_NAME => did_name,
        },
    );
}

fn build_entry_points() -> EntryPoints {
    let mut entry_points = EntryPoints::new();

    // This entrypoint initializes the contract and is required to be called during the session
    // where the contract is installed; immediately after the contract has been installed but
    // before exiting session. All parameters are required.
    // This entrypoint is intended to be called exactly once and will error if called more than
    // once.
    let init_contract = EntryPoint::new(
        ENTRY_POINT_INIT,
        vec![Parameter::new(DID_NAME, CLType::String)],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    let change_owner = EntryPoint::new(
        ENTRY_POINT_CHANGE_OWNER,
        vec![
            Parameter::new(IDENTITY, CLType::Key),
            Parameter::new(NEW_OWNER, CLType::Key),
        ],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    let add_delegate = EntryPoint::new(
        ENTRY_POINT_ADD_DELEGATE,
        vec![
            Parameter::new(IDENTITY, CLType::Key),
            Parameter::new(DELEGATE_TYPE, CLType::ByteArray(32)),
            Parameter::new(DELEGATE, CLType::Key),
            Parameter::new(VALIDITY, CLType::U64),
        ],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    let revoke_delegate = EntryPoint::new(
        ENTRY_POINT_REVOKE_DELEGATE,
        vec![
            Parameter::new(IDENTITY, CLType::Key),
            Parameter::new(DELEGATE_TYPE, CLType::ByteArray(32)),
            Parameter::new(DELEGATE, CLType::Key),
        ],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    let set_attribute = EntryPoint::new(
        ENTRY_POINT_SET_ATTRIBUTE,
        vec![
            Parameter::new(IDENTITY, CLType::Key),
            Parameter::new(ATTRIBUTE_NAME, CLType::ByteArray(32)),
            Parameter::new(ATTRIBUTE_VALUE, CLType::ByteArray(32)),
        ],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    let revoke_attribute = EntryPoint::new(
        ENTRY_POINT_REVOKE_ATTRIBUTE,
        vec![
            Parameter::new(IDENTITY, CLType::Key),
            Parameter::new(ATTRIBUTE_NAME, CLType::ByteArray(32)),
        ],
        CLType::Unit,
        EntryPointAccess::Public,
        EntryPointType::Contract,
    );

    entry_points.add_entry_point(init_contract);
    entry_points.add_entry_point(change_owner);
    entry_points.add_entry_point(add_delegate);
    entry_points.add_entry_point(revoke_delegate);
    entry_points.add_entry_point(set_attribute);
    entry_points.add_entry_point(revoke_attribute);
    entry_points
}
