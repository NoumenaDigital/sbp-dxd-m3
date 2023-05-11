package graphql

data class Protocol(val protocolId: String, val protoRefId: String)
data class NodeProtocol(val protocol: Protocol)
data class ProtocolFieldsTexts(val totalCount: Int, val nodes: List<NodeProtocol>)
data class DataSupplier(val protocolFieldsTexts: ProtocolFieldsTexts)
data class GraphQlSupplierResponse(val data: DataSupplier)
