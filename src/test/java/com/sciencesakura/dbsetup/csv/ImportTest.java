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

import com.ninja_squad.dbsetup.Operations;
import com.ninja_squad.dbsetup.bind.BinderConfiguration;
import com.ninja_squad.dbsetup.operation.Insert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.sql.Connection;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Insert.class, Insert.Builder.class, Operations.class})
public class ImportTest {

    @Test
    public void import_csv_default_style() throws SQLException {
        Connection connection = mock(Connection.class);
        BinderConfiguration configuration = mock(BinderConfiguration.class);
        Insert insert = mock(Insert.class);
        doNothing().when(insert).execute(any(), any());
        Insert.Builder ib = mock(Insert.Builder.class);
        when(ib.build()).thenReturn(insert);
        mockStatic(Operations.class);
        when(Operations.insertInto(anyString())).thenReturn(ib);

        Import importCsv = Import.csv("data/default.csv")
                .into("table_name")
                .build();
        importCsv.execute(connection, configuration);

        verifyStatic(Operations.class);
        Operations.insertInto("table_name");
        verify(ib).columns("a", "b", "c");
        verify(ib).values("a1", "b1", "c1");
        verify(ib).values("a2", "b2", "c2");
        verify(ib).values("a3", null, null);
        verify(ib).build();
        verify(insert).execute(connection, configuration);
    }

    @Test
    public void import_tsv_pgsql_style() throws SQLException {
        Connection connection = mock(Connection.class);
        BinderConfiguration configuration = mock(BinderConfiguration.class);
        Insert insert = mock(Insert.class);
        doNothing().when(insert).execute(any(), any());
        Insert.Builder ib = mock(Insert.Builder.class);
        when(ib.build()).thenReturn(insert);
        mockStatic(Operations.class);
        when(Operations.insertInto(anyString())).thenReturn(ib);

        Import importCsv = Import.csv("data/pgsql.tsv")
                .into("table_name")
                .withHeader("a", "b", "c")
                .withDelimiter('\t')
                .withNullAs("\\N")
                .build();
        importCsv.execute(connection, configuration);

        verifyStatic(Operations.class);
        Operations.insertInto("table_name");
        verify(ib).columns("a", "b", "c");
        verify(ib).values("a1", "b1", "c1");
        verify(ib).values("a2", "b2", "c2");
        verify(ib).values("a3", null, null);
        verify(ib).build();
        verify(insert).execute(connection, configuration);
    }
}
