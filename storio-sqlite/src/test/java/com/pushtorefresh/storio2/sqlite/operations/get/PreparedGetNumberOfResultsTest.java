package com.pushtorefresh.storio2.sqlite.operations.get;

import android.database.Cursor;

import com.pushtorefresh.storio2.StorIOException;
import com.pushtorefresh.storio2.sqlite.Changes;
import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.operations.SchedulerChecker;
import com.pushtorefresh.storio2.sqlite.queries.Query;

import org.junit.Test;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;

import static io.reactivex.BackpressureStrategy.LATEST;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PreparedGetNumberOfResultsTest {

    @Test
    public void shouldReturnQueryInGetData() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final PreparedGetNumberOfResults operation = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare();

        assertThat(operation.getData()).isEqualTo(getStub.query);
    }

    @Test
    public void shouldReturnRawQueryInGetData() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final PreparedGetNumberOfResults operation = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare();

        assertThat(operation.getData()).isEqualTo(getStub.rawQuery);
    }

    @Test
    public void shouldGetNumberOfResultsWithQueryBlocking() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Integer numberOfResults = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .executeAsBlocking();

        getStub.verifyQueryBehaviorForInteger(numberOfResults);
    }

    @Test
    public void shouldGetNumberOfResultsWithQueryAsFlowable() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Flowable<Integer> numberOfResultsFlowable = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .asRxFlowable(LATEST)
                .take(1);

        getStub.verifyQueryBehaviorForInteger(numberOfResultsFlowable);
    }

    @Test
    public void shouldGetNumberOfResultsWithQueryAsSingle() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Single<Integer> numberOfResultsSingle = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .asRxSingle();

        getStub.verifyQueryBehaviorForInteger(numberOfResultsSingle);
    }

    @Test
    public void shouldGetNumberOfResultsWithRawQueryBlocking() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Integer numberOfResults = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .executeAsBlocking();

        getStub.verifyRawQueryBehaviorForInteger(numberOfResults);
    }

    @Test
    public void shouldGetNumberOfResultsWithRawQueryAsFlowable() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Flowable<Integer> numberOfResultsFlowable = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .asRxFlowable(LATEST)
                .take(1);

        getStub.verifyRawQueryBehaviorForInteger(numberOfResultsFlowable);
    }

    @Test
    public void shouldGetNumberOfResultsWithRawQueryAsSingle() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();

        final Single<Integer> numberOfResultsSingle = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .asRxSingle();

        getStub.verifyRawQueryBehaviorForInteger(numberOfResultsSingle);
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionForBlocking() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        //noinspection unchecked
        final GetResolver<Integer> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        try {
            new PreparedGetNumberOfResults.Builder(storIOSQLite)
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
    public void shouldWrapExceptionIntoStorIOExceptionForFlowable() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        when(storIOSQLite.observeChanges(any(BackpressureStrategy.class))).thenReturn(Flowable.<Changes>empty());

        //noinspection unchecked
        final GetResolver<Integer> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        final TestSubscriber<Integer> testSubscriber = new TestSubscriber<Integer>();

        new PreparedGetNumberOfResults.Builder(storIOSQLite)
                .withQuery(Query.builder().table("test_table").observesTags("test_tag").build())
                .withGetResolver(getResolver)
                .prepare()
                .asRxFlowable(LATEST)
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent(60, SECONDS);
        testSubscriber.assertError(StorIOException.class);

        assertThat(testSubscriber.errorCount()).isEqualTo(1);
        StorIOException storIOException = (StorIOException) testSubscriber.errors().get(0);
        IllegalStateException cause = (IllegalStateException) storIOException.getCause();
        assertThat(cause).hasMessage("test exception");

        testSubscriber.dispose();
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionForSingle() {
        final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);

        //noinspection unchecked
        final GetResolver<Integer> getResolver = mock(GetResolver.class);

        when(getResolver.performGet(eq(storIOSQLite), any(Query.class)))
                .thenThrow(new IllegalStateException("test exception"));

        final TestObserver<Integer> testObserver = new TestObserver<Integer>();

        new PreparedGetNumberOfResults.Builder(storIOSQLite)
                .withQuery(Query.builder().table("test_table").build())
                .withGetResolver(getResolver)
                .prepare()
                .asRxSingle()
                .subscribe(testObserver);

        testObserver.awaitTerminalEvent(60, SECONDS);
        testObserver.assertError(StorIOException.class);

        assertThat(testObserver.errorCount()).isEqualTo(1);
        StorIOException storIOException = (StorIOException) testObserver.errors().get(0);
        IllegalStateException cause = (IllegalStateException) storIOException.getCause();
        assertThat(cause).hasMessage("test exception");
    }

    @Test
    public void completeBuilderShouldThrowExceptionIfNoQueryWasSet() {
        PreparedGetNumberOfResults.CompleteBuilder completeBuilder = new PreparedGetNumberOfResults.Builder(mock(StorIOSQLite.class))
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
        PreparedGetNumberOfResults preparedGetNumberOfResults
                = new PreparedGetNumberOfResults(mock(StorIOSQLite.class), (Query) null, (GetResolver<Integer>) mock(GetResolver.class));

        try {
            preparedGetNumberOfResults.executeAsBlocking();
            failBecauseExceptionWasNotThrown(StorIOException.class);
        } catch (StorIOException expected) {
            IllegalStateException cause = (IllegalStateException) expected.getCause();
            assertThat(cause).hasMessage("Please specify query");
        }
    }

    @Test
    public void asRxFlowableShouldThrowExceptionIfNoQueryWasSet() {
        //noinspection unchecked,ConstantConditions
        PreparedGetNumberOfResults preparedGetNumberOfResults
                = new PreparedGetNumberOfResults(mock(StorIOSQLite.class), (Query) null, (GetResolver<Integer>) mock(GetResolver.class));

        try {
            //noinspection CheckResult
            preparedGetNumberOfResults.asRxFlowable(LATEST);
            failBecauseExceptionWasNotThrown(StorIOException.class);
        } catch (IllegalStateException expected) {
            assertThat(expected).hasMessage("Please specify query");
        }
    }

    @Test
    public void verifyThatStandardGetResolverJustReturnsCursorGetCount() {
        final GetCursorStub getStub = GetCursorStub.newInstance();
        final GetResolver<Integer> standardGetResolver
                = PreparedGetNumberOfResults.CompleteBuilder.STANDARD_GET_RESOLVER;

        final Cursor cursor = mock(Cursor.class);

        when(cursor.getCount()).thenReturn(12314);

        assertThat(standardGetResolver.mapFromCursor(getStub.storIOSQLite, cursor)).isEqualTo(12314);
    }

    @Test
    public void getNumberOfResultsFlowableExecutesOnSpecifiedScheduler() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(getStub.storIOSQLite);

        final PreparedGetNumberOfResults operation = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare();

        schedulerChecker.checkAsFlowable(operation);
    }

    @Test
    public void getNumberOfResultsSingleExecutesOnSpecifiedScheduler() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(getStub.storIOSQLite);

        final PreparedGetNumberOfResults operation = getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare();

        schedulerChecker.checkAsSingle(operation);
    }

    @Test
    public void shouldPassStorIOSQLiteToResolverOnQuery() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();
        getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.query)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .executeAsBlocking();

        verify(getStub.getResolverForNumberOfResults).mapFromCursor(eq(getStub.storIOSQLite), any(Cursor.class));
    }

    @Test
    public void shouldPassStorIOSQLiteToResolverOnRawQuery() {
        final GetNumberOfResultsStub getStub = GetNumberOfResultsStub.newInstance();
        getStub.storIOSQLite
                .get()
                .numberOfResults()
                .withQuery(getStub.rawQuery)
                .withGetResolver(getStub.getResolverForNumberOfResults)
                .prepare()
                .executeAsBlocking();

        verify(getStub.getResolverForNumberOfResults).mapFromCursor(eq(getStub.storIOSQLite), any(Cursor.class));
    }
}
