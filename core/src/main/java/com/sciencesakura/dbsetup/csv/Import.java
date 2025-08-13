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
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.jetbrains.annotations.NotNull;

/**
 * An operation which imports the CSV file into the specified table.
 *
 * <h2>Usage</h2>
 * <p>We recommend to import {@code csv} method statically so that your code looks clearer.</p>
 * <pre>
 * {@code import static com.sciencesakura.dbsetup.csv.Import.csv;}
 * </pre>
 * <p>Then you can use {@code csv} method as follows:</p>
 * <pre>
 * {@code @BeforeEach
 * void setUp() {
 *   var operations = sequenceOf(
 *     truncate("my_table"),
 *     csv("testdata.csv").into("my_table").build());
 *   var dbSetup = new DbSetup(destination, operations);
 *   dbSetup.launch();
 * }
 * }
 * </pre>
 *
 * @author sciencesakura
 */
public final class Import implements Operation {

  /**
   * Creates a new {@code Import.Builder} instance.
   * <p>
   * The specified location string must be the relative path string from classpath root.
   * </p>
   *
   * @param location the location of the source file that is the relative path from classpath root
   * @return the new {@code Import.Builder} instance
   * @throws IllegalArgumentException if the source file was not found
   */
  @NotNull
  public static Builder csv(@NotNull String location) {
    var urlLocation = Import.class.getClassLoader()
        .getResource(requireNonNull(location, "location must not be null"));
    if (urlLocation == null) {
      throw new IllegalArgumentException(location + " not found");
    }
    return new Builder(urlLocation);
  }

  private static CSVFormat createFormat(Builder builder) {
    CSVFormat.Builder fb = CSVFormat.Builder.create(CSVFormat.DEFAULT)
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

  @Override
  public void execute(Connection connection, BinderConfiguration configuration) throws SQLException {
    internalOperation.execute(connection, configuration);
  }

  /**
   * A builder to create the {@code Import} instance.
   *
   * <h2>Usage</h2>
   * <p>The default settings are:</p>
   * <ul>
   *   <li>{@code withCharset("UTF-8")}</li>
   *   <li>{@code withDelimiter(',')}</li>
   *   <li>{@code withNullAs("")}</li>
   *   <li>{@code withQuote('"')}</li>
   *   <li>The source file name excluding extension is used as the table name.</li>
   *   <li>The first row of the source file is treated as the header row.</li>
   * </ul>
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
     * Build a new {@code Import} instance.
     *
     * @return the new {@code Import} instance
     */
    @NotNull
    public Import build() {
      if (built) {
        throw new IllegalStateException("already built");
      }
      built = true;
      return new Import(this);
    }

    /**
     * Specifies a table name to insert into.
     * <p>
     * By default the name of source file (without extension) is used.
     * </p>
     *
     * @param table the table name
     * @return the reference to this object
     */
    public Builder into(@NotNull String table) {
      this.table = requireNonNull(table, "table must not be null");
      return this;
    }

    /**
     * Specifies a charset of the source file.
     * <p>
     * By default UTF-8 is used.
     * </p>
     *
     * @param charset the charset
     * @return the reference to this object
     */
    public Builder withCharset(@NotNull Charset charset) {
      this.charset = requireNonNull(charset, "charset must not be null");
      return this;
    }

    /**
     * Specifies a charset of the source file.
     * <p>
     * By default UTF-8 is used.
     * </p>
     *
     * @param charset the charset name
     * @return the reference to this object
     */
    public Builder withCharset(@NotNull String charset) {
      requireNonNull(charset, "charset must not be null");
      this.charset = Charset.forName(charset);
      return this;
    }

    /**
     * Specifies a default value for the given column.
     *
     * @param column the column name
     * @param value  the default value
     * @return the reference to this object
     */
    public Builder withDefaultValue(@NotNull String column, Object value) {
      requireNonNull(column, "column must not be null");
      defaultValues.put(column, value);
      return this;
    }

    /**
     * Specifies a delimiter character of the source file.
     * <p>
     * By default {@code ','} is used.
     * </p>
     *
     * @param delimiter the delimiter character
     * @return the reference to this object
     */
    public Builder withDelimiter(char delimiter) {
      this.delimiter = delimiter;
      return this;
    }

    /**
     * Specifies a value generator for the given column.
     *
     * @param column         the column name
     * @param valueGenerator the generator
     * @return the reference to this object
     */
    public Builder withGeneratedValue(@NotNull String column, @NotNull ValueGenerator<?> valueGenerator) {
      requireNonNull(column, "column must not be null");
      requireNonNull(valueGenerator, "valueGenerator must not be null");
      valueGenerators.put(column, valueGenerator);
      return this;
    }

    /**
     * Specifies headers of the source file.
     * <p>
     * By default the first row of the source file is used as header.
     * </p>
     *
     * @param headers the header names
     * @return the reference to this object
     */
    public Builder withHeader(@NotNull Collection<String> headers) {
      requireNonNull(headers, "headers must not be null");
      this.headers = new String[headers.size()];
      int i = 0;
      for (String header : headers) {
        this.headers[i++] = requireNonNull(header, "headers must not contain null");
      }
      return this;
    }

    /**
     * Specifies headers of the source file.
     * <p>
     * By default the first row of the source file is used as header.
     * </p>
     *
     * @param headers the header names
     * @return the reference to this object
     */
    public Builder withHeader(@NotNull String... headers) {
      requireNonNull(headers, "headers must not be null");
      this.headers = new String[headers.length];
      int i = 0;
      for (String header : headers) {
        this.headers[i++] = requireNonNull(header, "headers must not contain null");
      }
      return this;
    }

    /**
     * Specifies a string that represents {@code null} value.
     * <p>
     * By default {@code ""} (empty string) is used.
     * </p>
     *
     * @param nullString the string that represents {@code null} value
     * @return the reference to this object
     */
    public Builder withNullAs(@NotNull String nullString) {
      this.nullString = requireNonNull(nullString, "nullString must not be null");
      return this;
    }

    /**
     * Specifies a quotation mark of the source file.
     * <p>
     * By default {@code '"'} is used.
     * </p>
     *
     * @param quote the quotation mark
     * @return the reference to this object
     */
    public Builder withQuote(char quote) {
      this.quote = quote;
      return this;
    }

    private String table() {
      if (table == null) {
        String table;
        try {
          Path filename = Paths.get(location.toURI()).getFileName();
          assert filename != null;
          table = filename.toString();
        } catch (URISyntaxException e) {
          throw new DbSetupRuntimeException(e);
        }
        int p = table.lastIndexOf('.');
        return p == -1 ? table : table.substring(0, p);
      } else {
        return table;
      }
    }
  }
}
