package com.pushtorefresh.storio2.sqlite.operations.get;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.queries.Query;
import com.pushtorefresh.storio2.sqlite.queries.RawQuery;

import org.junit.Test;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DefaultGetResolverTest {

    @Test
    public void rawQuery() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
        final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

        final RawQuery rawQuery = RawQuery.builder()
                .query("test sql")
                .build();

        final Cursor expectedCursor = mock(Cursor.class);

        when(storIOSQLite.lowLevel())
                .thenReturn(lowLevel);

        when(lowLevel.rawQuery(rawQuery))
                .thenReturn(expectedCursor);

        final DefaultGetResolver<TestItem> defaultGetResolver = new DefaultGetResolver<TestItem>() {
            @NonNull
            @Override
            public TestItem mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
                return mock(TestItem.class);
            }
        };

        final Cursor actualCursor = defaultGetResolver.performGet(storIOSQLite, rawQuery);

        // only one request should occur
        verify(lowLevel, times(1)).rawQuery(any(RawQuery.class));

        // and this request should be equals to original
        verify(lowLevel, times(1)).rawQuery(rawQuery);

        assertThat(actualCursor).isSameAs(expectedCursor);
    }

    @Test
    public void query() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
        final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

        final Query query = Query.builder()
                .table("test_table")
                .build();

        final Cursor expectedCursor = mock(Cursor.class);

        when(storIOSQLite.lowLevel())
                .thenReturn(lowLevel);

        when(lowLevel.query(query))
                .thenReturn(expectedCursor);

        final DefaultGetResolver<TestItem> defaultGetResolver = new DefaultGetResolver<TestItem>() {
            @NonNull
            @Override
            public TestItem mapFromCursor(@NonNull StorIOSQLite storIOSQLite, @NonNull Cursor cursor) {
                return mock(TestItem.class);
            }
        };

        final Cursor actualCursor = defaultGetResolver.performGet(storIOSQLite, query);

        // only one request should occur
        verify(lowLevel, times(1)).query(any(Query.class));

        // and this request should be equals to original
        verify(lowLevel, times(1)).query(query);

        assertThat(actualCursor).isSameAs(expectedCursor);
    }

    private static class TestItem {

    }
}
