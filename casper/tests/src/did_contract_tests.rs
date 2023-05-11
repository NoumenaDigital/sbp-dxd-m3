use alloc::string::ToString;

use casper_types::account::AccountHash;
use casper_types::Key;
use casperlabs_test_env::TestEnv;

use crate::did_instance::{key_to_str, DIDInstance};

const DID_CONTRACT_NAME: &str = "Test_DID";

fn deploy() -> (TestEnv, DIDInstance, AccountHash) {
    let env = TestEnv::new();
    let deployer_account = env.next_user();
    let contract = DIDInstance::new(&env, DID_CONTRACT_NAME, deployer_account, DID_CONTRACT_NAME);
    (env, contract, deployer_account)
}

#[test]
fn test_deploy() {
    let (_, contract, _) = deploy();
    assert_eq!(contract.name(), DID_CONTRACT_NAME);
}

#[test]
fn test_identity_get_owner_identity_not_changed() {
    let (env, contract, _) = deploy();
    let identity = env.next_user();
    assert_eq!(None, contract.get_owner(Key::Account(identity)));
}

#[test]
fn test_change_owner_by_owner_successful() {
    let (env, contract, deployer_account) = deploy();
    let new_owner_account_hash = env.next_user();
    let new_owner = Key::Account(new_owner_account_hash);
    let identity = Key::Account(deployer_account);

    contract.change_owner(deployer_account, identity, new_owner);
    assert_eq!(contract.get_owner(identity).unwrap(), new_owner);
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_change_owner_and_try_to_change_by_previous_owner_rejected() {
    let (env, contract, deployer_account) = deploy();
    let new_owner_account_hash = env.next_user();
    let new_owner = Key::Account(new_owner_account_hash);
    let identity = Key::Account(deployer_account);

    contract.change_owner(deployer_account, identity, new_owner);
    contract.change_owner(deployer_account, identity, identity);
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_change_owner_by_non_owner_rejected() {
    let (env, contract, identity_owner_account) = deploy();
    let other_account = env.next_user();
    let new_owner = Key::Account(other_account);
    let identity = Key::Account(identity_owner_account);

    contract.change_owner(other_account, identity, new_owner);
}

#[test]
fn test_add_delegate_by_owner_successful() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();
    let validity = 10000;

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );
    let delegate_option =
        contract.get_delegate(key_to_str(&identity), delegate_type, key_to_str(&delegate));
    assert!(delegate_option.is_some());
}

#[test]
fn test_add_delegate_for_existing_identity_successful(){
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();
    let validity = 10000;
    let new_delegate = Key::Account(env.next_user());
    let new_delegate_type = "New_type".to_string();

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );

    contract.add_delegate(
        deployer_account,
        identity,
        &new_delegate_type,
        new_delegate,
        validity,
    );
    let delegate_option =
        contract.get_delegate(key_to_str(&identity), new_delegate_type, key_to_str(&new_delegate));
    assert!(delegate_option.is_some());
}

#[test]
fn test_add_delegate_for_existing_delegate_type_successful(){
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();
    let validity = 10000;
    let new_delegate = Key::Account(env.next_user());

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        new_delegate,
        validity,
    );
    let delegate_option =
        contract.get_delegate(key_to_str(&identity), delegate_type, key_to_str(&new_delegate));
    assert!(delegate_option.is_some());
}

#[test]
fn test_add_delegate_update_existing_delegate_successful(){
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();
    let validity = 10000;
    let new_validity = 20000;

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        new_validity,
    );
    let delegate_option =
        contract.get_delegate(key_to_str(&identity), delegate_type, key_to_str(&delegate));
    assert!(delegate_option.is_some());
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_add_delegate_by_non_owner_rejected() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let non_owner = env.next_user();
    let delegate_type = "Test_type".to_string();
    let validity = 10000;

    contract.add_delegate(non_owner, identity, &delegate_type, delegate, validity);
}

#[test]
fn test_revoke_delegate_by_owner_successful() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Missing_type".to_string();
    let validity = 10000;

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );

    contract.revoke_delegate(deployer_account, identity, &delegate_type, delegate);
}

#[test]
#[should_panic(expected = "ApiError::User(15) [65551]")]
fn test_revoke_delegate_by_owner_missing_identity() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();

    contract.revoke_delegate(deployer_account, identity, &delegate_type, delegate);
}

