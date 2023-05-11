    # Decentralized Identifier (DID) implementation for Casper Blockchain

This contract is an implementation of Decentralized Identifier (DID) registry and implements a subset of ERC-1056.

## Table of Contents

1. [Background Information](#Background-Information)
   1. [Context](#Context)
   2. [DID Concept](#DID-Concept)
2. [Code Documentation](#Code-Documentation)
   1. [Implementation](#Implementation)
   2. [Local testing](#Local-Testing)
   3. [Manual testing on the testnet](#Manual-testing-on-the-testnet)

## Background Information

### Context

In the scope of this project, a prototype of a Smart Billing Platform that provides invoice verification, liquidity
to invoices' trading market and debtor rating is being implemented.
This platform utilizes off-chain smart contracts implemented in the Noumena Protocol
Language (NPL) to handle complex business logic and sensitive data. These off-chain smart-contracts are used to orchestrate
on-chain smart contract which control actions on the Casper blockchain itself e.g. add DID, change owner, etc.

Milestone 2 aims at implementing DID registry deployed to the Casper testnet. The DIDs stored on-chain represents
the ownership of the invoice (invoice) and will be later used to generate a verifiable claim for the invoice.

For a detailed description of the user journeys and the solution architecture overview, please refer to [Milestone 1](https://github.com/NoumenaDigital/sbp-dxd-m1/blob/master/Smart%20Billing%20Platform%20-%20Technical%20Architecture.pdf) of this grant. A prototype of the smart
billing platform will be delivered with the Milestone 3.

### DID Concept

The main idea behind the DID is decentralized identity. Owner of the identity should be able to maintain his own identity without centralized institution. 
More info can be found [here](https://www.w3.org/TR/did-core). Blockchain technology is suitable for the identity orchestration because the identity can be represented in form of the blockchain account. Any change of the identity must be signed by the account's private key.

The DID registry should allow owners to perform some actions:
- give ownership of their identity (account) to another account (owner can register another account to be the new owner which means new owner can maintain that identity and previous owner loses all rights)
- add/revoke delegates (owner can mark another account as a delegate for specific period of time)
- set/revoke attributes (owner can add/remove attribute to his identity)

On the other hand, the DID registry should allow anyone to resolve DID Document (structure that contains information about the identity) for any identity. With the DID Document resolved, anyone can check what is the state of the identity (who is the owner, does it have attributes or delegates, are delegates still valid etc.). 

## Code Documentation

### Implementation

The contract is implemented as a combination of the data storage and entry points used to manipulate that data. The contract installer doesn't have any special permissions. In this contract, each identity is represented by the account.

The data is stored in the three dictionaries
- owners (owners for identities - if identity is missing, it means the owner of the identity is the identity itself (default))
- delegates (delegates for identities)
- attributes (attributes for identities)

Here is the list of the access points (each entry point can be invoked only by the identity owner) used to maintain the registry data:
- change_owner(identity, new_owner)
- add_delegate(identity, delegate_type, delegate, validity)
- remove_delegate(identity, delegate_type, delegate)
- set_attribute(identity, attribute_key, attribute_value)
- revoke_attribute(identity, attribute_key)

All three dictionaries are public and anyone can query data from them.
#### Owners
The "owners" data storage is implemented as a simple key/value map where the key is the string that represents identity, and the value is the owner's account. If it is missing, it means the account is the owner of its own identity.

#### Delegates
The "delegates" is a map with the key as a string (identity) and value as a map (delegate_types_for_identity).  
The value (delegate_types_for_identity) is a map with the key as a string (delegate_type) and a value as a map (delegates_for_delegate_type).  
The value (delegates_for_delegate_type) is a map with the key as a string (delegate account) and a value as a number (validity).  
The value (validity) is the timestamp until this delegate is valid.  
It is a number that represents Unix time of milliseconds (e.g. Unix time of 1672412543881 represents the time Dec 30 2022 15:02:23).

#### Attributes
The "attributes" is a map with the key as a string (identity) and value as a map (attributes_for_identity).  
The value (attributes_for_identity) is the map with the key as a string (attribute_name) and the value as a string (attribute_value).  

#### Restrictions
The Casper doesn't have support for read entry points, so it is not possible to create read-only methods for checking the owner/delegates/attributes. Nevertheless, user can check these values in the dictionaries and perform calculations on his own (e.g. checking if delegate is still valid by comparing the current time with the validity).

### Local Testing
Commands for preparing and running local tests:

   ```bash
   # Set up the Rust toolchain
   make prepare
   # Compile the DID smart contract
   make build-did-registry
   # Run tests
   make test
   ```

Tested scenarios can be found [here](tests/src/did_contract_tests.rs)

### Manual testing on the testnet

At least 2 sets of public keys and their Accounts are needed to perform manual testing.  
So, steps 2,3 and 4 below need done 2 times. There should be 2 accounts:

| account    | account hash reference | private key reference                |
|------------|------------------------|--------------------------------------|
| identity_1 | <ACCOUNT_HASH_1>       | \<PATH_TO_ACCOUNT_SECRET_KEY_1\>.pem |
| identity_2 | <ACCOUNT_HASH_2>       | \<PATH_TO_ACCOUNT_SECRET_KEY_2\>.pem |

##### Account Setup
1. Install casper-client: https://docs.casperlabs.io/workflow/setup/#the-casper-command-line-client
2. Create keys: https://docs.casperlabs.io/dapp-dev-guide/keys/#option-1-key-generation-using-the-casper-client (if `tree ed25519-keys/` doesn't work use `ls ed25519-keys/`)
3. Get Casper Signer: https://chrome.google.com/webstore/detail/casper-signer/djhndpllfiibmcdbnmaaahkhchcoijce and use the keys from “2.” to create account.
4. Go to https://testnet.cspr.live and then to Tools/Faucet in order to get some CSPR.
   To verify the account is setup correctly, login to it on https://testnet.cspr.live

##### Deploy

1. Clone the repo and build the contract locally to produce the deployable wasm file
   ```bash
      git clone https://github.com/NoumenaDigital/sbp-dxd-m2.git
      make prepare
      make clean test
   ```
2. `deploy` DID contract
   ```bash
   casper-client put-deploy \
      --chain-name casper-test \
      --node-address http://136.243.187.84:7777 \
      --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_1>.pem \
      --payment-amount 100000000000 \
      --session-path ./target/wasm32-unknown-unknown/release/did_registry.wasm \
      --session-arg "did_name:string='did_registry_v1'"
   ```

NB:
All casper-client commands are expected to return immediately with a json String containing a deploy_hash.
e.g.

   ```json
      {
        "id": -7201873181009066000,
          "jsonrpc": "2.0",
          "result": {
            "api_version": "1.4.7",
            "deploy_hash": "8bba4460a145e4a9d8e815bd741de9ad4974db2df0cc9d685f455e3d81d38717"
          }
      }
   ```

The actual deploy takes a few minutes to complete and can be seen at https://testnet.cspr.live under "Deploys"
tab for the Account corresponding to the secret key used in the deploy command.
A test that's expected to fail will show a failed Deploy. To see the failure reason, click on the deploy hash of
the failed deploy.

Verification: The contract hash (<CONTRACT_HASH>) should be stored under the "Named Keys" tab of installer's account on https://testnet.cspr.live, under the Key 'did_registry_v1_contract_hash' (if the 'did_name' arg is 'did_registry_v1' as per example).
The value will start with a `hash-` and look like this: `hash-dbf8393bf6eb376ad750ac0dd9a96a8f4f3d5e43c57145714a50f380d5a7805e`.
That value should be used for every access point (for the field 'session-hash') together with the 'hash-' prefix.
Account arguments (of type 'key') should have an `account-hash-` prefix (eg `account-hash-50daaeb5fb53d5c879b4583012150398d5a24e83726c1e83459ab67cd6d610c0`).

##### Testing of the access points
Each method is used to make some changes in the data (dictionaries). Expected values can be verified by querying dictionaries:
- using the casper-client    
   ```bash
      # example command for querying dictionaries
      casper-client get-dictionary-item \
        --node-address http://136.243.187.84:7777 \
        --state-root-hash bcf1e13632f4c3928bba8effdf5fbf5308991b590b5328456238670d7fa96ed2 \
        --contract-hash hash-dbf8393bf6eb376ad750ac0dd9a96a8f4f3d5e43c57145714a50f380d5a7805e \
        --dictionary-name attributes \
        --dictionary-item-key 803422a5a4ca116df3c631bd7cb53ac8d1086a8bf879aa23b3f7fe1ac960ba6e
      # state-root-hash is dynamically changed so it should be resolved before every dictionary invocation. Here is the command:
      casper-client get-state-root-hash \
        --id 1 \
        --node-address http://136.243.187.84:7777
   ```

- or by checking at the https://testnet.cspr.live/<CONTRACT_HASH> (e.g. https://testnet.cspr.live/contract/0440f3264e927dc491ee86818e9b4220ce14ba6089eb245f01a0858bcd89b5c7)). Dictionary values can be checked in the "Named Keys" tab.

Here is the list of access points. Each access point can only be invoked by the identity owner (should be verified for every access point):
1. `change_owner` is used to change ownership of the account. 

   ```bash
      casper-client put-deploy \
        --node-address http://136.243.187.84:7777 \
        --chain-name casper-test \
        --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_1>.pem \
        --payment-amount 2000000000 \
        --session-hash "hash-<CONTRACT_HASH>" \
        --session-entry-point "change_owner" \
        --session-arg "identity:key='account-hash-<ACCOUNT_HASH_1>'" \
        --session-arg "new_owner:key='account-hash-<ACCOUNT_HASH_2>'"
   ```

   Verification: The "owners" dictionary should contain value <ACCOUNT_HASH_2> for the key <ACCOUNT_HASH_1>
   After this step, the owner of the account_1 is the account_2. So, the account_1 cannot anymore maintain identity_1 (unless account_2 revert ownership)

2. `add_delegate` is used to add delegate to the identity

   ```bash
      casper-client put-deploy \
        --node-address http://136.243.187.84:7777 \
        --chain-name casper-test \
        --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_2>.pem \
        --payment-amount 3000000000 \
        --session-hash "hash-<CONTRACT_HASH>" \
        --session-entry-point "add_delegate" \
        --session-arg "identity:key='account-hash-<ACCOUNT_HASH_2>'" \
        --session-arg "delegate_type:string='can_sign_documents'" \
        --session-arg "delegate:key='account-hash-<ACCOUNT_HASH_1>'" \
        --session-arg "validity:u64='10000000'"
   ```

   Verification: The "delegates" dictionary should contain expected value for the key <ACCOUNT_HASH_2>

3. `revoke_delegate` is used to add delegate to the identity

   ```bash
      casper-client put-deploy \
        --node-address http://136.243.187.84:7777 \
        --chain-name casper-test \
        --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_2>.pem \
        --payment-amount 3000000000 \
        --session-hash "hash-<CONTRACT_HASH>" \
        --session-entry-point "revoke_delegate" \
        --session-arg "identity:key='account-hash-<ACCOUNT_HASH_2>'" \
        --session-arg "delegate_type:string='can_sign_documents'" \
        --session-arg "delegate:key='account-hash-<ACCOUNT_HASH_1>'"
   ```
   Verification: The "delegates" dictionary should not contain <ACCOUNT_HASH_1> as a delegate for the key <ACCOUNT_HASH_2>

4. `set_attribute` is used to set the attribute to the identity

   ```bash
      casper-client put-deploy \
        --node-address http://136.243.187.84:7777 \
        --chain-name casper-test \
        --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_2>.pem \
        --payment-amount 3000000000 \
        --session-hash "hash-<CONTRACT_HASH>" \
        --session-entry-point "set_attribute" \
        --session-arg "identity:key='account-hash-<ACCOUNT_HASH_2>'" \
        --session-arg "attribute_name:string='name_of_the_owner'" \
        --session-arg "attribute_value:string='John'"
   ```
   Verification: The "attributes" dictionary should contain expected attribute for the key <ACCOUNT_HASH_2>

5. `revoke_attribute` is used to revoke the attribute for the identity

   ```bash
      casper-client put-deploy \
        --node-address http://136.243.187.84:7777 \
        --chain-name casper-test \
        --secret-key <PATH_TO_ACCOUNT_SECRET_KEY_2>.pem \
        --payment-amount 3000000000 \
        --session-hash "hash-<CONTRACT_HASH>" \
        --session-entry-point "revoke_attribute" \
        --session-arg "identity:key='account-hash-<ACCOUNT_HASH_2>'" \
        --session-arg "attribute_name:string='name_of_the_owner'"
   ```
Verification: The "attributes" dictionary should not contain 'name_of_the_owner' attribute for the key <ACCOUNT_HASH_2>

Additional testing:
- verify that non-owner account cannot invoke any method of the non-owned identity
