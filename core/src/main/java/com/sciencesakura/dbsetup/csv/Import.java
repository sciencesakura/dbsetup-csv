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
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

/**
 * An operation which imports the CSV file into the specified table.
 * <p>
 * Usage:
 * </p>
 * <pre><code>
 * import static com.sciencesakura.dbsetup.csv.Import.csv;
 *
 * // `testdata.csv` must be in classpath.
 * Operation operation = csv("testdata.csv").build();
 * DbSetup dbSetup = new DbSetup(destination, operation);
 * dbSetup.launch();
 * </code></pre>
 *
 * @author sciencesakura
 */
public final class Import implements Operation {

    private static final String[] EMPTY_ARRAY = {};

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
        return new Builder(location);
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
        return fb.build();
    }

    private static Object[] toArray(CSVRecord row) {
        int length = row.size();
        String[] values = new String[length];
        for (int i = 0; i < length; i++) {
            values[i] = row.get(i);
        }
        return values;
    }

    private final Builder builder;
    private Operation internalOperation;

    private Import(Builder builder) {
        this.builder = builder;
    }

    @Override
    public void execute(Connection connection, BinderConfiguration configuration) throws SQLException {
        if (internalOperation == null) {
            CSVFormat format = createFormat(builder);
            Insert.Builder ib = Insert.into(builder.table());
            try (CSVParser csv = CSVParser.parse(builder.location.openStream(), builder.charset, format)) {
                ib.columns(csv.getHeaderNames().toArray(EMPTY_ARRAY));
                builder.defaultValues.forEach(ib::withDefaultValue);
                builder.valueGenerators.forEach(ib::withGeneratedValue);
                csv.forEach(row -> ib.values(toArray(row)));
            } catch (IOException e) {
                throw new DbSetupRuntimeException("failed to open " + builder.location, e);
            }
            internalOperation = ib.build();
        }
        internalOperation.execute(connection, configuration);
    }

    /**
     * A builder to create the {@code Import} instance.
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

        private Builder(String location) {
            requireNonNull(location, "location must not be null");
            URL urlLocation = getClass().getClassLoader().getResource(location);
            if (urlLocation == null) {
                throw new IllegalArgumentException(location + " not found");
            }
            this.location = urlLocation;
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            return withCharset(Charset.forName(charset));
        }

        /**
         * Specifies a default value for the given column.
         *
         * @param column the column name
         * @param value  the default value
         * @return the reference to this object
         */
        public Builder withDefaultValue(@NotNull String column, Object value) {
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
        public Builder withHeader(@NotNull Collection<@NotNull String> headers) {
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
            if (built) {
                throw new IllegalStateException("already built");
            }
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
