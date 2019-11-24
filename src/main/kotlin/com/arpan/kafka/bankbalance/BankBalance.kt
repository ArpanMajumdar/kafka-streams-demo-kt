package com.arpan.kafka.bankbalance

import com.arpan.kafka.KafkaStreamsUtil
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Materialized
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import java.util.*

class BankBalance(
    private val inputTopic: String,
    private val outputTopic: String,
    private val props: Map<String, Any>
) {
    private val logger = LoggerFactory.getLogger(BankBalance::class.java)
    private val properties = Properties().apply {
        props.forEach { (key, value) -> put(key, value) }
    }
    private val objectMapper = jacksonObjectMapper()
    private val streamsBuilder = StreamsBuilder()
    private val transactions = streamsBuilder.stream<String, String>(inputTopic)
    private val bankBalance = transactions
        .peek { _, transaction -> logger.info(transaction) }
        .mapValues { _, transaction ->
            objectMapper.readValue(transaction, Transaction::class.java)
        }
        .groupByKey()
        .aggregate(
            { 0L },
            { _, transaction, balance -> balance + transaction.amount },
            Materialized.with(Serdes.String(), Serdes.Long())
        )

    fun start() {
        bankBalance.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()))
        KafkaStreamsUtil(streamsBuilder, properties).start()
    }
}
