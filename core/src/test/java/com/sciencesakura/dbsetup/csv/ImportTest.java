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
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.destination.Destination;
import com.ninja_squad.dbsetup.generator.ValueGenerator;
import com.ninja_squad.dbsetup.generator.ValueGenerators;
import com.ninja_squad.dbsetup.operation.Operation;
import org.assertj.db.type.Table;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static com.ninja_squad.dbsetup.Operations.truncate;
import static com.sciencesakura.dbsetup.csv.Import.csv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.type.Table.Order.asc;

class ImportTest {

    private static final Table.Order[] ORDER_BY_PK = {asc("pk")};

    private static DataSource dataSource;

    private static Destination destination;

    @BeforeAll
    static void setUpClass() {
        String url = "jdbc:h2:mem:ImportTest;DB_CLOSE_DELAY=-1";
        FluentConfiguration conf = Flyway.configure().dataSource(url, "sa", null);
        conf.load().migrate();
        dataSource = conf.getDataSource();
        destination = DataSourceDestination.with(dataSource);
    }

    @Test
    void default_csv() {
        Operation operation = sequenceOf(
                truncate("table_1"),
                csv("data/default.csv").into("table_1")
                        .withGeneratedValue("pk", ValueGenerators.sequence())
                        .build());
        DbSetup dbSetup = new DbSetup(destination, operation);
        dbSetup.launch();
        assertThat(new Table(dataSource, "table_1", ORDER_BY_PK))
                .row()
                .column("pk").value().isEqualTo(1)
                .column("a").value().isEqualTo(100)
                .column("b").value().isEqualTo(10000000000L)
                .column("c").value().isEqualTo(0.5)
                .column("d").value().isEqualTo("2019-12-01")
                .column("e").value().isEqualTo("2019-12-01T09:30:01.001000000")
                .column("f").value().isEqualTo("AAA")
                .column("g").value().isEqualTo("甲")
                .column("h").value().isTrue()
                .column("i").value().isNotNull()
                .row()
                .column("pk").value().isEqualTo(2)
                .column("a").value().isEqualTo(200)
                .column("b").value().isEqualTo(20000000000L)
                .column("c").value().isEqualTo(0.25)
                .column("d").value().isEqualTo("2019-12-02")
                .column("e").value().isEqualTo("2019-12-02T09:30:02.002000000")
                .column("f").value().isEqualTo("BBB")
                .column("g").value().isEqualTo("乙")
                .column("h").value().isFalse()
                .column("i").value().isNull();
    }

    @Test
    void pgsql_tsv() {
        Operation operation = sequenceOf(
                truncate("table_1"),
                csv("data/pgsql.tsv").into("table_1")
                        .withGeneratedValue("pk", ValueGenerators.sequence())
                        .withDelimiter('\t')
                        .withHeader("a", "b", "c", "d", "e", "f", "g", "h", "i")
                        .withNullAs("\\N")
                        .build());
        DbSetup dbSetup = new DbSetup(destination, operation);
        dbSetup.launch();
        assertThat(new Table(dataSource, "table_1", ORDER_BY_PK))
                .row()
                .column("pk").value().isEqualTo(1)
                .column("a").value().isEqualTo(100)
                .column("b").value().isEqualTo(10000000000L)
                .column("c").value().isEqualTo(0.5)
                .column("d").value().isEqualTo("2019-12-01")
                .column("e").value().isEqualTo("2019-12-01T09:30:01.001000000")
                .column("f").value().isEqualTo("AAA")
                .column("g").value().isEqualTo("甲")
                .column("h").value().isTrue()
                .column("i").value().isNotNull()
                .row()
                .column("pk").value().isEqualTo(2)
                .column("a").value().isEqualTo(200)
                .column("b").value().isEqualTo(20000000000L)
                .column("c").value().isEqualTo(0.25)
                .column("d").value().isEqualTo("2019-12-02")
                .column("e").value().isEqualTo("2019-12-02T09:30:02.002000000")
                .column("f").value().isEqualTo("BBB")
                .column("g").value().isEqualTo("乙")
                .column("h").value().isFalse()
                .column("i").value().isNull();
    }

    @Nested
    class IllegalArgument {

        @Test
        void file_not_found() {
            assertThatThrownBy(() -> csv("data/file_not_found"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("data/file_not_found not found");
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
            assertThatThrownBy(() -> csv("data/default.csv").into(table))
                    .hasMessage("table must not be null");
        }

        @Test
        void charset_charset_is_null() {
            String table = "table";
            Charset charset = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withCharset(charset))
                    .hasMessage("charset must not be null");
        }

        @Test
        void string_charset_is_null() {
            String table = "table";
            String charset = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withCharset(charset))
                    .hasMessage("charset must not be null");
        }

        @Test
        void value_generator_column_is_null() {
            String table = "table";
            String column = null;
            ValueGenerator<?> valueGenerator = ValueGenerators.sequence();
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withGeneratedValue(column, valueGenerator))
                    .hasMessage("column must not be null");
        }

        @Test
        void value_generator_generator_is_null() {
            String table = "table";
            String column = "column";
            ValueGenerator<?> valueGenerator = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withGeneratedValue(column, valueGenerator))
                    .hasMessage("valueGenerator must not be null");
        }

        @Test
        void collection_header_is_null() {
            String table = "table";
            Collection<String> headers = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not be null");
        }

        @Test
        void collection_header_contains_null() {
            String table = "table";
            Collection<String> headers = Arrays.asList("a", null);
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not contain null");
        }

        @Test
        void array_header_is_null() {
            String table = "table";
            String[] headers = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not be null");
        }

        @Test
        void array_header_contains_null() {
            String table = "table";
            String[] headers = {"a", null};
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withHeader(headers))
                    .hasMessage("headers must not contain null");
        }

        @Test
        void nullString_is_null() {
            String table = "table";
            String nullString = null;
            assertThatThrownBy(() -> csv("data/default.csv").into(table)
                    .withNullAs(nullString))
                    .hasMessage("nullString must not be null");
        }
    }

    @Nested
    class IllegalState {

        @Test
        void build_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(ib::build)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void charset_charset_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withCharset(StandardCharsets.UTF_8))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void string_charset_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withCharset("UTF-8"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void delimiter_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withDelimiter(','))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void value_generator_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withGeneratedValue("column", ValueGenerators.sequence()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void collection_headers_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withHeader(Arrays.asList("a", "b")))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void array_headers_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withHeader("a", "b"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void nullString_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withNullAs("null"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }

        @Test
        void quote_after_built() {
            String table = "table";
            Import.Builder ib = csv("data/default.csv").into(table);
            ib.build();
            assertThatThrownBy(() -> ib.withQuote('"'))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("this operation has been built already");
        }
    }
}
