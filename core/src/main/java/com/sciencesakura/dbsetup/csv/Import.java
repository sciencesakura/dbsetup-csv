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
import com.ninja_squad.dbsetup.bind.BinderConfiguration;
import com.ninja_squad.dbsetup.generator.ValueGenerator;
import com.ninja_squad.dbsetup.operation.Insert;
import com.ninja_squad.dbsetup.operation.Operation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An operation which imports the CSV file into the specified table.
 *
 * @author sciencesakura
 */
public class Import implements Operation {

    private static final String[] EMPTY_ARRAY = {};

    /**
     * Creates a new {@code Import.CSV} instance.
     * <p>
     * The specified location string must be the relative path string from classpath root.
     * </p>
     *
     * @param location the location of the source file that is the relative path from classpath root
     * @return the new {@code Import.CSV} instance
     * @throws IllegalArgumentException if the source file was not found
     */
    @NotNull
    public static CSV csv(@NotNull String location) {
        return new CSV(location);
    }

    private static CSVFormat createFormat(Builder builder) {
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

    private static Object[] toArray(CSVRecord row) {
        int length = row.size();
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = row.get(i);
        }
        return values;
    }

    private final Operation internalOperation;

    private Import(Builder builder) {
        CSVFormat format = createFormat(builder);
        Insert.Builder ib = Insert.into(builder.table);
        try (CSVParser csv = CSVParser.parse(builder.csv.location.openStream(), builder.charset, format)) {
            ib.columns(csv.getHeaderNames().toArray(EMPTY_ARRAY));
            builder.defaultValues.forEach(ib::withDefaultValue);
            builder.valueGenerators.forEach(ib::withGeneratedValue);
            csv.forEach(row -> ib.values(toArray(row)));
        } catch (IOException e) {
            throw new DbSetupRuntimeException("failed to open " + builder.csv.location, e);
        }
        internalOperation = ib.build();
    }

    @Override
    public void execute(Connection connection, BinderConfiguration configuration) throws SQLException {
        internalOperation.execute(connection, configuration);
    }

    /**
     * A representation of the CSV file.
     *
     * @author sciencesakura
     */
    public static class CSV implements Serializable {

        private static final long serialVersionUID = -411937921273901442L;

        private final URL location;

        private CSV(String location) {
            requireNonNull(location, "location must not be null");
            this.location = getClass().getClassLoader().getResource(location);
            if (this.location == null)
                throw new IllegalArgumentException(location + " not found");
        }

        /**
         * Create a new {@code Import.Builder} instance.
         *
         * @param table the table name
         * @return the new {@code Import.Builder} instance
         */
        @NotNull
        public Builder into(@NotNull String table) {
            return new Builder(this, requireNonNull(table, "table must not be null"));
        }
    }

    /**
     * A builder to create the {@code Import} instance.
     * <p>
     * This builder can be used only once. Once it has built {@code Import} instance, builder's
     * methods will throw an {@code IllegalStateException}.
     * </p>
     *
     * @author sciencesakura
     */
    public static class Builder {

        private final Map<String, Object> defaultValues = new LinkedHashMap<>();

        private final Map<String, ValueGenerator<?>> valueGenerators = new LinkedHashMap<>();

        private final CSV csv;

        private final String table;

        private Charset charset = StandardCharsets.UTF_8;

        private char delimiter = ',';

        private String[] headers;

        private String nullString = "";

        private char quote = '"';

        private boolean built;

        private Builder(CSV csv, String table) {
            this.csv = csv;
            this.table = table;
        }

        /**
         * Build a new {@code Import} instance.
         *
         * @return the new {@code Import} instance
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Import build() {
            requireNotBuilt();
            built = true;
            return new Import(this);
        }

        /**
         * Specifies a charset of the source file.
         * <p>
         * By default UTF-8 is used.
         * </p>
         *
         * @param charset the charset
         * @return the reference to this object
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withCharset(@NotNull Charset charset) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withCharset(@NotNull String charset) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already.
         */
        @NotNull
        public Builder withDefaultValue(@NotNull String column, Object value) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withDelimiter(char delimiter) {
            requireNotBuilt();
            this.delimiter = delimiter;
            return this;
        }

        /**
         * Specifies a value generator for the given column.
         *
         * @param column         the column name
         * @param valueGenerator the generator
         * @return the reference to this object
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withGeneratedValue(@NotNull String column, @NotNull ValueGenerator<?> valueGenerator) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withHeader(@NotNull Collection<@NotNull String> headers) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withHeader(@NotNull String... headers) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withNullAs(@NotNull String nullString) {
            requireNotBuilt();
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
         * @throws IllegalStateException if this builder has built an {@code Import} already
         */
        @NotNull
        public Builder withQuote(char quote) {
            requireNotBuilt();
            this.quote = quote;
            return this;
        }

        private void requireNotBuilt() {
            if (built) throw new IllegalStateException("this operation has been built already");
        }
    }
}
