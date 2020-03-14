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
import com.ninja_squad.dbsetup.operation.Operation;
import org.assertj.db.type.DateTimeValue;
import org.assertj.db.type.DateValue;
import org.assertj.db.type.Source;
import org.assertj.db.type.Table;
import org.assertj.db.type.TimeValue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;

import static com.sciencesakura.dbsetup.csv.Import.csv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.type.Table.Order.asc;

class ImportTest {

    private static final Table.Order[] ORDER_BY_A = new Table.Order[] {asc("a")};

    private static Destination destination;

    private static Source source;

    @BeforeAll
    static void setUpClass() throws SQLException {
        String url = "jdbc:h2:mem:ImportTest;DB_CLOSE_DELAY=-1";
        destination = DriverManagerDestination.with(url, "sa", "");
        source = new Source(url, "sa", "");
        try (Connection conn = destination.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.addBatch("create table default_csv (a int, b decimal(7, 3), c date, d timestamp, e varchar(6), f boolean)");
            stmt.addBatch("create table pgsql_tsv (a int, b decimal(7, 3), c date, d timestamp, e varchar(6), f boolean)");
            stmt.executeBatch();
        }
    }

    @Test
    void default_csv() {
        Operation operation = csv("data/default.csv").into("default_csv").build();
        DbSetup dbSetup = new DbSetup(destination, operation);
        dbSetup.launch();
        assertThat(new Table(source, "default_csv", ORDER_BY_A))
                // 1
                .row()
                .column("a").value().isEqualTo(100)
                .column("b").value().isEqualTo(0.5)
                .column("c").value().isEqualTo(DateValue.of(2019, 12, 1))
                .column("d").value().isEqualTo(datetime(2019, 12, 1, 9, 30, 1))
                .column("e").value().isEqualTo("hoge")
                .column("f").value().isTrue()
                // 2
                .row()
                .column("a").value().isEqualTo(200)
                .column("b").value().isEqualTo(0.25)
                .column("c").value().isEqualTo(DateValue.of(2019, 12, 2))
                .column("d").value().isEqualTo(datetime(2019, 12, 2, 9, 30, 2))
                .column("e").value().isNull()
                .column("f").value().isFalse();
    }

    @Test
    void pgsql_tsv() {
        Operation operation = csv("data/pgsql.tsv")
                .into("pgsql_tsv")
                .withDelimiter('\t')
                .withHeader("a", "b", "c", "d", "e", "f")
                .withNullAs("\\N")
                .build();
        DbSetup dbSetup = new DbSetup(destination, operation);
        dbSetup.launch();
        assertThat(new Table(source, "pgsql_tsv", ORDER_BY_A))
                // 1
                .row()
                .column("a").value().isEqualTo(100)
                .column("b").value().isEqualTo(0.5)
                .column("c").value().isEqualTo(DateValue.of(2019, 12, 1))
                .column("d").value().isEqualTo(datetime(2019, 12, 1, 9, 30, 1))
                .column("e").value().isEqualTo("hoge")
                .column("f").value().isTrue()
                // 2
                .row()
                .column("a").value().isEqualTo(200)
                .column("b").value().isEqualTo(0.25)
                .column("c").value().isEqualTo(DateValue.of(2019, 12, 2))
                .column("d").value().isEqualTo(datetime(2019, 12, 2, 9, 30, 2))
                .column("e").value().isNull()
                .column("f").value().isFalse();
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
            String[] headers = new String[] {"a", null};
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

    private static DateTimeValue datetime(int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
        DateValue d = DateValue.of(year, month, dayOfMonth);
        TimeValue t = TimeValue.of(hours, minutes, seconds);
        return DateTimeValue.of(d, t);
    }
}
