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
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static com.sciencesakura.dbsetup.csv.Import.csv;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.db.api.Assertions.assertThat;
import static org.assertj.db.type.Table.Order.asc;

class ImportTest {

    private static final Table.Order[] ORDER_BY_A = new Table.Order[] { asc("a") };

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

    @Test
    void file_not_found() {
        assertThatThrownBy(() -> csv("data/file_not_found"))
                .hasMessage("specified resource was not found: data/file_not_found");
    }

    private static DateTimeValue datetime(int year, int month, int dayOfMonth, int hours, int minutes, int seconds) {
        DateValue d = DateValue.of(year, month, dayOfMonth);
        TimeValue t = TimeValue.of(hours, minutes, seconds);
        return DateTimeValue.of(d, t);
    }
}
