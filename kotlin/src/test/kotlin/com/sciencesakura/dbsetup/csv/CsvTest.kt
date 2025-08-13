// SPDX-License-Identifier: MIT

package com.sciencesakura.dbsetup.csv

import com.ninja_squad.dbsetup.destination.DriverManagerDestination
import com.ninja_squad.dbsetup_kotlin.dbSetup
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.AssertDbConnectionFactory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CsvTest {
  val url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"

  val username = "sa"

  val connection = AssertDbConnectionFactory.of(url, username, null).create()

  val destination = DriverManagerDestination.with(url, username, null)

  @BeforeEach
  fun setUp() {
    val ddl =
      """
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
    val changes = connection.changes().table("kt_test").build()
    changes.setStartPointNow()
    dbSetup(destination) {
      csv("kt_test.csv")
    }.launch()
    assertThat(changes.setEndPointNow())
      .hasNumberOfChanges(1)
      .changeOfCreation()
      .rowAtEndPoint()
      .value("id")
      .isEqualTo(1)
      .value("name")
      .isEqualTo("foo")
  }

  @Test
  fun import_csv_with_configure() {
    val changes = connection.changes().table("kt_test").build()
    changes.setStartPointNow()
    dbSetup(destination) {
      csv("kt_test.tsv") {
        withDelimiter('\t')
      }
    }.launch()
    assertThat(changes.setEndPointNow())
      .hasNumberOfChanges(1)
      .changeOfCreation()
      .rowAtEndPoint()
      .value("id")
      .isEqualTo(1)
      .value("name")
      .isEqualTo("foo")
  }
}
