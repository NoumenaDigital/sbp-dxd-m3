use casper_types::ApiError;

#[repr(u16)]
#[derive(Clone, Copy)]
pub enum DidCoreError {
    InvalidAccount = 1,
    UnexpectedKeyVariant = 2,
    FailedToGetArgBytes = 3,
    FailedToCreateDictionary = 4,
    MissingStorageUref = 5,
    InvalidStorageUref = 6,
    MissingCollectionName = 7,
    InvalidCollectionName = 8,
    MissingAccountHash = 9,
    InvalidAccountHash = 10,
    ContractAlreadyInitialized = 11,
    MissingDelegateType = 12,
    MissingDelegate = 13,
    MissingAttribute = 14,
    MissingIdentity = 15,
}

impl From<DidCoreError> for ApiError {
    fn from(e: DidCoreError) -> Self {
        ApiError::User(e as u16)
    }
}
