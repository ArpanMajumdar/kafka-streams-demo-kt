# kafka-streams-demo-kt
Kafka streams demo application for kotlin

## Internal topics

Running kafka streams may create internal intermediary topics. They are of 2 types
    1. **Repartitioning topics** - In case you start transforming the key of your stream, a repartitioning will happen at some processor.
    2. **Changelog topics** - In case you perform aggregations, Kafka streams will save compacted data in these topics.
    
Internal topics are

- managed by Kafka streams
- are used by kafka streams to save/restore state and repartition data
- are prefixed by `application.id` parameter. 
- should never be deleted, altered or published to. They are internal to kafka streams.

## KStreams

- All inserts
- Similar to a log
- Infinite
- Unbounded data streams

## KTable

- All upserts on non null values
- Deletes on null values
- Similar to a table
- Parallel with log compacted topics

## When to use KStream vs KTable?

| KStream | KTable 
| --- | ---
| Reading from a topic that is not log compacted. | Reading from a log compacted topic.
| If new data is partial information. | If you need a structure like database table where every record is self sufficient.

## KStream and KTable functions

### `mapValues/map`

| mapValues | map
| --- | ---
| Only affects values | Affects both keys and values
| Does not change keys | May change keys
| Does not trigger a repartition | Triggers repartition
| For both KStreams and KTables | For KStreams only

### `flatMapValues/flatMap`

| flatMapValues | flatMap
| --- | ---
| does not change keys | changes keys
| does not trigger repartition | triggers repartition
| For KStreams only | For KStreams only

### KStream branch

- Branch a KStream based on one or more predicates
- Predicates are evaluated in order, if no matches, records are dropped
- Multiple streams as result

### KStream `selectKey`

- Assigns a new key to the record
- Marks data for re-partitioning
- Best practice is to isolate the transformation to know exactly where the partitioning happens.

## Reading from kafka

You can read a topic as a **KStream**, **KTable** or a **GlobalKTable**.

### KStream

```kotlin
val wordCounts : KStream<String , Long> = builder.stream(
    Serdes.String(),
    Serdes.Long(),
    "word-count-topic"
)
```

### KTable

```kotlin
val wordCounts : KStream<String , Long> = builder.table(
    Serdes.String(),
    Serdes.Long(),
    "word-count-topic"
)
```

### GlobalKTable

```kotlin
val wordCounts : KStream<String , Long> = builder.globalTable(
    Serdes.String(),
    Serdes.Long(),
    "word-count-topic"
)
```

## Writing to kafka

- You can write any KStream or KTable back to Kafka
- If you write a KTable, think about creating a log compacted topic

There are two ways to write to topic

1. **To**

Terminal operation - write the records to topic

```kotlin
stream.to("stream-output-topic")  // For streams
stream.to("table-output-topic")   // For tables
```

2. **Through**

Write to a topic and get a stream/table from the topic

```kotlin
val newStream : KStream<String, Long> = stream.through("stream-output-topic")   // For streams
val newTable : KTable<String, Long> = stream.to("table-output-topic")           // For tables
```

## Streams marked for repartition

- As soon as an operation can possibly change the key, the stream will be marked for repartition.
    - Map
    - FlatMap
    - SelectKey
- Use above APIs only if you need to change the key, otherwise use their counterparts.
    - MapValues
    - FlatMapValues
- Repartitioning is done seamlessly behind the scenes but will incur a performance cost.

## KStream & KTable duality

- **Stream as table** - A stream can be considered as a changelog of a table, where each data record in the stream 
captures a state change of the table.
- **Table as a stream** - A table can be considered a snapshot at a point in time of the latest value of each key in a stream.

## Trnasforming a KTable to KStream

- It is sometimes helpful to transform a KTable to KStream in order to keep a changelog of all changes to the KTable.

```kotlin
val table : KTable<byte[], String> = ...
val stream: KStream<byte[], String> = table.toStream()
```

## Transforming a KStream to KTable
There are two ways to do this
- Chain a `groupByKey()` and an agggregation step (count, aggregate, reduce)

```kotlin
val table: <String, Long> = source.groupByKey().count()
```

- Write back to kafka and read as KTable
```kotlin
// Write to kafka
stream.to("intermediate-topic")
val table: <String, Long> = builder.table("intermediate-topic")
```

