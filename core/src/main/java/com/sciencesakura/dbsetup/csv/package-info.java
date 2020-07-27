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
/**
 * A package containing classes to import data from CSV/TSV files.
 * <p>
 * Usage:
 * </p>
 * <pre><code>
 * import static com.sciencesakura.dbsetup.csv.Import.csv;
 *
 * // `testdata.csv` must be in classpath.
 * Operation operation = csv("testdata.csv").into("tablename").build();
 * DbSetup dbSetup = new DbSetup(destination, operation);
 * dbSetup.launch();
 * </code></pre>
 * <p>
 * By default, the source file is treated as an UTF-8-encoded and comma-delimited file. And the
 * first line of the source file is used as a header. Of course you can customize it:
 * </p>
 * <pre><code>
 * // import an ms932-encoded, tab-delimited and no-header file
 * csv("testdata.tsv").into("tablename")
 *     .withCharset("ms932")
 *     .withDelimiter('\t')
 *     .withHeader("column_1", "column_2", "column_3")
 *     .build();
 * </code></pre>
 */
package com.sciencesakura.dbsetup.csv;
