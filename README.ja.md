# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

CSV/TSVファイルからデータ取り込みができる[DbSetup](http://dbsetup.ninja-squad.com/)拡張機能です.

![](https://github.com/sciencesakura/dbsetup-csv/workflows/build/badge.svg) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sciencesakura/dbsetup-csv)

## Requirements

* Java 8+

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:2.0.2'

// optional - Kotlin Extensions
testImplementation 'com.sciencesakura:dbsetup-csv-kt:2.0.2'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>2.0.2</version>
  <scope>test</scope>
</dependency>

<!-- optional - Kotlin Extensions -->
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv-kt</artifactId>
  <version>2.0.2</version>
  <scope>test</scope>
</dependency>
```

## Usage

Java:

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;

// `testdata.csv`はクラスパス上にある必要があります
Operation operation = csv("testdata.csv").build();
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

Kotlin:

```kotlin
import com.sciencesakura.dbsetup.csv.csv

dbSetup(destination) {
    csv("testdata.csv")
}.launch()
```

詳細は[APIリファレンス](https://sciencesakura.github.io/dbsetup-csv/)を参照して下さい.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura
