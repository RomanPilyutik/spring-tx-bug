package com.rollback.r2dbc.synchronizaions.dbconfig.converter

import java.nio.ByteBuffer
import java.util.UUID
import org.springframework.core.convert.converter.Converter

object ByteArrayToUuidConverter : Converter<ByteArray, UUID> {
    override fun convert(source: ByteArray): UUID = ByteBuffer.wrap(source).let { UUID(it.long, it.long) }
}
