package com.pushtorefresh.storio2.sqlite.operations.get;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio2.sqlite.Changes;
import com.pushtorefresh.storio2.sqlite.SQLiteTypeMapping;
import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.queries.Query;
import com.pushtorefresh.storio2.sqlite.queries.RawQuery;
import com.pushtorefresh.storio2.test.FlowableBehaviorChecker;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static com.pushtorefresh.storio2.test.Asserts.assertThatListIsImmutable;
import static io.reactivex.BackpressureStrategy.LATEST;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class GetObjectsStub {

    @NonNull
    final StorIOSQLite storIOSQLite;

    @NonNull
    private final StorIOSQLite.LowLevel lowLevel;

    @NonNull
    final Query query;

    @NonNull
    final RawQuery rawQuery;

    @NonNull
    final GetResolver<TestItem> getResolver;

    @NonNull
    private final Cursor cursor;

    @NonNull
    final List<TestItem> items;

    @NonNull
    private final SQLiteTypeMapping<TestItem> typeMapping;

    private final boolean withTypeMapping;

    private GetObjectsStub(boolean withTypeMapping) {
        this.withTypeMapping = withTypeMapping;

        storIOSQLite = mock(StorIOSQLite.class);
        lowLevel = mock(StorIOSQLite.LowLevel.class);

        when(storIOSQLite.lowLevel())
                .thenReturn(lowLevel);

        String table = "test_table";
        String tag = "test_tag";

        query = Query
                .builder()
                .table(table)
                .observesTags(tag)
                .build();

        rawQuery = RawQuery
                .builder()
                .query("select * from who_cares")
                .observesTables(table)
                .observesTags(tag)
                .build();

        //noinspection unchecked
        getResolver = mock(GetResolver.class);
        cursor = mock(Cursor.class);

        items = new ArrayList<TestItem>();
        items.add(new TestItem());
        items.add(new TestItem());
        items.add(new TestItem());

        when(cursor.getCount())
                .thenReturn(items.size());

        when(cursor.moveToNext()).thenAnswer(new Answer<Boolean>() {
            int invocationsCount = 0;

            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return invocationsCount++ < items.size();
            }
        });

        when(storIOSQLite.get())
                .thenReturn(new PreparedGet.Builder(storIOSQLite));

        when(storIOSQLite.observeChanges(any(BackpressureStrategy.class))).thenReturn(Flowable.<Changes>empty());

        assertThat(rawQuery.observesTables()).isNotNull();

        when(getResolver.performGet(storIOSQLite, query))
                .thenReturn(cursor);

        when(getResolver.performGet(storIOSQLite, rawQuery))
                .thenReturn(cursor);

        when(getResolver.mapFromCursor(storIOSQLite, cursor))
                .thenAnswer(new Answer<TestItem>() {
                    int invocationsCount = 0;

                    @Override
                    public TestItem answer(InvocationOnMock invocation) throws Throwable {
                        final TestItem testItem = items.get(invocationsCount);
                        invocationsCount++;
                        return testItem;
                    }
                });

        //noinspection unchecked
        typeMapping = mock(SQLiteTypeMapping.class);

        if (withTypeMapping) {
            when(lowLevel.typeMapping(TestItem.class)).thenReturn(typeMapping);
            when(typeMapping.getResolver()).thenReturn(getResolver);
        }
    }

    @NonNull
    static GetObjectsStub newInstanceWithoutTypeMapping() {
        return new GetObjectsStub(false);
    }

    @NonNull
    static GetObjectsStub newInstanceWithTypeMapping() {
        return new GetObjectsStub(true);
    }

    void verifyQueryBehavior(@NonNull List<TestItem> actualList) {
        // should be called once
        verify(storIOSQLite).get();

        // should be called once
        verify(storIOSQLite).interceptors();

        // should be called only once
        verify(getResolver).performGet(storIOSQLite, query);

        // should be called same number of times as number of items
        verify(getResolver, times(items.size())).mapFromCursor(storIOSQLite, cursor);

        // should be called only once because of Performance!
        verify(cursor).getCount();

        // should be called same number of times as count of items in cursor + 1 (last -> false)
        verify(cursor, times(items.size() + 1)).moveToNext();

        // cursor must be closed!
        verify(cursor, times(1)).close();

        // actual items should be equals to expected
        assertThat(actualList).isEqualTo(items);

        // list should be immutable!
        assertThatListIsImmutable(actualList);

        if (withTypeMapping) {
            // should be called only once because of Performance!
            verify(storIOSQLite).lowLevel();

            // should be called only once because of Performance!
            verify(lowLevel).typeMapping(TestItem.class);

            // should be called only once because of Performance!
            verify(typeMapping).getResolver();
        }

        verifyNoMoreInteractions(storIOSQLite, lowLevel, cursor);
    }

    void verifyQueryBehavior(@NonNull Flowable<List<TestItem>> flowable) {
        new FlowableBehaviorChecker<List<TestItem>>()
                .flowable(flowable)
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<List<TestItem>>() {
                    @Override
                    public void accept(List<TestItem> testItems) {
                        // Get Operation should be subscribed to changes of tables from query
                        verify(storIOSQLite).observeChanges(LATEST);
                        verify(storIOSQLite).defaultRxScheduler();
                        verifyQueryBehavior(testItems);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyQueryBehavior(@NonNull Single<List<TestItem>> single) {
        new FlowableBehaviorChecker<List<TestItem>>()
                .flowable(single.toFlowable())
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<List<TestItem>>() {
                    @Override
                    public void accept(List<TestItem> testItems) {
                        verify(storIOSQLite).defaultRxScheduler();
                        verifyQueryBehavior(testItems);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyRawQueryBehavior(@NonNull List<TestItem> actualList) {
        assertThat(actualList).isNotNull();
        verify(storIOSQLite).get();
        verify(getResolver).performGet(storIOSQLite, rawQuery);
        verify(getResolver, times(items.size())).mapFromCursor(storIOSQLite, cursor);
        verify(cursor).close();
        assertThat(actualList).isEqualTo(items);
        assertThatListIsImmutable(actualList);
    }

    void verifyRawQueryBehavior(@NonNull Flowable<List<TestItem>> flowable) {
        new FlowableBehaviorChecker<List<TestItem>>()
                .flowable(flowable)
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<List<TestItem>>() {
                    @Override
                    public void accept(List<TestItem> testItems) {
                        // Get Operation should be subscribed to changes of tables from query
                        verify(storIOSQLite).observeChanges(LATEST);
                        verifyRawQueryBehavior(testItems);
                    }
                })
                .checkBehaviorOfFlowable();

        assertThat(rawQuery.observesTables()).isNotNull();
    }

    void verifyRawQueryBehavior(@NonNull Single<List<TestItem>> single) {
        new FlowableBehaviorChecker<List<TestItem>>()
                .flowable(single.toFlowable())
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<List<TestItem>>() {
                    @Override
                    public void accept(List<TestItem> testItems) {
                        verifyRawQueryBehavior(testItems);
                    }
                })
                .checkBehaviorOfFlowable();

        assertThat(rawQuery.observesTables()).isNotNull();
    }
}
