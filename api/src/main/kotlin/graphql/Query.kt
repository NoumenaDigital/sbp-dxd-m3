package graphql

fun getSupplierByName(name: String) =
    """
        {
          protocolFieldsTexts(filter: {field: {equalTo: "name"}, value: {equalTo: "$name"}}) {
            totalCount
            nodes {
              protocol {
                protocolId,
                protoRefId
              }
            }
          }
        }
    """
