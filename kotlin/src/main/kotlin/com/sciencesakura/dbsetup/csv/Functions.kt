// SPDX-License-Identifier: MIT

package com.sciencesakura.dbsetup.csv

import com.ninja_squad.dbsetup_kotlin.DbSetupBuilder

/**
 * Add a CSV import operation to the `DbSetupBuilder`.
 *
 * @param location the location of the source file that is the relative path from classpath root
 * @throws IllegalArgumentException if the source file was not found
 */
fun DbSetupBuilder.csv(location: String) {
  this.execute(Import.csv(location).build())
}

/**
 * Add a CSV import operation to the `DbSetupBuilder`.
 *
 * @param location  the location of the source file that is the relative path from classpath root
 * @param configure the function used to configure the CSV import
 * @throws IllegalArgumentException if the source file was not found
 */
fun DbSetupBuilder.csv(
  location: String,
  configure: Import.Builder.() -> Unit,
) {
  val builder = Import.csv(location)
  builder.configure()
  this.execute(builder.build())
}
