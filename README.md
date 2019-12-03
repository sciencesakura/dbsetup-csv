# dbsetup-csv: Import CSV using DbSetup

A [DbSetup](http://dbsetup.ninja-squad.com/) extension to import data from external CSV/TSV files.

## Requirement

* Java 8 or later

## Installation

Gradle:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:0.0.1'
```

Maven:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>0.0.1</version>
  <scope>test</scope>
</dependency>
```

## Usage

Here are a few examples:

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;
```

```java
// `data/customer.csv` must be in classpath.
Operation operation = sequenceOf(
    truncate("customer"),
    csv("data/customer.csv").into("customer").build()
);
DbSetup dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

By default, the source file is treated as an UTF-8-encoded and comma-delimited file. And the first line of the source file is used as a header. Of course you can customize it:

```java
// import an ms932-encoded, tab-delimited and no-header file
csv("data/vender.tsv").into("vender")
    .withCharset("ms932")
    .withDelimiter('\t')
    .withHeader("id", "vender_no", "vender_name", "tel_no", "email_address")
    .build()
```

See [API reference](https://sciencesakura.github.io/dbsetup-csv/) for details.

## Recommendation

We recommend using this extension only when the destination table has too many columns to keep your code using the [Insert.Builder](http://dbsetup.ninja-squad.com/apidoc/2.1.0/com/ninja_squad/dbsetup/operation/Insert.Builder.html) class readable.

## License

MIT License

Copyright (c) 2019 sciencesakura
