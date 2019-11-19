package com.arpan.kafka.wordcount

import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Produced
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

class WordCount(private val inputTopic: String, private val outputTopic: String, private val props: Map<String, Any>) {

    private val properties = Properties().apply {
        props.forEach { (key, value) -> put(key, value) }
    }
    private val streamsBuilder = StreamsBuilder()
    private val source = streamsBuilder.stream<String, String>(inputTopic)
    private val wordCounts = source
        .flatMapValues { value -> value.toLowerCase().split("\\W+") }
        .groupBy { _, value -> value }
        .count()


    fun start() {
        wordCounts.toStream().to(outputTopic, Produced.with(Serdes.String(), Serdes.Long()))

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