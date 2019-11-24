package com.arpan.kafka

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.StreamsBuilder
import java.util.*
import java.util.concurrent.CountDownLatch
import kotlin.system.exitProcess

class KafkaStreamsUtil(private val streamsBuilder: StreamsBuilder, private val properties: Properties) {
    fun start() {
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