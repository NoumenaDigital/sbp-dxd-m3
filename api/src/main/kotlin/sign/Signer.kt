package sign

fun interface Signer {
    fun sign(msg: String): String
}
