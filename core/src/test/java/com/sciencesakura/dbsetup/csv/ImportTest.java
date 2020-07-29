/*
 * MIT License
 *
 * Copyright (c) 2019 sciencesakura
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.sciencesakura.dbsetup.csv;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.destination.DriverManagerDestination;
import com.ninja_squad.dbsetup.generator.ValueGenerator;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Operation;
import org.assertj.db.type.Changes;
import org.assertj.db.type.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static com.ninja_squad.dbsetup.Operations.sql;
import static com.sciencesakura.dbsetup.csv.Import.csv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;

class ImportTest {

    private static final String url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1";

    private static final String username = "sa";

    private final Destination destination = new DriverManagerDestination(url, username, null);

    private final Source source = new Source(url, username, null);

    @BeforeEach
    void setUp() {
        String[] ddl = {
                "drop table if exists table_1 cascade",
                "create table table_1 (" +
                "  a   integer primary key," +
                "  b   bigint," +
                "  c   decimal(7, 3)," +
                "  d   date," +
                "  e   timestamp," +
                "  f   char(3)," +
                "  g   varchar(6)," +
                "  h   boolean," +
                "  i   varchar(6)" +
                ")"
        };
        new DbSetup(destination, sql(ddl)).launch();
    }

    @Test
    void default_csv() {
        Changes changes = new Changes(source).setStartPointNow();
        Operation operation = csv("default.csv")
                .into("table_1")
                .build();
        new DbSetup(destination, operation).launch();
        changes.setEndPointNow();
        validateChanges(changes);
    }

    @Test
    void pgsql_tsv() {
        Changes changes = new Changes(source).setStartPointNow();
        Operation operation = csv("pgsql.tsv")
                .into("table_1")
                .withDelimiter('\t')
                .withHeader("a", "b", "c", "d", "e", "f", "g", "h", "i")
                .withNullAs("\\N")
                .build();
        new DbSetup(destination, operation).launch();
        changes.setEndPointNow();
        validateChanges(changes);
    }

    @Test
    void generators() {
        Changes changes = new Changes(source).setStartPointNow();
        Operation operation = csv("generators.csv")
                .into("table_1")
                .withGeneratedValue("a", ValueGenerators.sequence())
                .withGeneratedValue("g", ValueGenerators.stringSequence("G-"))
                .build();
        new DbSetup(destination, operation).launch();
        changes.setEndPointNow();
        assertThat(changes).hasNumberOfChanges(2)
                .changeOfCreation()
                .rowAtEndPoint()
                .value("a").isEqualTo(1)
                .value("g").isEqualTo("G-1")
                .changeOfCreation()
                .rowAtEndPoint()
                .value("a").isEqualTo(2)
                .value("g").isEqualTo("G-2");
    }

    @Test
    void constants() {
        Changes changes = new Changes(source).setStartPointNow();
        Operation operation = csv("constants.csv")
                .into("table_1")
                .withDefaultValue("g", "G")
                .withDefaultValue("i", null)
                .build();
        new DbSetup(destination, operation).launch();
        changes.setEndPointNow();
        assertThat(changes).hasNumberOfChanges(2)
                .changeOfCreation()
                .rowAtEndPoint()
                .value("g").isEqualTo("G")
                .value("i").isNull()
                .changeOfCreation()
                .rowAtEndPoint()
                .value("g").isEqualTo("G")
                .value("i").isNull();
    }

    private void validateChanges(Changes changes) {
        assertThat(changes).hasNumberOfChanges(2)
                .changeOfCreation()
                .rowAtEndPoint()
                .value("a").isEqualTo(100)
                .value("b").isEqualTo(10000000000L)
                .value("c").isEqualTo(0.5)
                .value("d").isEqualTo("2019-12-01")
                .value("e").isEqualTo("2019-12-01T09:30:01.001000000")
                .value("f").isEqualTo("AAA")
                .value("g").isEqualTo("甲")
                .value("h").isTrue()
                .value("i").isNotNull()
                .changeOfCreation()
                .rowAtEndPoint()
                .value("a").isEqualTo(200)
                .value("b").isEqualTo(20000000000L)
                .value("c").isEqualTo(0.25)
                .value("d").isEqualTo("2019-12-02")
                .value("e").isEqualTo("2019-12-02T09:30:02.002000000")
                .value("f").isEqualTo("BBB")
                .value("g").isEqualTo("乙")
                .value("h").isFalse()
                .value("i").isNull();
    }

    static class IllegalArgument {

        @Test
        void file_not_found() {
            assertThatThrownBy(() -> csv("file_not_found"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("file_not_found not found");
        }

        @Test
        void location_is_null() {
            String location = null;
            assertThatThrownBy(() -> csv(location))
                    .hasMessage("location must not be null");
        }

        @Test
        void table_is_null() {
            String table = null;
            assertThatThrownBy(() -> csv("default.csv").into(table))
                    .hasMessage("table must not be null");
        }

        @Test
        void charset_charset_is_null() {
            String table = "table";
            Charset charset = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withCharset(charset))
                    .hasMessage("charset must not be null");
        }

        @Test
        void string_charset_is_null() {
            String table = "table";
            String charset = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withCharset(charset))
                    .hasMessage("charset must not be null");
        }

        @Test
        void default_value_column_is_null() {
            String table = "table";
            String column = null;
            Object value = new Object();
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withDefaultValue(column, value))
                    .hasMessage("column must not be null");
        }

        @Test
        void value_generator_column_is_null() {
            String table = "table";
            String column = null;
            ValueGenerator<?> valueGenerator = ValueGenerators.sequence();
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withGeneratedValue(column, valueGenerator))
                    .hasMessage("column must not be null");
        }

        @Test
        void value_generator_generator_is_null() {
            String table = "table";
            String column = "column";
            ValueGenerator<?> valueGenerator = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withGeneratedValue(column, valueGenerator))
                    .hasMessage("valueGenerator must not be null");
        }

        @Test
        void collection_header_is_null() {
            String table = "table";
            Collection<String> headers = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not be null");
        }

        @Test
        void collection_header_contains_null() {
            String table = "table";
            Collection<String> headers = Arrays.asList("a", null);
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not contain null");
        }

        @Test
        void array_header_is_null() {
            String table = "table";
            String[] headers = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not be null");
        }

        @Test
        void array_header_contains_null() {
            String table = "table";
            String[] headers = {"a", null};
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not contain null");
        }

        @Test
        void nullString_is_null() {
            String table = "table";
            String nullString = null;
            assertThatThrownBy(() -> csv("default.csv").into(table)
                    .withNullAs(nullString))
                    .hasMessage("nullString must not be null");
        }
    }

    static class IllegalState {

        @Test
        void build_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(ib::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void charset_charset_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withCharset(StandardCharsets.UTF_8))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void string_charset_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withCharset("UTF-8"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void default_value_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withDefaultValue("column", "value"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void delimiter_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withDelimiter(','))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void value_generator_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withGeneratedValue("column", ValueGenerators.sequence()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void collection_headers_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withHeader(Arrays.asList("a", "b")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void array_headers_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withHeader("a", "b"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void nullString_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withNullAs("null"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void quote_after_built() {
            String table = "table";
            Import.Builder ib = csv("default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withQuote('"'))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }
    }
}
