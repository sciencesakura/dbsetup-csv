/*
 * MIT License
 *
 * Copyright (c) 2020 sciencesakura
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
package com.sciencesakura.dbsetup.csv

import com.ninja_squad.dbsetup.destination.DriverManagerDestination
import com.ninja_squad.dbsetup_kotlin.dbSetup
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.Changes
import org.assertj.db.type.Source
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CsvTest {

    val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

    val username = "sa"

    val source = Source(url, username, null)

    val destination = DriverManagerDestination.with(url, username, null)

    @BeforeEach
    fun setUp() {
        val ddl = """
            create table if not exists kt_test (
              id integer primary key,
              name varchar(100)
            )
        """.trimIndent()
        dbSetup(destination) {
            sql(ddl)
            truncate("kt_test")
        }.launch()
    }

    @Test
    fun import_csv() {
        val changes = Changes(source).setStartPointNow()
        dbSetup(destination) {
            csv("kt_test.csv")
        }.launch()
        assertThat(changes.setEndPointNow())
            .hasNumberOfChanges(1)
            .changeOfCreation()
            .rowAtEndPoint()
            .value("id").isEqualTo(1)
            .value("name").isEqualTo("foo")
    }

    @Test
    fun import_csv_with_configure() {
        val changes = Changes(source).setStartPointNow()
        dbSetup(destination) {
            csv("kt_test.tsv") {
                withDelimiter('\t')
            }
        }.launch()
        assertThat(changes.setEndPointNow())
            .hasNumberOfChanges(1)
            .changeOfCreation()
            .rowAtEndPoint()
            .value("id").isEqualTo(1)
            .value("name").isEqualTo("foo")
    }
}
