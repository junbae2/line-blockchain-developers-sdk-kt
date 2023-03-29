/*
 * Copyright 2023 LINE Corporation
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
@file:Suppress("UNCHECKED_CAST")

package com.linecorp.link.developers.txresult.v1.raw.adapter

import com.linecorp.link.developers.txresult.adapter.TxResultAdapter
import com.linecorp.link.developers.txresult.core.event.item.EventCollectionFtMinted
import com.linecorp.link.developers.txresult.core.model.TransactionEvent
import com.linecorp.link.developers.txresult.core.model.TxMessage
import com.linecorp.link.developers.txresult.core.model.TxResultSummary
import com.linecorp.link.developers.txresult.util.RawTransactionLoader
import com.linecorp.link.developers.txresult.v1.raw.model.RawTransactionResult
import io.mockk.spyk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DomainTxResultAdapterV1Test {
    private lateinit var txResultSummaryAdapter: TxResultAdapter<RawTransactionResult, TxResultSummary>
    private lateinit var txMessageAdapter: TxResultAdapter<RawTransactionResult, Set<TxMessage>>
    private lateinit var txEventsAdapter: TxResultAdapter<RawTransactionResult, List<TransactionEvent>>
    private lateinit var underTest: DomainTxResultAdapterV1
    private lateinit var jsonRawTransactionResultAdapter: JsonRawTransactionResultAdapter

    @BeforeEach
    fun setUp() {
        txResultSummaryAdapter = spyk(DomainTxSummaryAdapterV1("tlink"))
        txMessageAdapter = spyk(DomainTxMessageAdapterV1())
        txEventsAdapter = spyk(DomainTxEventsAdapterV1())
        underTest = DomainTxResultAdapterV1(
            txResultSummaryAdapter = txResultSummaryAdapter,
            txMessageAdapter = txMessageAdapter,
            txEventsAdapter = txEventsAdapter,
        )
        jsonRawTransactionResultAdapter = JsonRawTransactionResultAdapter()
    }

    @Test
    fun test() {
        val rawTransactionInJsonText =
            RawTransactionLoader.loadRawTransactionResultInJsonText("raw-transaction/raw_transaction1.json")

        val rawTransactionResult = jsonRawTransactionResultAdapter.adapt(rawTransactionInJsonText)

        assertNotNull(rawTransactionResult)

        val txResult = underTest.adapt(rawTransactionResult)

        assertEquals(235816, txResult.summary.height)
        assertEquals(
            "61AB8A054D47CA05E4ABE591B929282CBCD7DACD5A4C8259020C566F0EC186BE",
            txResult.summary.txHash
        )
        assertEquals(0, txResult.summary.txIndex)
        assertEquals("", txResult.summary.result.codeSpace)
        assertEquals(0, txResult.summary.result.code)

        assertEquals(1, txResult.messages.size)
        assertNotNull(txResult.messages.find { it.requestType == "collection/MsgMintFT" })
        val txResultMessage = txResult.messages.find { it.requestType == "collection/MsgMintFT" }
        assertEquals(0, txResultMessage?.msgIndex)

        val mintFtEvent = txResult.events.firstOrNull { it::class == EventCollectionFtMinted::class }
        assertNotNull(mintFtEvent)
        assertEquals((mintFtEvent as EventCollectionFtMinted).contractId, "61e14383")
    }
}
