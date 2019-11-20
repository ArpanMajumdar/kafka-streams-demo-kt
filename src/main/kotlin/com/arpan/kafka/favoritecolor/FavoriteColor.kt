package com.arpan.kafka.favoritecolor

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.KeyValue
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

class FavoriteColor(
    private val inputTopic: String,
    private val outputTopic: String,
    private val intermediateTopic: String,
    private val props: Map<String, Any>
) {
    private val logger = LoggerFactory.getLogger(FavoriteColor::class.java)
    private val properties = Properties().apply {
        props.forEach { (key, value) -> put(key, value) }
    }
    private val streamsBuilder = StreamsBuilder()
    private val source = streamsBuilder.stream<String, String>(inputTopic)
    private val intermediate = streamsBuilder.table<String, String>(intermediateTopic)
    private val colorsByUserId = source
        .filter { _, value -> """\w+,\w+""".toRegex().containsMatchIn(value) }
        .mapValues { _, value -> value.toLowerCase().split(",") }
        .selectKey { _, value -> value[0] }
        .mapValues { _, value -> value[1] }
        .filter { _, color -> color in setOf("red", "green", "blue") }
        .peek { user, color -> logger.info("$user -> $color") }
    private val colorsGrouped = intermediate
        .groupBy { _, color -> KeyValue(color, color) }
        .count()

    fun start() {
        colorsByUserId.to(intermediateTopic, Produced.with(Serdes.String(), Serdes.String()))
        colorsGrouped.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()))

        val kafkaStreams = KafkaStreams(streamsBuilder.build(), properties)

        // Shutdown hook
        val countDownLatch = CountDownLatch(1)
        Runtime.getRuntime().addShutdownHook(Thread
        {
            kafkaStreams.close()
            countDownLatch.countDown()
        })

        // Start kafka streams
        try {
            kafkaStreams.start()
            countDownLatch.await()
        } catch (ex: Exception) {
            println("Error occurred")
            println(ex.printStackTrace())
            exitProcess(-1)
        }
    }
}
