# dbsetup-csv: Import CSV using DbSetup

A [DbSetup](http://dbsetup.ninja-squad.com/) extention to import data from external CSV/TSV files.

## Usage

Here are a few examples:

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;
```

```java
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
    .withDelimiter("\t")
    .withHeader("id", "vender_no", "vender_name", "tel_no", "email_address")
    .build()
```

## Recommended

We recommend using this extention only when the destination table has too many columns to keep your code readable.

## LICENSE

MIT License

Copyright (c) 2019 sciencesakura
