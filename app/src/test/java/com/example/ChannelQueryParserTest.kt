package com.example

import com.example.domain.model.ChannelQueryParser
import com.example.domain.model.ChannelQueryType
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests unitaires pour l'analyseur de requêtes de chaînes YouTube.
 */
class ChannelQueryParserTest {

    @Test
    fun parseChannelIdDirect() {
        val result = ChannelQueryParser.parse("UCX6OQ3DkcsbYNE6H8uQQuVA")
        assertEquals(ChannelQueryType.CHANNEL_ID, result.type)
        assertEquals("UCX6OQ3DkcsbYNE6H8uQQuVA", result.value)
    }

    @Test
    fun parseChannelUrl() {
        val result = ChannelQueryParser.parse("https://www.youtube.com/channel/UCX6OQ3DkcsbYNE6H8uQQuVA")
        assertEquals(ChannelQueryType.CHANNEL_ID, result.type)
        assertEquals("UCX6OQ3DkcsbYNE6H8uQQuVA", result.value)
    }

    @Test
    fun parseHandleDirect() {
        val result = ChannelQueryParser.parse("@MrBeast")
        assertEquals(ChannelQueryType.HANDLE, result.type)
        assertEquals("MrBeast", result.value)
    }

    @Test
    fun parseHandleUrl() {
        val result = ChannelQueryParser.parse("https://youtube.com/@mkbhd")
        assertEquals(ChannelQueryType.HANDLE, result.type)
        assertEquals("mkbhd", result.value)
    }
}
