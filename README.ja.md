# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

CSV/TSVファイルからデータ取り込みができる[DbSetup](http://dbsetup.ninja-squad.com/)拡張機能です.

![](https://github.com/sciencesakura/dbsetup-csv/actions/workflows/check.yaml/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv)

## Requirements

* Java 11+

## Installation

### Gradle

#### Java

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:3.0.0'
```

#### Kotlin

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv-kt:3.0.0'
```

### Maven

#### Java

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>3.0.0</version>
  <scope>test</scope>
</dependency>
```

#### Kotlin

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv-kt</artifactId>
  <version>3.0.0</version>
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
    // `testdata.csv`はクラスパス上にある必要があります
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
    // `testdata.csv`はクラスパス上にある必要があります
    csv("testdata.csv") {
      into("my_table")
    }
  }.launch()
}
```

詳細は[APIリファレンス](https://sciencesakura.github.io/dbsetup-csv/)を参照して下さい.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura
