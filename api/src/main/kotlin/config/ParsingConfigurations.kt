package config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS
import com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_INTEGER_FOR_INTS
import com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.http4k.format.ConfigurableJackson
import org.http4k.format.asConfigurable
import org.http4k.format.withStandardMappings
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder

private class ISO8601Formatter :
    InstantSerializer(INSTANCE, false, DateTimeFormatterBuilder().appendInstant(0).toFormatter())

private class ZonedDateTimeSerializer : StdSerializer<ZonedDateTime>(ZonedDateTime::class.java) {
    override fun serialize(value: ZonedDateTime, gen: JsonGenerator, provider: SerializerProvider) {
        gen.writeString(value.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssxxx")))
    }
}

object JSON : ConfigurableJackson(
    KotlinModule()
        .asConfigurable()
        .done()
        .registerModule(
            JavaTimeModule()
                .addSerializer(Instant::class.java, ISO8601Formatter())
                .addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer())
        )
)

object XML : ConfigurableJackson(
    XmlMapper.builder()
        .defaultUseWrapper(false)
        .enable(SerializationFeature.WRITE_DATES_WITH_ZONE_ID)
        .build()
        .registerModule(
            JavaTimeModule()
                .addSerializer(Instant::class.java, ISO8601Formatter())
        )
)

object SnakeCaseJsonConfiguration : ConfigurableJackson(
    KotlinModule()
        .asConfigurable()
        .withStandardMappings()
        .done()
        .deactivateDefaultTyping()
        .configure(FAIL_ON_UNKNOWN_PROPERTIES, true)
        .configure(FAIL_ON_IGNORED_PROPERTIES, true)
        .configure(USE_BIG_DECIMAL_FOR_FLOATS, true)
        .configure(USE_BIG_INTEGER_FOR_INTS, true)
        .setPropertyNamingStrategy(SNAKE_CASE)
)
