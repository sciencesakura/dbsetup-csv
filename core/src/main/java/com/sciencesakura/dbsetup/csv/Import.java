// SPDX-License-Identifier: MIT

package com.sciencesakura.dbsetup.csv;

import static java.util.Objects.requireNonNull;

import com.ninja_squad.dbsetup.DbSetupRuntimeException;
import com.ninja_squad.dbsetup.bind.BinderConfiguration;
import com.ninja_squad.dbsetup.generator.ValueGenerator;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.jspecify.annotations.NonNull;

/**
 * An operation which imports the CSV file into the database.
 *
 * <p>We recommend to import {@code csv} method statically so that your code looks clearer.</p>
 * <pre>{@code import static com.sciencesakura.dbsetup.csv.Import.csv;}</pre>
 *
 * <p>Then you can use {@code csv} method as follows:</p>
 * <pre>{@code
 * var operation = csv("test-items.csv").into("items").build();
 * var dbSetup = new DbSetup(destination, operation);
 * dbSetup.launch();
 * }</pre>
 *
 * @author sciencesakura
 */
public final class Import implements Operation {

  /**
   * Creates a new {@code Import.Builder} instance.
   *
   * @param location the {@code /}-separated path from classpath root to the CSV file
   * @return the new {@code Import.Builder} instance
   * @throws IllegalArgumentException if the CSV file is not found
   */
  @NonNull
  public static Builder csv(@NonNull String location) {
    var urlLocation = Import.class.getClassLoader()
        .getResource(requireNonNull(location, "location must not be null"));
    if (urlLocation == null) {
      throw new IllegalArgumentException(location + " not found");
    }
    return new Builder(urlLocation);
  }

  /**
   * Creates a new {@code Import.Builder} instance with TSV format.
   *
   * @param location the {@code /}-separated path from classpath root to the TSV file
   * @return the new {@code Import.Builder} instance
   * @throws IllegalArgumentException if the TSV file is not found
   */
  @NonNull
  public static Builder tsv(@NonNull String location) {
    return csv(location).withDelimiter('\t');
  }

  private static CSVFormat createFormat(Builder builder) {
    var fb = CSVFormat.Builder.create(CSVFormat.DEFAULT)
        .setDelimiter(builder.delimiter)
        .setNullString(builder.nullString)
        .setQuote(builder.quote)
        .setTrim(true);
    if (builder.headers == null) {
      fb.setHeader().setSkipHeaderRecord(true);
    } else {
      fb.setHeader(builder.headers);
    }
    return fb.get();
  }

  private final Operation internalOperation;

