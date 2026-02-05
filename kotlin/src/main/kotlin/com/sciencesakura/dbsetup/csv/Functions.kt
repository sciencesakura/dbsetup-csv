// SPDX-License-Identifier: MIT

package com.sciencesakura.dbsetup.csv

import com.ninja_squad.dbsetup_kotlin.DbSetupBuilder

/**
 * Creates a CSV import operation.
 *
 * @param location the `/`-separated path from classpath root to the CSV file
 * @throws IllegalArgumentException if the CSV file is not found
 */
fun DbSetupBuilder.csv(location: String) {
  this.execute(Import.csv(location).build())
}

/**
 * Creates a CSV import operation.
 *
 * @param location the `/`-separated path from classpath root to the CSV file
 * @param configure A lambda to configure the import operation
 * @throws IllegalArgumentException if the CSV file is not found
 */
fun DbSetupBuilder.csv(
  location: String,
  configure: Import.Builder.() -> Unit,
) {
  val builder = Import.csv(location)
  builder.configure()
  this.execute(builder.build())
}

/**
 * Creates a TSV import operation.
 *
 * @param location the `/`-separated path from classpath root to the TSV file
 * @throws IllegalArgumentException if the TSV file is not found
 */
fun DbSetupBuilder.tsv(location: String) {
  this.execute(Import.tsv(location).build())
}

/**
 * Creates a TSV import operation.
 *
 * @param location the `/`-separated path from classpath root to the TSV file
 * @param configure A lambda to configure the import operation
 * @throws IllegalArgumentException if the TSV file is not found
 */
fun DbSetupBuilder.tsv(
  location: String,
  configure: Import.Builder.() -> Unit,
) {
  val builder = Import.tsv(location)
  builder.configure()
  this.execute(builder.build())
}
