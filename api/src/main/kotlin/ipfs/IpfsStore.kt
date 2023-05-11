package ipfs

import com.fasterxml.jackson.annotation.JsonProperty
import config.ISbpdxdConfiguration
import mu.KotlinLogging
import org.http4k.client.ApacheClient
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.format.Jackson.auto
import org.http4k.lens.MultipartForm
import org.http4k.lens.MultipartFormFile
import org.http4k.lens.Validator
import org.http4k.lens.multipartForm
import java.net.URI
import java.net.URL
import java.util.Base64

private val logger = KotlinLogging.logger {}

class IpfsStoreException(message: String, reason: Throwable? = null) : RuntimeException(message, reason)

fun interface IpfsStore {
    /**
     * Store file to IPFS and return ipfs hash as a URI
     */
    fun store(file: MultipartFormFile): String
}

private data class IpfsResponse(
    @JsonProperty("Hash") val hash: String,
    @JsonProperty("Name") val name: String,
    @JsonProperty("Size") val size: String,
)

private val fileForm = MultipartFormFile.required("file")
private val strictIpfsFormBody = Body.multipartForm(Validator.Strict, fileForm, diskThreshold = 10000000).toLens()
private val messageLens = Body.auto<IpfsResponse>().toLens()

class DefaultIpfsStore(config: ISbpdxdConfiguration, private val client: HttpHandler = ApacheClient()) :
    IpfsStore {
    private val uri: URI = config.storeURI
    private val infuraIpfsProjectId: String = config.infuraIpfsProjectId
    private val infuraIpfsProjectSecret: String = config.infuraIpfsProjectSecret
    override fun store(file: MultipartFormFile): String {
        logger.info { "Received request for storing the file to the IPFS: fileName: ${file.filename} , contentType: ${file.contentType}" }
        val url = URL(uri.toURL(), "/api/v0/add").toExternalForm()
        val encoded: String =
            Base64.getEncoder().encodeToString("$infuraIpfsProjectId:$infuraIpfsProjectSecret".toByteArray())

        val multipartForm = MultipartForm().with(fileForm of file)
        val request = Request(Method.POST, url)
            .header("Authorization", "Basic $encoded")
            .with(strictIpfsFormBody of multipartForm)

        val res = client(request)
        return when (res.status) {
            Status.OK -> {
                val ipfsHash = messageLens.extract(res).hash
                logger.info { "Received ipfs hash from the IPFS provider: $ipfsHash" }
                ipfsHash
            }

            else -> {
                logger.error { "$url returned ${res.status} when executing\n$ post request" }
                throw IpfsStoreException(res.status.toString())
            }
        }
    }
}