  private Import(Builder builder) {
    var ib = Insert.into(builder.table());
    try (var csv = CSVParser.parse(builder.location.openStream(), builder.charset, createFormat(builder))) {
      ib.columns(csv.getHeaderNames().toArray(new String[0]));
      builder.defaultValues.forEach(ib::withDefaultValue);
      builder.valueGenerators.forEach(ib::withGeneratedValue);
      csv.forEach(row -> ib.values((Object[]) row.values()));
    } catch (IOException e) {
      throw new DbSetupRuntimeException("failed to open " + builder.location, e);
    }
    internalOperation = ib.build();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void execute(Connection connection, BinderConfiguration configuration) throws SQLException {
    internalOperation.execute(connection, configuration);
  }

  /**
   * A builder to create the {@code Import} operation.
   * The builder instance is created by the static method {@link Import#csv(String)}.
   * <table class="striped">
   *   <caption>Settings</caption>
   *   <thead>
   *     <tr>
   *       <th>Property</th>
   *       <th>Default Value</th>
   *       <th>To Customize</th>
   *     </tr>
   *   </thead>
   *   <tbody>
   *     <tr>
   *       <th>Table for import</th>
   *       <td>CSV file name without extension</td>
   *       <td>{@link #into(String)}</td>
   *     </tr>
   *     <tr>
   *       <th>CSV file encoding</th>
   *       <td>UTF-8</td>
   *       <td>{@link #withCharset(Charset)} or {@link #withCharset(String)}</td>
   *     </tr>
   *     <tr>
   *       <th>CSV delimiter</th>
   *       <td>{@code ,} (comma)</td>
   *       <td>{@link #withDelimiter(char)}</td>
   *     </tr>
   *     <tr>
   *       <th>CSV header</th>
   *       <td>First row of the CSV file</td>
   *       <td>{@link #withHeader(Collection)} or {@link #withHeader(String...)}</td>
   *     </tr>
   *     <tr>
   *       <th>Representation of null value</th>
   *       <td>Empty string</td>
   *       <td>{@link #withNullAs(String)}</td>
   *     </tr>
   *     <tr>
   *       <th>CSV quotation mark</th>
   *       <td>{@code "} (double quote)</td>
   *       <td>{@link #withQuote(char)}</td>
   *     </tr>
   *   </tbody>
   * </table>
   *
   * @author sciencesakura
   */
  public static final class Builder {

    private final Map<String, Object> defaultValues = new LinkedHashMap<>();

    private final Map<String, ValueGenerator<?>> valueGenerators = new LinkedHashMap<>();

    private final URL location;

    private String table;

    private Charset charset = StandardCharsets.UTF_8;

    private char delimiter = ',';

    private String[] headers;

    private String nullString = "";

    private char quote = '"';

    private boolean built;

    private Builder(URL location) {
      this.location = location;
    }

    /**
     * Build a new {@code Import} operation instance.
     *
     * @return the new {@code Import} instance
     */
    @NonNull
    public Import build() {
      if (built) {
        throw new IllegalStateException("already built");
      }
      built = true;
      return new Import(this);
    }

    /**
     * Specifies a table name to import the CSV file.
     * By default, the table name is derived from the CSV file name without extension.
     *
     * @param table the table name to import the CSV file
     * @return the reference to this object
     */
    public Builder into(@NonNull String table) {
      this.table = requireNonNull(table, "table must not be null");
      return this;
    }

    /**
     * Specifies a character encoding to read the CSV file.
     *
     * <p>By default, the encoding is {@code UTF-8}.</p>
     *
     * @param charset the character encoding to read the CSV file
     * @return the reference to this object
     */
    public Builder withCharset(@NonNull Charset charset) {
      this.charset = requireNonNull(charset, "charset must not be null");
      return this;
    }

    /**
     * Specifies a character encoding to read the CSV file.
     *
     * <p>By default, the encoding is {@code UTF-8}.</p>
     *
     * @param charset the character encoding to read the CSV file
     * @return the reference to this object
     */
    public Builder withCharset(@NonNull String charset) {
      requireNonNull(charset, "charset must not be null");
      this.charset = Charset.forName(charset);
      return this;
    }

    /**
     * Specifies a default value for the given column.
     *
     * @param column the column name to set the default value
     * @param value  the default value (nullable)
     * @return the reference to this object
     */
    public Builder withDefaultValue(@NonNull String column, Object value) {
      requireNonNull(column, "column must not be null");
      defaultValues.put(column, value);
      return this;
    }

    /**
     * Specifies a delimiter to separate values in the CSV file.
     *
     * <p>By default, the delimiter is {@code ,} (comma).</p>
     *
     * @param delimiter the delimiter
     * @return the reference to this object
     */
    public Builder withDelimiter(char delimiter) {
      this.delimiter = delimiter;
      return this;
    }

    /**
     * Specifies a value generator for the given column.
     * The value generator is used to generate values for the column when inserting rows.
     *
     * @param column         the column name to set the value generator
     * @param valueGenerator the value generator to use
     * @return the reference to this object
     */
    public Builder withGeneratedValue(@NonNull String column, @NonNull ValueGenerator<?> valueGenerator) {
      requireNonNull(column, "column must not be null");
      requireNonNull(valueGenerator, "valueGenerator must not be null");
      valueGenerators.put(column, valueGenerator);
      return this;
    }

    /**
     * Specifies the headers of the CSV file.
     *
     * <p>By default, the first row of the CSV file is used as the header.</p>
     *
     * @param headers the headers of the CSV file
     * @return the reference to this object
     */
    public Builder withHeader(@NonNull Collection<String> headers) {
      requireNonNull(headers, "headers must not be null");
      this.headers = new String[headers.size()];
      var i = 0;
      for (var header : headers) {
        this.headers[i++] = requireNonNull(header, "headers must not contain null");
      }
      return this;
    }

    /**
     * Specifies the headers of the CSV file.
     *
     * <p>By default, the first row of the CSV file is used as the header.</p>
     *
     * @param headers the headers of the CSV file
     * @return the reference to this object
     */
    public Builder withHeader(@NonNull String... headers) {
      requireNonNull(headers, "headers must not be null");
      this.headers = new String[headers.length];
      var i = 0;
      for (var header : headers) {
        this.headers[i++] = requireNonNull(header, "headers must not contain null");
      }
      return this;
    }

    /**
     * Specifies a string to represent null values in the CSV file.
     *
     * <p>By default, an empty string is used to represent null values.</p>
     *
     * @param nullString the string to represent null values
     * @return the reference to this object
     */
    public Builder withNullAs(@NonNull String nullString) {
      this.nullString = requireNonNull(nullString, "nullString must not be null");
      return this;
    }

    /**
     * Specifies a quotation mark to enclose values in the CSV file.
     *
     * <p>By default, the quotation mark is {@code "} (double quote).</p>
     *
     * @param quote the quotation mark
     * @return the reference to this object
     */
    public Builder withQuote(char quote) {
      this.quote = quote;
      return this;
    }

    private String table() {
      if (table != null) {
        return table;
      }
      try {
        var filename = Path.of(location.toURI()).getFileName().toString();
        var p = filename.lastIndexOf('.');
        return p == -1 ? filename : filename.substring(0, p);
      } catch (URISyntaxException e) {
        throw new DbSetupRuntimeException(e);
      }
    }
  }
}
