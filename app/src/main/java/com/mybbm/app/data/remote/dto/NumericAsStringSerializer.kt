package com.mybbm.app.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive

/**
 * Serializer kotlinx.serialization khusus untuk mempertahankan representasi string
 * dari nilai NUMERIC PostgreSQL tanpa kehilangan presisi desimal akibat konversi
 * ke tipe Floating Point (Double / Float).
 *
 * Mendekode baik JSON number (misal 10000.00) maupun JSON string (misal "10000.00")
 * langsung menjadi [String].
 */
object NumericAsStringSerializer : KSerializer<String> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("NumericAsString", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): String {
        return if (decoder is JsonDecoder) {
            val jsonElement = decoder.decodeJsonElement()
            jsonElement.jsonPrimitive.content
        } else {
            decoder.decodeString()
        }
    }

    override fun serialize(encoder: Encoder, value: String) {
        encoder.encodeString(value)
    }
}
