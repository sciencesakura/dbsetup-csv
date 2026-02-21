# dbsetup-csv: Import CSV into database with DbSetup

![](https://github.com/sciencesakura/dbsetup-csv/actions/workflows/build.yaml/badge.svg) [![Maven Central](https://maven-badges.sml.io/sonatype-central/com.sciencesakura/dbsetup-csv/badge.svg)](https://maven-badges.sml.io/sonatype-central/com.sciencesakura/dbsetup-csv)

A [DbSetup](http://dbsetup.ninja-squad.com/) extension for importing CSV files into the database.

## Requirements

* Java 11+

## Installation

The dbsetup-csv library is available on Maven Central. You can install it using your build system of choice.

### Gradle

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv:3.0.2'
```

If you are using Kotlin, you can use the Kotlin module for a more concise DSL:

```groovy
testImplementation 'com.sciencesakura:dbsetup-csv-kt:3.0.2'
```

### Maven

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv</artifactId>
  <version>3.0.2</version>
  <scope>test</scope>
</dependency>
```

If you are using Kotlin, you can use the Kotlin module for a more concise DSL:

```xml
<dependency>
  <groupId>com.sciencesakura</groupId>
  <artifactId>dbsetup-csv-kt</artifactId>
  <version>3.0.2</version>
  <scope>test</scope>
</dependency>
```

## Usage

### Import CSV/TSV file

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;
import com.ninja_squad.dbsetup.DbSetup;

// The operation to import a CSV file into a table
var operation = csv("test-items.csv").into("items").build();
// when importing a TSV file:
// var operation = tsv("test-items.tsv").into("items").build();

// Create a `DbSetup` instance with the operation and execute it
var dbSetup = new DbSetup(destination, operation);
dbSetup.launch();
```

### Clear table before import

```java
import static com.ninja_squad.dbsetup.Operations.*;
import static com.sciencesakura.dbsetup.csv.Import.csv;
import com.ninja_squad.dbsetup.DbSetup;

// The operations to clear the table and then import the CSV file
var operations = sequenceOf(
    deleteAllFrom("items"),
    csv("test-items.csv").into("items").build()
);

var dbSetup = new DbSetup(destination, operations);
dbSetup.launch();
```

### Use generated values and fixed values

```java
import static com.sciencesakura.dbsetup.csv.Import.csv;
import com.ninja_squad.dbsetup.generator.ValueGenerators;

var operation = csv("test-items.csv").into("items")
    // Generate a random UUID for the `id` column
    .withGeneratedValue("id", () -> UUID.randomUUID().toString())
    // Generate a sequential string for the `name` column, starting with "item-001"
    .withGeneratedValue("name", ValueGenerators.stringSequence("item-").withLeftPadding(3))
    // Set a fixed value for the `created_at` column
    .withDefaultValue("created_at", "2023-01-01 10:20:30")
    .build();
```

### Use Kotlin DSL

```kotlin
import com.ninja_squad.dbsetup_kotlin.dbSetup
import com.sciencesakura.dbsetup.csv.csv

dbSetup(destination) {
  csv("test-items.csv") {
    into("items")
    withGeneratedValue("id") { UUID.randomUUID().toString() }
  }
}.launch()
```

See [API reference](https://sciencesakura.github.io/dbsetup-csv/) for more details.

## Prefer Excel?

→ [dbsetup-spreadsheet](https://github.com/sciencesakura/dbsetup-spreadsheet)

## License

This library is licensed under the MIT License.

Copyright (c) 2019 sciencesakura
