use alloc::string::String;
use std::collections::BTreeMap;

use casper_types::account::AccountHash;
use casper_types::RuntimeArgs;
use casper_types::{runtime_args, Key};
use casperlabs_test_env::{TestContract, TestEnv};
use common::constants::*;

pub struct DIDInstance(TestContract);

impl DIDInstance {
    pub fn new(
        env: &TestEnv,
        contract_name: &str,
        sender: AccountHash,
        did_name: &str,
    ) -> DIDInstance {
        DIDInstance(TestContract::new(
            env,
            "did_registry.wasm",
            contract_name,
            sender,
            runtime_args! {
                "did_name" => did_name,
            },
            0,
        ))
    }

    pub fn change_owner<T: Into<Key>>(&self, sender: AccountHash, identity: T, new_owner: T) {
        self.0.call_contract(
            sender,
            ENTRY_POINT_CHANGE_OWNER,
            runtime_args! {
                IDENTITY => identity.into(),
                NEW_OWNER => new_owner.into(),
            },
            0,
        )
    }

    pub fn add_delegate<T: Into<Key>>(
        &self,
        sender: AccountHash,
        identity: T,
        delegate_type: &str,
        delegate: T,
        validity: u64,
    ) {
        self.0.call_contract(
            sender,
            ENTRY_POINT_ADD_DELEGATE,
            runtime_args! {
                IDENTITY => identity.into(),
                DELEGATE_TYPE => delegate_type,
                DELEGATE => delegate.into(),
               VALIDITY => validity,
            },
            0,
        )
    }

    pub fn revoke_delegate<T: Into<Key>>(
        &self,
        sender: AccountHash,
        identity: T,
        delegate_type: &str,
        delegate: T,
    ) {
        self.0.call_contract(
            sender,
            ENTRY_POINT_REVOKE_DELEGATE,
            runtime_args! {
                IDENTITY => identity.into(),
                DELEGATE_TYPE => delegate_type,
                DELEGATE => delegate.into(),
            },
            0,
        )
    }

    pub fn set_attribute<T: Into<Key>>(
        &self,
        sender: AccountHash,
        identity: T,
        attribute_name: &str,
        attribute_value: &str,
    ) {
        self.0.call_contract(
            sender,
            ENTRY_POINT_SET_ATTRIBUTE,
            runtime_args! {
                IDENTITY => identity.into(),
                ATTRIBUTE_NAME => attribute_name,
                ATTRIBUTE_VALUE => attribute_value,
            },
            0,
        )
    }

    pub fn revoke_attribute<T: Into<Key>>(
        &self,
        sender: AccountHash,
        identity: T,
        attribute_name: &str,
    ) {
        self.0.call_contract(
            sender,
            ENTRY_POINT_REVOKE_ATTRIBUTE,
            runtime_args! {
                IDENTITY => identity.into(),
                ATTRIBUTE_NAME => attribute_name,
            },
            0,
        )
    }

    pub fn get_owner(&self, identity: Key) -> Option<Key> {
        self.0.query_dictionary(OWNERS, key_to_str(&identity))
    }

    pub fn get_delegate(
        &self,
        identity: String,
        delegate_type: String,
        delegate: String,
    ) -> Option<u64> {
        let types_for_identity_option: Option<BTreeMap<String, BTreeMap<String, u64>>> =
            self.0.query_dictionary(DELEGATES, identity);
        return match types_for_identity_option {
            None => None,
            Some(types_for_identity) => {
                let delegates_for_type_option = types_for_identity.get(&delegate_type);
                match delegates_for_type_option {
                    None => None,
                    Some(delegates_for_type) => {
                        let value_option = delegates_for_type.get(&delegate);
                        value_option.copied()
                    }
                }
            }
        };
    }

    pub fn get_attribute(&self, identity: String, attribute_name: String) -> Option<String> {
        let attributes_for_identity_option: Option<BTreeMap<String, String>> =
            self.0.query_dictionary(ATTRIBUTES, identity);
        return match attributes_for_identity_option {
            None => None,
            Some(attributes_for_identity) => {
                let attribute = attributes_for_identity.get(&attribute_name);
                attribute.cloned()
            }
        };
    }

    pub fn name(&self) -> String {
        self.0.query_named_key(String::from(DID_NAME))
    }
}

pub fn key_to_str(key: &Key) -> String {
    match key {
        Key::Account(account) => account.to_string(),
        _ => panic!("Unexpected key type"),
    }
}
