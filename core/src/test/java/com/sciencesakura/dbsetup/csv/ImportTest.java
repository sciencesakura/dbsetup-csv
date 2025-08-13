// SPDX-License-Identifier: MIT

package com.sciencesakura.dbsetup.csv;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.sql;
import static com.ninja_squad.dbsetup.Operations.truncate;
import static com.sciencesakura.dbsetup.csv.Import.csv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.assertj.db.type.AssertDbConnection;
import org.assertj.db.type.AssertDbConnectionFactory;
import org.assertj.db.type.Changes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ImportTest {

  static final String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

  static final String username = "sa";

  AssertDbConnection connection;

  Destination destination;

  @BeforeEach
  void setUp() {
    connection = AssertDbConnectionFactory.of(url, username, null).create();
    destination = new DriverManagerDestination(url, username, null);
  }

  @Nested
  class DataTypes {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists data_types ("
          + "id uuid not null,"
          + "num1 smallint,"
          + "num2 integer,"
          + "num3 bigint,"
          + "num4 real,"
          + "num5 decimal(7,3),"
          + "text1 char(5),"
          + "text2 varchar(100),"
          + "date1 timestamp,"
          + "date2 date,"
          + "date3 time,"
          + "bool1 boolean,"
          + "primary key (id)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("data_types"))).launch();
      changes = connection.changes().table("data_types").build();
    }

    @Test
    void import_with_default_settings() {
      changes.setStartPointNow();
      var operation = csv("DataTypes/data_types.csv").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(2)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(new UUID(0, 1))
          .value("num1").isEqualTo(1000)
          .value("num2").isEqualTo(20000)
          .value("num3").isEqualTo(3000000000L)
          .value("num4").isEqualTo(400.75)
          .value("num5").isEqualTo(new BigDecimal("5000.333"))
          .value("text1").isEqualTo("aaa  ")
          .value("text2").isEqualTo("bbb")
          .value("date1").isEqualTo(LocalDateTime.parse("2001-02-03T10:20:30.456"))
          .value("date2").isEqualTo(LocalDate.parse("2001-02-03"))
          .value("date3").isEqualTo(LocalTime.parse("10:20:30"))
          .value("bool1").isTrue()
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(new UUID(0, 2))
          .value("num1").isNull()
          .value("num2").isNull()
          .value("num3").isNull()
          .value("num4").isNull()
          .value("num5").isNull()
          .value("text1").isNull()
          .value("text2").isNull()
          .value("date1").isNull()
          .value("date2").isNull()
          .value("date3").isNull()
          .value("bool1").isFalse();
    }
  }

  @Nested
  class CsvFile {

    @Test
    void throw_npe_if_location_is_null() {
      assertThatThrownBy(() -> csv(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("location must not be null");
    }

    @Test
    void throw_iae_if_location_has_bean_not_found() {
      assertThatThrownBy(() -> csv("not_found.csv"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("not_found.csv not found");
    }
  }

  @Nested
  class IntoTable {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists into_table ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("into_table"))).launch();
      changes = connection.changes().table("into_table").build();
    }

    @Test
    void resolve_table_name_from_file_name() {
      changes.setStartPointNow();
      var operation = csv("IntoTable/into_table.csv").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void specify_table_name_explicitly() {
      changes.setStartPointNow();
      var operation = csv("IntoTable/into_table_2.csv").into("into_table").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void throw_npe_if_table_name_is_null() {
      assertThatThrownBy(() -> csv("IntoTable/into_table_2.csv").into(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("table must not be null");
    }
  }

  @Nested
  class WithCharset {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_charset ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_charset"))).launch();
      changes = connection.changes().table("with_charset").build();
    }

    @Test
    void use_utf8_if_not_specified() {
      changes.setStartPointNow();
      var operation = csv("WithCharset/utf8.csv").into("with_charset").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("田中　太郎");
    }

    @Test
    void specify_charset_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithCharset/utf16.csv").into("with_charset")
          .withCharset(StandardCharsets.UTF_16).build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("田中　太郎");
    }

    @Test
    void specify_charset_as_string_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithCharset/cp932.csv").into("with_charset")
          .withCharset("CP932").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("田中　太郎");
    }

    @Test
    void throw_npe_if_charset_is_null() {
      assertThatThrownBy(() -> csv("WithCharset/utf8.csv").into("with_charset")
          .withCharset((Charset) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("charset must not be null");
    }

    @Test
    void throw_npe_if_string_charset_is_null() {
      assertThatThrownBy(() -> csv("WithCharset/utf8.csv").into("with_charset")
          .withCharset((String) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("charset must not be null");
    }
  }

  @Nested
  class WithDelimiter {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_delimiter ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_delimiter"))).launch();
      changes = connection.changes().table("with_delimiter").build();
    }

    @Test
    void use_comma_if_not_specified() {
      changes.setStartPointNow();
      var operation = csv("WithDelimiter/with_delimiter.csv").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void specify_delimiter_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithDelimiter/with_delimiter.tsv")
          .withDelimiter('\t').build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }
  }

  @Nested
  class WithHeader {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_header ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_header"))).launch();
      changes = connection.changes().table("with_header").build();
    }

    @Test
    void use_first_row_of_file_if_not_specified() {
      changes.setStartPointNow();
      var operation = csv("WithHeader/with_header.csv")
          .into("with_header").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void specify_header_as_array_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader("id", "name")
          .build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void specify_header_as_list_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader(List.of("id", "name"))
          .build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("Alice");
    }

    @Test
    void throw_npe_if_array_header_is_null() {
      assertThatThrownBy(() -> csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader((String[]) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("headers must not be null");
    }

    @Test
    void throw_npe_if_array_header_contains_null() {
      assertThatThrownBy(() -> csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader("id", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("headers must not contain null");
    }

    @Test
    void throw_npe_if_collection_header_is_null() {
      assertThatThrownBy(() -> csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader((Collection<String>) null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("headers must not be null");
    }

    @Test
    void throw_npe_if_collection_header_contains_null() {
      var headers = new ArrayList<String>();
      headers.add("id");
      headers.add(null);
      assertThatThrownBy(() -> csv("WithHeader/without_header.csv")
          .into("with_header")
          .withHeader(headers))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("headers must not contain null");
    }
  }

  @Nested
  class WithNullAs {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_null_as ("
          + "id integer primary key,"
          + "name char(4)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_null_as"))).launch();
      changes = connection.changes().table("with_null_as").build();
    }

    @Test
    void treat_empty_as_null_if_not_specified() {
      changes.setStartPointNow();
      var operation = csv("WithNullAs/with_null_as.csv").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(2)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isNull()
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(2)
          .value("name").isEqualTo("NULL");
    }

    @Test
    void specify_null_string_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithNullAs/with_null_as.csv")
          .withNullAs("NULL").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(2)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("    ")
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(2)
          .value("name").isNull();
    }

    @Test
    void throw_npe_if_null_string_is_null() {
      assertThatThrownBy(() -> csv("WithNullAs/with_null_as.csv")
          .withNullAs(null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("nullString must not be null");
    }
  }

  @Nested
  class WithQuote {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_quote ("
          + "id integer primary key,"
          + "name1 varchar(100),"
          + "name2 varchar(100),"
          + "name3 varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_quote"))).launch();
      changes = connection.changes().table("with_quote").build();
    }

    @Test
    void use_double_quote_if_not_specified() {
      changes.setStartPointNow();
      var operation = csv("WithQuote/with_double_quote.csv").into("with_quote").build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name1").isEqualTo("Hello, World!")
          .value("name2").isEqualTo("foo")
          .value("name3").isEqualTo("'bar'");
    }

    @Test
    void specify_single_quote_explicitly() {
      changes.setStartPointNow();
      var operation = csv("WithQuote/with_single_quote.csv").into("with_quote")
          .withQuote('\'')
          .build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(1)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name1").isEqualTo("Hello, World!")
          .value("name2").isEqualTo("\"foo\"")
          .value("name3").isEqualTo("bar");
    }
  }

  @Nested
  class WithDefaultValue {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_default_value ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_default_value"))).launch();
      changes = connection.changes().table("with_default_value").build();
    }

    @Test
    void specify_default_value() {
      changes.setStartPointNow();
      var operation = csv("WithDefaultValue/with_default_value.csv")
          .withDefaultValue("name", "DEFAULT")
          .build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(2)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(1)
          .value("name").isEqualTo("DEFAULT")
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(2)
          .value("name").isEqualTo("DEFAULT");
    }

    @Test
    void throw_npe_if_column_name_is_null() {
      assertThatThrownBy(() -> csv("WithDefaultValue/with_default_value.csv")
          .withDefaultValue(null, "DEFAULT"))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("column must not be null");
    }
  }

  @Nested
  class WithGeneratedValue {

    Changes changes;

    @BeforeEach
    void setUp() {
      var ddl = sql("create table if not exists with_generated_value ("
          + "id integer primary key,"
          + "name varchar(100)"
          + ")");
      new DbSetup(destination, sequenceOf(ddl, truncate("with_generated_value"))).launch();
      changes = connection.changes().table("with_generated_value").build();
    }

    @Test
    void specify_value_generator() {
      changes.setStartPointNow();
      var operation = csv("WithGeneratedValue/with_generated_value.csv")
          .withGeneratedValue("id", ValueGenerators.sequence().startingAt(10).incrementingBy(10))
          .build();
      new DbSetup(destination, operation).launch();
      assertThat(changes.setEndPointNow())
          .hasNumberOfChanges(2)
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(10)
          .value("name").isEqualTo("foo")
          .changeOfCreation()
          .rowAtEndPoint()
          .value("id").isEqualTo(20)
          .value("name").isEqualTo("bar");
    }

    @Test
    void throw_npe_if_column_name_is_null() {
      assertThatThrownBy(() -> csv("WithGeneratedValue/with_generated_value.csv")
          .withGeneratedValue(null, ValueGenerators.sequence()))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("column must not be null");
    }

    @Test
    void throw_npe_if_value_generator_is_null() {
      assertThatThrownBy(() -> csv("WithGeneratedValue/with_generated_value.csv")
          .withGeneratedValue("id", null))
          .isInstanceOf(NullPointerException.class)
          .hasMessage("valueGenerator must not be null");
    }
  }
}
