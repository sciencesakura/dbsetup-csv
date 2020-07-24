# dbsetup-csv: Import CSV using DbSetup

[English](README.md) | [日本語](README.ja.md)

A [DbSetup](http://dbsetup.ninja-squad.com/) extension to import data from CSV/TSV files.

![](https://github.com/sciencesakura/dbsetup-csv/workflows/build/badge.svg)

## Requirement

* Java 8 or later

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:1.0.1'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>1.0.1</version>
  <scope>test</scope>
</dependency>
```

## Usage

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;

// `testdata.csv` must be in classpath.
Operation operation = csv("testdata.csv").into("tablename").build()
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

By default, the source file is treated as an UTF-8-encoded and comma-delimited file. And the first line of the source file is used as a header. Of course you can customize it:

```java
// import an ms932-encoded, tab-delimited and no-header file
csv("testdata.tsv").into("tablename")
    .withCharset("ms932")
    .withDelimiter('\t')
    .withHeader("column_1", "column_2", "column_3")
    .build()
```

See [API reference](https://sciencesakura.github.io/dbsetup-csv/) for details.

## Recommendation

We recommend using this extension only when the destination table has too many columns to keep your code using the [Insert.Builder](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/operation/Insert.Builder.html) class readable.

## Prefer Excel ?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

MIT License

Copyright (c) 2019 sciencesakura
