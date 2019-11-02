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

import com.ninja_squad.dbsetup.DbSetupRuntimeException;
import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.bind.BinderConfiguration;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * An operation which imports a CSV file into a table.
 * <p>
 * This operation just uses the {@link Insert} operation internal, does not 'upsert'. So almost always you need to use
 * it in combination with {@link com.ninja_squad.dbsetup.operation.Truncate Truncate} or
 * {@link com.ninja_squad.dbsetup.operation.DeleteAll DeleteAll} operations:
 * </p>
 * <pre>
 * <code>
 * &#064;Before
 * void setUp() {
 *     Operation operation = sequenceOf(
 *         truncate("user"),
 *         csv("data/customer.csv").into("customer").build());
 *     DbSetup dbSetup = new DbSetup(destination, operation);
 *     dbSetup.launch();
 * }
 * </code>
 * </pre>
 */
public class Import implements Operation {

    private static final String[] EMPTY_ARRAY = new String[0];

    /**
     * Creates a new builder.
     * <p>
     * The specified location string must be the relative path string from classpath root.
     * </p>
     *
     * @param location a location of source file that is the relative path from classpath root
     * @return a new builder
     */
    public static Builder csv(@NotNull String location) {
        return new Builder(location);
    }

    private final Charset charset;

    private final CSVFormat format;

    private final URL location;

    private final String table;

    private Import(Builder builder) {
        charset = builder.charset;
        format = createFormat(builder);
        location = builder.location;
        table = builder.table;
    }

    @Override
    public void execute(Connection connection, BinderConfiguration configuration) throws SQLException {
        Insert.Builder ib = Operations.insertInto(table);
        try (CSVParser csv = CSVParser.parse(location.openStream(), charset, format)) {
            ib.columns(csv.getHeaderNames().toArray(EMPTY_ARRAY));
            csv.forEach(row -> ib.values(toArray(row)));
        } catch (IOException e) {
            throw new DbSetupRuntimeException("failed to open " + location, e);
        }
        ib.build().execute(connection, configuration);
    }

    private CSVFormat createFormat(Builder builder) {
        CSVFormat format = CSVFormat.DEFAULT.withAllowDuplicateHeaderNames(false)
                .withAllowMissingColumnNames(false)
                .withDelimiter(builder.delimiter)
                .withNullString(builder.nullString)
                .withQuote(builder.quote)
                .withTrim();
        if (builder.headers == null) {
            format = format.withFirstRecordAsHeader();
        } else {
            format = format.withHeader(builder.headers);
        }
        return format;
    }

    private Object[] toArray(CSVRecord row) {
        int length = row.size();
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = row.get(i);
        }
        return values;
    }

    /**
     * A builder to create a {@link Import} instance.
     */
    public static class Builder {

        private final URL location;

        private Charset charset = StandardCharsets.UTF_8;

        private char delimiter = ',';

        private String[] headers;

        private String nullString = "";

        private char quote = '"';

        private String table;

        private Builder(String location) {
            this.location = getClass().getClassLoader().getResource(location);
            if (this.location == null)
                throw new IllegalArgumentException("specified resource was not found: " + location);
        }

        /**
         * Constructs and returns a new {@link Import} instance.
         *
         * @return a new {@link Import} instance
         */
        public Import build() {
            if (table == null) throw new IllegalStateException("'table' has not been specified yet");
            return new Import(this);
        }

        /**
         * Specifies a table to import into.
         *
         * @param table a table name
         * @return the reference to this object
         */
        public Builder into(@NotNull String table) {
            this.table = table;
            return this;
        }

        /**
         * Specifies a charset of source file.
         * <p>
         * By default UTF-8 is used.
         * </p>
         *
         * @param charset a charset
         * @return the reference to this object
         */
        public Builder withCharset(@NotNull Charset charset) {
            this.charset = charset;
            return this;
        }

        /**
         * Specifies a charset of source file.
         * <p>
         * By default UTF-8 is used.
         * </p>
         *
         * @param charset a charset
         * @return the reference to this object
         */
        public Builder withCharset(@NotNull String charset) {
            this.charset = Charset.forName(charset);
            return this;
        }

        /**
         * Specifies a delimiter character of source file.
         * <p>
         * By default {@code ','} is used.
         * </p>
         *
         * @param delimiter a delimiter character
         * @return the reference to this object
         */
        public Builder withDelimiter(char delimiter) {
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Specifies headers of source file.
         * <p>
         * By default the first row is used as header.
         * </p>
         *
         * @param headers headers
         * @return the reference to this object
         */
        public Builder withHeader(@NotNull Collection<String> headers) {
            this.headers = headers.toArray(EMPTY_ARRAY);
            return this;
        }

        /**
         * Specifies headers of source file.
         * <p>
         * By default the first row is used as header.
         * </p>
         *
         * @param headers headers
         * @return the reference to this object
         */
        public Builder withHeader(@NotNull String... headers) {
            this.headers = headers;
            return this;
        }

        /**
         * Specifies a string that represents {@code null} value.
         * <p>
         * By default {@code ""} (empty string) is used.
         * </p>
         *
         * @param nullString a string that represents {@code null} value
         * @return the reference to this object
         */
        public Builder withNullAs(@NotNull String nullString) {
            this.nullString = nullString;
            return this;
        }

        /**
         * Specifies a quotation mark of source file.
         * <p>
         * By default {@code '"'} is used.
         * </p>
         *
         * @param quote a quotation mark
         * @return the reference to this object
         */
        public Builder withQuote(char quote) {
            this.quote = quote;
            return this;
        }
    }
}
