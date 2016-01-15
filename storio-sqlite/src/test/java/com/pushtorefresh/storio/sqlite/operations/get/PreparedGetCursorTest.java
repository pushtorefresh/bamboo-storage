package com.pushtorefresh.storio.sqlite.operations.get;

import android.database.Cursor;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.sqlite.Changes;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.queries.Query;

import org.junit.Test;

import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

import static java.util.Collections.singleton;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreparedGetCursorTest {

    @Test
    public void shouldGetCursorWithQueryBlocking() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Cursor cursor = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .executeAsBlocking();

        getStub.verifyQueryBehaviorForCursor(cursor);
    }

    @Test
    public void shouldGetCursorWithQueryAsObservable() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Observable<Cursor> cursorObservable = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .createObservable()
                .take(1);

        getStub.verifyQueryBehaviorForCursor(cursorObservable);
    }

    @Test
    public void shouldGetCursorWithQueryAsSingle() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Single<Cursor> cursorSingle = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .asRxSingle();

        getStub.verifyQueryBehaviorForCursor(cursorSingle);
    }

    @Test
    public void shouldGetCursorWithRawQueryBlocking() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Cursor cursor = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .executeAsBlocking();

        getStub.verifyRawQueryBehaviorForCursor(cursor);
    }

    @Test
    public void shouldGetCursorWithRawQueryAsObservable() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Observable<Cursor> cursorObservable = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .createObservable()
                .take(1);

        getStub.verifyRawQueryBehaviorForCursor(cursorObservable);
    }

    @Test
    public void shouldGetCursorWithRawQueryAsSingle() {
        final GetCursorStub getStub = GetCursorStub.newInstance();

        final Single<Cursor> cursorSingle = getStub.storIOSQLite
                .get()
                .cursor()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForCursor)
                .prepare()
                .asRxSingle();

        getStub.verifyRawQueryBehaviorForCursor(cursorSingle);
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionForBlocking() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        //noinspection unchecked
        final GetResolver<Cursor> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        try {
            new PreparedGetCursor.Builder(storIOSQLite)
                    .withQuery(Query.builder().table("test_table").build())
                    .withGetResolver(getResolver)
                    .prepare()
                    .executeAsBlocking();

            failBecauseExceptionWasNotThrown(StorIOException.class);
        } catch (StorIOException expected) {
            IllegalStateException cause = (IllegalStateException) expected.getCause();
            assertThat(cause).hasMessage("test exception");
        }
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionForObservable() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        when(storIOSQLite.observeChangesInTables(eq(singleton("test_table"))))
                .thenReturn(Observable.<Changes>empty());

        //noinspection unchecked
        final GetResolver<Cursor> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        final TestSubscriber<Cursor> testSubscriber = new TestSubscriber<Cursor>();

        new PreparedGetCursor.Builder(storIOSQLite)
                .withQuery(Query.builder().table("test_table").build())
                .withGetResolver(getResolver)
                .prepare()
                .createObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent(60, SECONDS);
        testSubscriber.assertError(StorIOException.class);

        StorIOException storIOException = (StorIOException) testSubscriber.getOnErrorEvents().get(0);
        IllegalStateException cause = (IllegalStateException) storIOException.getCause();
        assertThat(cause).hasMessage("test exception");

        testSubscriber.unsubscribe();
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionForSingle() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        //noinspection unchecked
        final GetResolver<Cursor> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        final TestSubscriber<Cursor> testSubscriber = new TestSubscriber<Cursor>();

        new PreparedGetCursor.Builder(storIOSQLite)
                .withQuery(Query.builder().table("test_table").build())
                .withGetResolver(getResolver)
                .prepare()
                .asRxSingle()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent(60, SECONDS);
        testSubscriber.assertError(StorIOException.class);

        StorIOException storIOException = (StorIOException) testSubscriber.getOnErrorEvents().get(0);
        IllegalStateException cause = (IllegalStateException) storIOException.getCause();
        assertThat(cause).hasMessage("test exception");
    }

    @Test
    public void completeBuilderShouldThrowExceptionIfNoQueryWasSet() {
        PreparedGetCursor.CompleteBuilder completeBuilder = new PreparedGetCursor.Builder(mock(StorIOSQLite.class))
                .withQuery(Query.builder().table("test_table").build()); // We will null it later

        completeBuilder.query = null;

        try {
            completeBuilder.prepare();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Please specify query");
        }
    }

    @Test
    public void executeAsBlockingShouldThrowExceptionIfNoQueryWasSet() {
        //noinspection unchecked,ConstantConditions
        PreparedGetCursor preparedGetCursor
                = new PreparedGetCursor(mock(StorIOSQLite.class), (Query) null, (GetResolver<Cursor>) mock(GetResolver.class));

        try {
            preparedGetCursor.executeAsBlocking();
            failBecauseExceptionWasNotThrown(StorIOException.class);
        } catch (StorIOException expected) {
            IllegalStateException cause = (IllegalStateException) expected.getCause();
            assertThat(cause).hasMessage("Please specify query");
        }
    }

    @Test
    public void createObservableShouldThrowExceptionIfNoQueryWasSet() {
        //noinspection unchecked,ConstantConditions
        PreparedGetCursor preparedGetCursor
                = new PreparedGetCursor(mock(StorIOSQLite.class), (Query) null, (GetResolver<Cursor>) mock(GetResolver.class));

        try {
            //noinspection ResourceType
            preparedGetCursor.createObservable();
            failBecauseExceptionWasNotThrown(StorIOException.class);
        } catch (StorIOException expected) {
            assertThat(expected).hasMessage("Please specify query");
        }
    }

    @Test
    public void verifyThatStandardGetResolverDoesNotModifyCursor() {
        final GetResolver<Cursor> standardGetResolver
                = PreparedGetCursor.CompleteBuilder.STANDARD_GET_RESOLVER;

        final Cursor cursor = mock(Cursor.class);

        assertThat(standardGetResolver.mapFromCursor(cursor)).isSameAs(cursor);
    }
}
