package com.pushtorefresh.storio2.sqlite.operations.delete;

import com.pushtorefresh.storio2.StorIOException;
import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.operations.SchedulerChecker;
import com.pushtorefresh.storio2.sqlite.queries.DeleteQuery;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import rx.Completable;
import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PreparedDeleteObjectTest {

    public static class WithoutTypeMapping {

        @Test
        public void shouldDeleteObjectWithoutTypeMappingBlocking() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();

            final DeleteResult deleteResult = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .executeAsBlocking();

            deleteStub.verifyBehaviorForOneObject(deleteResult);
        }

        @Test
        public void shouldDeleteObjectWithoutTypeMappingAsObservable() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();

            final Observable<DeleteResult> observable = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxObservable();

            deleteStub.verifyBehaviorForOneObject(observable);
        }

        @Test
        public void shouldDeleteObjectWithoutTypeMappingAsSingle() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();

            final Single<DeleteResult> single = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxSingle();

            deleteStub.verifyBehaviorForOneObject(single);
        }

        @Test
        public void shouldDeleteObjectWithoutTypeMappingAsCompletable() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();

            final Completable completable = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxCompletable();

            deleteStub.verifyBehaviorForOneObject(completable);
        }

        @Test
        public void shouldNotNotifyIfWasNotDeleted() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMappingNothingDeleted();

            final DeleteResult deleteResult = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .executeAsBlocking();

            deleteStub.verifyBehaviorForOneObject(deleteResult);
        }
    }

    public static class WithTypeMapping {

        @Test
        public void shouldDeleteObjectWithTypeMappingBlocking() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithTypeMapping();

            final DeleteResult deleteResult = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .prepare()
                    .executeAsBlocking();

            deleteStub.verifyBehaviorForOneObject(deleteResult);
        }

        @Test
        public void shouldDeleteObjectWithTypeMappingAsObservable() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithTypeMapping();

            final Observable<DeleteResult> observable = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .prepare()
                    .asRxObservable();

            deleteStub.verifyBehaviorForOneObject(observable);
        }

        @Test
        public void shouldDeleteObjectWithTypeMappingAsSingle() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithTypeMapping();

            final Single<DeleteResult> single = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .prepare()
                    .asRxSingle();

            deleteStub.verifyBehaviorForOneObject(single);
        }

        @Test
        public void shouldDeleteObjectWithTypeMappingAsCompletable() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithTypeMapping();

            final Completable completable = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .prepare()
                    .asRxCompletable();

            deleteStub.verifyBehaviorForOneObject(completable);
        }
    }

    public static class NoTypeMappingError {

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingDbBlocking() {
            final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
            final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

            when(storIOSQLite.lowLevel()).thenReturn(lowLevel);

            when(storIOSQLite.delete()).thenReturn(new PreparedDelete.Builder(storIOSQLite));

            final PreparedDelete<DeleteResult, TestItem> preparedDelete = storIOSQLite
                    .delete()
                    .object(TestItem.newInstance())
                    .prepare();

            try {
                preparedDelete.executeAsBlocking();
                failBecauseExceptionWasNotThrown(StorIOException.class);
            } catch (StorIOException expected) {
                // it's okay, no type mapping was found
                assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
            }

            verify(storIOSQLite).delete();
            verify(storIOSQLite).lowLevel();
            verify(storIOSQLite).interceptors();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOSQLite, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingDbAsObservable() {
            final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
            final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

            when(storIOSQLite.lowLevel()).thenReturn(lowLevel);

            when(storIOSQLite.delete()).thenReturn(new PreparedDelete.Builder(storIOSQLite));

            final TestSubscriber<DeleteResult> testSubscriber = new TestSubscriber<DeleteResult>();

            storIOSQLite
                    .delete()
                    .object(TestItem.newInstance())
                    .prepare()
                    .asRxObservable()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0)).
                    hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOSQLite).delete();
            verify(storIOSQLite).lowLevel();
            verify(storIOSQLite).defaultScheduler();
            verify(storIOSQLite).interceptors();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOSQLite, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingDbAsSingle() {
            final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
            final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

            when(storIOSQLite.lowLevel()).thenReturn(lowLevel);

            when(storIOSQLite.delete()).thenReturn(new PreparedDelete.Builder(storIOSQLite));

            final TestSubscriber<DeleteResult> testSubscriber = new TestSubscriber<DeleteResult>();

            storIOSQLite
                    .delete()
                    .object(TestItem.newInstance())
                    .prepare()
                    .asRxSingle()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0)).
                    hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOSQLite).delete();
            verify(storIOSQLite).lowLevel();
            verify(storIOSQLite).defaultScheduler();
            verify(storIOSQLite).interceptors();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOSQLite, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingDbAsCompletable() {
            final StorIOSQLite storIOSQLite = mock(StorIOSQLite.class);
            final StorIOSQLite.LowLevel lowLevel = mock(StorIOSQLite.LowLevel.class);

            when(storIOSQLite.lowLevel()).thenReturn(lowLevel);

            when(storIOSQLite.delete()).thenReturn(new PreparedDelete.Builder(storIOSQLite));

            final TestSubscriber<DeleteResult> testSubscriber = new TestSubscriber<DeleteResult>();

            storIOSQLite
                    .delete()
                    .object(TestItem.newInstance())
                    .prepare()
                    .asRxCompletable()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0)).
                    hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOSQLite).delete();
            verify(storIOSQLite).lowLevel();
            verify(storIOSQLite).defaultScheduler();
            verify(storIOSQLite).interceptors();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOSQLite, lowLevel);
        }
    }

    public static class OtherTests {

        @Test
        public void shouldReturnObjectInGetData() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();

            final PreparedDeleteObject<TestItem> operation = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            assertThat(operation.getData()).isEqualTo(deleteStub.itemsRequestedForDelete.get(0));
        }

        @Test
        public void deleteObjectObservableExecutesOnSpecifiedScheduler() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOSQLite);

            final PreparedDeleteObject<TestItem> operation = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsObservable(operation);
        }

        @Test
        public void deleteObjectSingleExecutesOnSpecifiedScheduler() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOSQLite);

            final PreparedDeleteObject<TestItem> operation = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsSingle(operation);
        }

        @Test
        public void deleteObjectCompletableExecutesOnSpecifiedScheduler() {
            final DeleteStub deleteStub = DeleteStub.newStubForOneObjectWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOSQLite);

            final PreparedDeleteObject<TestItem> operation = deleteStub.storIOSQLite
                    .delete()
                    .object(deleteStub.itemsRequestedForDelete.get(0))
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsCompletable(operation);
        }
    }
}
