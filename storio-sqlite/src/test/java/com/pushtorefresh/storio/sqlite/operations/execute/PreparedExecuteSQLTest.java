package com.pushtorefresh.storio.sqlite.operations.execute;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.sqlite.Changes;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;
import com.pushtorefresh.storio.sqlite.operations.SchedulerChecker;
import com.pushtorefresh.storio.sqlite.queries.RawQuery;
import com.pushtorefresh.storio.test.ObservableBehaviorChecker;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import rx.Observable;
import rx.Single;
import rx.functions.Action1;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class PreparedExecuteSQLTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void shouldReturnRawQueryInGetData() {
        final Stub stub = Stub.newInstanceDoNotApplyAffectedTablesAndTags();

        final PreparedExecuteSQL operation =  stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare();

        assertThat(operation.getData()).isEqualTo(stub.rawQuery);
    }

    @Test
    public void executeSQLBlockingWithoutNotifications() {
        final Stub stub = Stub.newInstanceDoNotApplyAffectedTablesAndTags();

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .executeAsBlocking();

        verify(stub.storIOSQLite, never()).defaultScheduler();
        stub.verifyBehavior();
    }

    @Test
    public void executeSQLBlockingWithEmptyAffectedTablesAndTags() {
        final Stub stub = Stub.newInstanceApplyEmptyAffectedTablesAndTags();

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .executeAsBlocking();

        verify(stub.storIOSQLite, never()).defaultScheduler();
        stub.verifyBehavior();
    }

    @Test
    public void executeSQLBlockingWithNotification() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .executeAsBlocking();

        verify(stub.storIOSQLite, never()).defaultScheduler();
        stub.verifyBehavior();
    }

    @Test
    public void executeSQLObservableWithoutNotifications() {
        final Stub stub = Stub.newInstanceDoNotApplyAffectedTablesAndTags();

        final Observable<Object> observable = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxObservable();

        verify(stub.storIOSQLite).defaultScheduler();
        stub.verifyBehavior(observable);
    }

    @Test
    public void executeSQLSingleWithNotification() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        final Single<Object> single = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxSingle();

        verify(stub.storIOSQLite).defaultScheduler();
        stub.verifyBehavior(single);
    }

    @Test
    public void executeSQLSingleWithoutNotifications() {
        final Stub stub = Stub.newInstanceDoNotApplyAffectedTablesAndTags();

        final Single<Object> single = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxSingle();

        verify(stub.storIOSQLite).defaultScheduler();
        stub.verifyBehavior(single);
    }

    @Test
    public void executeSQLObservableWithNotification() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        final Observable<Object> observable = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxObservable();

        verify(stub.storIOSQLite).defaultScheduler();
        stub.verifyBehavior(observable);
    }

    @Test
    public void executeSQLObservableExecutesOnSpecifiedScheduler() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(stub.storIOSQLite);

        final PreparedExecuteSQL operation = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare();

        schedulerChecker.checkAsObservable(operation);
    }

    @Test
    public void executeSQLSingleExecutesOnSpecifiedScheduler() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(stub.storIOSQLite);

        final PreparedExecuteSQL operation = stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare();

        schedulerChecker.checkAsSingle(operation);
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionBlocking() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        IllegalStateException testException = new IllegalStateException("test exception");
        doThrow(testException).when(stub.lowLevel).executeSQL(stub.rawQuery);

        expectedException.expect(StorIOException.class);
        expectedException.expectMessage("Error has occurred during ExecuteSQL operation. query = RawQuery{query='DROP TABLE users!', args=[], affectsTables=[test_table1, test_table2], affectsTags=[test_tag1, test_tag2], observesTables=[], observesTags=[]}");
        expectedException.expectCause(equalTo(testException));

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .executeAsBlocking();

        verifyNoMoreInteractions(stub.storIOSQLite, stub.lowLevel);
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionObservable() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        IllegalStateException testException = new IllegalStateException("test exception");
        doThrow(testException).when(stub.lowLevel).executeSQL(stub.rawQuery);

        final TestSubscriber<Object> testSubscriber = new TestSubscriber<Object>();

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxObservable()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoValues();
        testSubscriber.assertError(StorIOException.class);

        //noinspection ThrowableResultOfMethodCallIgnored
        StorIOException expected = (StorIOException) testSubscriber.getOnErrorEvents().get(0);

        IllegalStateException cause = (IllegalStateException) expected.getCause();
        assertThat(cause).hasMessage("test exception");

        verify(stub.storIOSQLite).executeSQL();
        verify(stub.storIOSQLite).defaultScheduler();
        verify(stub.storIOSQLite).lowLevel();
        verify(stub.lowLevel).executeSQL(stub.rawQuery);
        verify(stub.storIOSQLite).interceptors();
        verifyNoMoreInteractions(stub.storIOSQLite, stub.lowLevel);
    }

    @Test
    public void shouldWrapExceptionIntoStorIOExceptionSingle() {
        final Stub stub = Stub.newInstanceApplyNotEmptyAffectedTablesAndTags();

        IllegalStateException testException = new IllegalStateException("test exception");
        doThrow(testException).when(stub.lowLevel).executeSQL(stub.rawQuery);

        final TestSubscriber<Object> testSubscriber = new TestSubscriber<Object>();

        stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare()
                .asRxSingle()
                .subscribe(testSubscriber);

        testSubscriber.awaitTerminalEvent();
        testSubscriber.assertNoValues();
        testSubscriber.assertError(StorIOException.class);

        //noinspection ThrowableResultOfMethodCallIgnored
        StorIOException expected = (StorIOException) testSubscriber.getOnErrorEvents().get(0);

        IllegalStateException cause = (IllegalStateException) expected.getCause();
        assertThat(cause).hasMessage("test exception");

        verify(stub.storIOSQLite).executeSQL();
        verify(stub.storIOSQLite).defaultScheduler();
        verify(stub.storIOSQLite).lowLevel();
        verify(stub.lowLevel).executeSQL(stub.rawQuery);
        verify(stub.storIOSQLite).interceptors();
        verifyNoMoreInteractions(stub.storIOSQLite, stub.lowLevel);
    }

    @Test
    public void createObservableReturnsAsRxObservable() {
        final Stub stub = Stub.newInstanceDoNotApplyAffectedTablesAndTags();

        PreparedExecuteSQL preparedExecuteSQL = spy(stub.storIOSQLite
                .executeSQL()
                .withQuery(stub.rawQuery)
                .prepare());

        Observable<Object> observable = Observable.just(new Object());
        //noinspection CheckResult
        doReturn(observable).when(preparedExecuteSQL).asRxObservable();

        //noinspection deprecation
        assertThat(preparedExecuteSQL.createObservable()).isEqualTo(observable);

        //noinspection CheckResult
        verify(preparedExecuteSQL).asRxObservable();
    }

    static class Stub {

        private final StorIOSQLite storIOSQLite;
        private final StorIOSQLite.LowLevel lowLevel;
        private final RawQuery rawQuery;
        private final boolean applyAffectedTablesAndTags;

        @NonNull
        private static final String[] DEFAULT_AFFECTED_TABLES = {"test_table1", "test_table2"};

        @NonNull
        private static final List<String> DEFAULT_AFFECTED_TAGS = asList("test_tag1", "test_tag2");

        @NonNull
        private final String[] affectedTables;

        @NonNull
        private final List<String> affectedTags;

        @NonNull
        public static Stub newInstanceDoNotApplyAffectedTablesAndTags() {
            return new Stub(false, DEFAULT_AFFECTED_TABLES, DEFAULT_AFFECTED_TAGS);
        }

        @NonNull
        public static Stub newInstanceApplyEmptyAffectedTablesAndTags() {
            return new Stub(false, new String[0], Collections.<String>emptyList());
        }

        @NonNull
        public static Stub newInstanceApplyNotEmptyAffectedTablesAndTags() {
            return new Stub(true, DEFAULT_AFFECTED_TABLES, DEFAULT_AFFECTED_TAGS);
        }

        private Stub(
                boolean applyAffectedTablesAndTags,
                @NonNull String[] affectedTables,
                @NonNull List<String> affectedTags
        ) {
            this.applyAffectedTablesAndTags = applyAffectedTablesAndTags;
            this.affectedTables = affectedTables;
            this.affectedTags = affectedTags;

            storIOSQLite = mock(StorIOSQLite.class);
            lowLevel = mock(StorIOSQLite.LowLevel.class);

            if (applyAffectedTablesAndTags) {
                rawQuery = RawQuery.builder()
                        .query("DROP TABLE users!")
                        .affectsTables(affectedTables)
                        .affectsTags(affectedTags)
                        .build();
            } else {
                rawQuery = RawQuery.builder()
                        .query("DROP TABLE users!")
                        .build();
            }

            when(storIOSQLite.lowLevel()).thenReturn(lowLevel);

            when(storIOSQLite.executeSQL())
                    .thenReturn(new PreparedExecuteSQL.Builder(storIOSQLite));
        }

        @SuppressWarnings("unchecked")
        void verifyBehavior() {
            // storIOSQLite.executeSQL() should be called once
            verify(storIOSQLite).executeSQL();

            // storIOSQLite.lowLevel.executeSQL() should be called once for ANY RawQuery
            verify(lowLevel).executeSQL(any(RawQuery.class));

            // storIOSQLite.lowLevel.executeSQL() should be called once for required RawQuery
            verify(lowLevel).executeSQL(rawQuery);

            if (applyAffectedTablesAndTags && (affectedTables.length > 0 || !affectedTags.isEmpty())) {
                final Changes changes = Changes.newInstance(
                        new HashSet<String>(asList(affectedTables)),
                        new HashSet<String>(affectedTags)
                );
                verify(lowLevel).notifyAboutChanges(changes);
            } else {
                verify(lowLevel, never()).notifyAboutChanges(any(Changes.class));
            }
        }

        void verifyBehavior(@NonNull Observable<Object> observable) {
            new ObservableBehaviorChecker<Object>()
                    .observable(observable)
                    .expectedNumberOfEmissions(1)
                    .testAction(new Action1<Object>() {
                        @Override
                        public void call(Object anObject) {
                            verifyBehavior();
                        }
                    })
                    .checkBehaviorOfObservable();
        }

        void verifyBehavior(@NonNull Single<Object> single) {
            new ObservableBehaviorChecker<Object>()
                    .observable(single.toObservable())
                    .expectedNumberOfEmissions(1)
                    .testAction(new Action1<Object>() {
                        @Override
                        public void call(Object anObject) {
                            verifyBehavior();
                        }
                    })
                    .checkBehaviorOfObservable();
        }
    }
}
