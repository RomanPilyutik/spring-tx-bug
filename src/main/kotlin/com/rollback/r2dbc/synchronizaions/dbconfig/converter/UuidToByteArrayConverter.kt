package com.rollback.r2dbc.synchronizaions.dbconfig.converter

import java.nio.ByteBuffer
import java.util.UUID
import org.springframework.core.convert.converter.Converter

object UuidToByteArrayConverter : Converter<UUID, ByteArray> {
    private const val UUID_SIZE = 16

    override fun convert(source: UUID): ByteArray =
        ByteBuffer
            .wrap(ByteArray(UUID_SIZE))
            .let {
                it.putLong(source.mostSignificantBits)
                it.putLong(source.leastSignificantBits)
                it.array()
            }
}
