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

import com.ninja_squad.dbsetup.destination.Destination
import com.ninja_squad.dbsetup.destination.DriverManagerDestination
import com.ninja_squad.dbsetup.generator.ValueGenerators
import com.ninja_squad.dbsetup_kotlin.dbSetup
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.Source
import org.assertj.db.type.Table
import org.assertj.db.type.Table.Order.asc
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CsvTest {

    private val order = arrayOf(asc("a"))

    lateinit var destination: Destination

    lateinit var source: Source

    @BeforeEach
    fun setUp() {
        val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
        destination = DriverManagerDestination(url, "sa", null)
        source = Source(url, "sa", null)
        dbSetup(destination) {
            sql("drop table if exists table_1")
            sql(
                """
                create table table_1 (
                  a integer primary key,
                  b integer
                )
                """
            )
        }.launch()
    }

    @Test
    fun into_table() {
        dbSetup(destination) {
            csv("into_table.csv").into("table_1")
        }.launch()
        assertThat(Table(source, "table_1", order))
            .row()
            .column("a").value().isEqualTo(10)
            .column("b").value().isEqualTo(100)
            .row()
            .column("a").value().isEqualTo(20)
            .column("b").value().isEqualTo(200)
    }

    @Test
    fun into_table_configure() {
        dbSetup(destination) {
            csv("into_table_configure.csv").into("table_1") {
                withGeneratedValue("a", ValueGenerators.sequence())
            }
        }.launch()
        assertThat(Table(source, "table_1", order))
            .row()
            .column("a").value().isEqualTo(1)
            .column("b").value().isEqualTo(100)
            .row()
            .column("a").value().isEqualTo(2)
            .column("b").value().isEqualTo(200)
    }
}
