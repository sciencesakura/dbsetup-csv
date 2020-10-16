# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

A [DbSetup](http://dbsetup.ninja-squad.com/) extension to import data from CSV/TSV files.

![](https://github.com/sciencesakura/dbsetup-csv/workflows/build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv)

## Requirement

* Java 8+

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:1.1.0'

// optional - Kotlin Extensions
testImplementation 'com.sciencesakura:dbsetup-csv-kt:1.1.0'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>1.1.0</version>
  <scope>test</scope>
</dependency>

<!-- optional - Kotlin Extensions -->
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv-kt</artifactId>
  <version>1.1.0</version>
  <scope>test</scope>
</dependency>
```

## Usage

Java:

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;

// `testdata.csv` must be in classpath.
Operation operation = csv("testdata.csv").into("tablename").build();
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

Kotlin:

```kotlin
import com.sciencesakura.dbsetup.csv.csv

dbSetup(destination) {
    csv("testdata.csv").into("tablename")
}.launch()
```

See [API reference](https://sciencesakura.github.io/dbsetup-csv/) for details.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura
