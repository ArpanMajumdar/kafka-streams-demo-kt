package com.arpan.kafka

import com.arpan.kafka.favoritecolor.FavoriteColor
import com.arpan.kafka.wordcount.WordCount
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsConfig

fun main() {
    runFavoriteColorApp()
}

fun runWordCountApp() {
    val inputTopic = "word-count-input"
    val outputTopic = "word-count-output"
    val properties = mapOf(
        StreamsConfig.APPLICATION_ID_CONFIG to "wordcount-app",
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String()::class.java
    )

    val wordCount = WordCount(inputTopic, outputTopic, properties)
    wordCount.start()
}

fun runFavoriteColorApp() {
    val inputTopic = "favorite-color-input"
    val outPutTopic = "favorite-color-output"
    val intermediateTopic = "favorite-color-compacted"

    val properties = mapOf(
        StreamsConfig.APPLICATION_ID_CONFIG to "favorite-color-app",
        StreamsConfig.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
        StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG to Serdes.String()::class.java,
        StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG to Serdes.String()::class.java
    )
    val favoriteColor = FavoriteColor(inputTopic, outPutTopic, intermediateTopic, properties)
    favoriteColor.start()
}