package ru.tinkoff.asdk.preprocessor

import org.junit.Assert
import org.junit.Test

class QrDeepLinkModifierKtTest {

    @Test
    fun modifyQrResponse() {
        val expected = mapOf("Data" to "bank100000000111://qr.nspk.ru/4D178EFC5F484F8FA1F901FF64A3FAE7")
        val origin = mapOf("Data" to "https://qr.nspk.ru/4D178EFC5F484F8FA1F901FF64A3FAE7")
        val bank100000000111 = "bank100000000111"
        val new = origin.modifyGetQrResponse(bank100000000111)
        Assert.assertEquals(new, expected)
    }
}