#[test]
#[should_panic(expected = "ApiError::User(12) [65548]")]
fn test_revoke_delegate_by_owner_missing_delegate_type() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let missing_type = "Missing_type".to_string();
    let delegate_type = "Test_type".to_string();
    let validity = 10000;

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );
    contract.revoke_delegate(deployer_account, identity, &missing_type, delegate);
}

#[test]
#[should_panic(expected = "ApiError::User(13) [65549]")]
fn test_revoke_delegate_by_owner_missing_delegate() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let missing_delegate = Key::Account(env.next_user());
    let delegate_type = "Test_type".to_string();
    let validity = 10000;

    contract.add_delegate(
        deployer_account,
        identity,
        &delegate_type,
        delegate,
        validity,
    );
    contract.revoke_delegate(deployer_account, identity, &delegate_type, missing_delegate);
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_revoke_delegate_by_non_owner_rejected() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let delegate = Key::Account(env.next_user());
    let non_owner = env.next_user();
    let delegate_type = "Test_type".to_string();

    contract.revoke_delegate(non_owner, identity, &delegate_type, delegate);
}

#[test]
fn test_set_attribute_by_owner_successful() {
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let attribute_value = "Attribute_value".to_string();

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &attribute_value,
    );
    let attribute_option = contract.get_attribute(key_to_str(&identity), attribute_name);
    assert!(attribute_option.is_some());
    let attribute: String = attribute_option.unwrap();
    assert_eq!(attribute_value, attribute);
}

#[test]
fn test_set_attribute_for_existing_identity_successful(){
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let attribute_value = "Attribute_value".to_string();
    let new_attribute_name = "New_attribute_name".to_string();
    let new_attribute_value = "New_attribute_value".to_string();

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &attribute_value,
    );

    contract.set_attribute(
        deployer_account,
        identity,
        &new_attribute_name,
        &new_attribute_value,
    );
    let attribute_option = contract.get_attribute(key_to_str(&identity), attribute_name);
    assert!(attribute_option.is_some());
    let attribute: String = attribute_option.unwrap();
    assert_eq!(attribute_value, attribute);

    let new_attribute_option = contract.get_attribute(key_to_str(&identity), new_attribute_name);
    assert!(new_attribute_option.is_some());
    let new_attribute: String = new_attribute_option.unwrap();
    assert_eq!(new_attribute_value, new_attribute);
}

#[test]
fn test_set_attribute_update_existing_attribute_successful(){
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let attribute_value = "Attribute_value".to_string();
    let new_attribute_value = "New_attribute_value".to_string();

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &attribute_value,
    );

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &new_attribute_value,
    );
    let attribute_option = contract.get_attribute(key_to_str(&identity), attribute_name);
    assert!(attribute_option.is_some());
    let attribute: String = attribute_option.unwrap();
    assert_eq!(new_attribute_value, attribute);
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_set_attribute_by_non_owner_rejected() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let attribute_value = "Attribute_value".to_string();
    let non_owner = env.next_user();

    contract.set_attribute(
        non_owner,
        identity,
        &attribute_name,
        &attribute_value,
    );
}

#[test]
fn test_revoke_attribute_by_owner_successful() {
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let attribute_value = "Attribute_value".to_string();

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &attribute_value,
    );
    contract.revoke_attribute(deployer_account, identity, &attribute_name);
    let attribute_option = contract.get_attribute(key_to_str(&identity), attribute_name);
    assert!(attribute_option.is_none());
}

#[test]
#[should_panic(expected = "ApiError::User(1) [65537]")]
fn test_revoke_attribute_by_non_owner_rejected() {
    let (env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let non_owner = env.next_user();

    contract.revoke_attribute(non_owner, identity, &attribute_name);
}

#[test]
#[should_panic(expected = "ApiError::User(15) [65551]")]
fn test_revoke_attribute_by_owner_missing_identity() {
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();

    contract.revoke_attribute(deployer_account, identity, &attribute_name);
}

#[test]
#[should_panic(expected = "ApiError::User(14) [65550]")]
fn test_revoke_attribute_by_owner_missing_attribute() {
    let (_env, contract, deployer_account) = deploy();
    let identity = Key::Account(deployer_account);
    let attribute_name = "Attribute_name".to_string();
    let wrong_attribute = "Wrong_name".to_string();
    let attribute_value = "Attribute_value".to_string();

    contract.set_attribute(
        deployer_account,
        identity,
        &attribute_name,
        &attribute_value,
    );
    contract.revoke_attribute(deployer_account, identity, &wrong_attribute);
}
