package com.arpan.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.concurrent.CountDownLatch

fun main() {

    val inputTopic = "word-count-input"
    val outputTopic = "word-count-output"

    val properties = Properties().apply {
        put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-app")
        put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
        put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String()::class.java)
    }


    val streamsBuilder = StreamsBuilder()
    val source = streamsBuilder.stream<String, String>(inputTopic)

    val wordCounts = source
        .flatMapValues { value -> value.toLowerCase().split("\\W+") }
        .groupBy { _, value -> value }
        .count()

    wordCounts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()))


    val kafkaStreams = KafkaStreams(streamsBuilder.build(), properties)

    // Shutdown hook
    val countDownLatch = CountDownLatch(1)
    Runtime.getRuntime().addShutdownHook(Thread {
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
        System.exit(-1)
    }
}