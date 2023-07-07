# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

A [DbSetup](http://dbsetup.ninja-squad.com/) extension to import data from CSV/TSV files.

![](https://github.com/sciencesakura/dbsetup-csv/actions/workflows/check.yaml/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv)

## Requirements

* Java 8+

## Installation

### Gradle

#### Java

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:2.0.2'
```

#### Kotlin

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv-kt:2.0.2'
```

### Maven

#### Java

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>2.0.2</version>
  <scope>test</scope>
</dependency>
```

#### Kotlin

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv-kt</artifactId>
  <version>2.0.2</version>
  <scope>test</scope>
</dependency>
```

## Usage

### Java

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;

@BeforeEach
void setUp() {
  var operations = sequenceOf(
    truncate("my_table"),
    // `testdata.csv` must be in classpath.
    csv("testdata.csv").into("my_table").build());
  var dbSetup = new DbSetup(destination, operations);
  dbSetup.launch();
}
```

### Kotlin

```kotlin
import com.sciencesakura.dbsetup.csv.csv

@BeforeEach
fun setUp() {
  dbSetup(destination) {
    // `testdata.csv` must be in classpath.
    csv("testdata.csv") {
      into("my_table")
    }
  }.launch()
}
```

See [API reference](https://sciencesakura.github.io/dbsetup-csv/) for details.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura
