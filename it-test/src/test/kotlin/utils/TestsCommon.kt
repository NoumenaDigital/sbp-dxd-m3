package utils

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.openapitools.client.models.ErrorResponse

val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
val errorResponseAdapter: JsonAdapter<ErrorResponse> = moshi.adapter(ErrorResponse::class.java)
