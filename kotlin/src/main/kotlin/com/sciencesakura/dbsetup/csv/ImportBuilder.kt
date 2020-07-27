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

import com.ninja_squad.dbsetup_kotlin.DbSetupBuilder

/**
 * A builder to create the CSV import operation.
 */
class ImportBuilder internal constructor(
    private val csv: Import.CSV,
    private val builder: DbSetupBuilder
) {

    /**
     * Specifies the table and add the CSV import operation to the
     * `DbSetupBuilder`.
     *
     * @param table the table name
     */
    fun into(table: String) {
        builder.execute(csv.into(table).build())
    }

    /**
     * Specifies the table and add the CSV import operation to the
     * `DbSetupBuilder`.
     *
     * @param table     the table name
     * @param configure the function used to configure the csv import
     */
    fun into(table: String, configure: Import.Builder.() -> Unit) {
        val csvBuilder = csv.into(table)
        csvBuilder.configure()
        builder.execute(csvBuilder.build())
    }
}
