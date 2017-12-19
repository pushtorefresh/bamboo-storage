package com.pushtorefresh.storio3.contentresolver.operations.delete;

import com.pushtorefresh.storio3.StorIOException;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.contentresolver.operations.SchedulerChecker;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.Collection;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subscribers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PreparedDeleteCollectionOfObjectsTest {

    public static class WithoutTypeMapping {

        @Test
        public void shouldDeleteObjectsWithoutTypeMappingBlocking() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();

            final DeleteResults<TestItem> deleteResults = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .executeAsBlocking();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(deleteResults);
        }

        @Test
        public void shouldDeleteObjectsWithoutTypeMappingAsFlowable() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();

            final Flowable<DeleteResults<TestItem>> flowable = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxFlowable(BackpressureStrategy.MISSING);

            deleteStub.verifyBehaviorForDeleteMultipleObjects(flowable);
        }

        @Test
        public void shouldDeleteObjectsWithoutTypeMappingAsSingle() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();

            final Single<DeleteResults<TestItem>> single = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxSingle();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(single);
        }

        @Test
        public void shouldDeleteObjectsWithoutTypeMappingAsCompletable() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();

            final Completable completable = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare()
                    .asRxCompletable();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(completable);
        }
    }

    public static class WithTypeMapping {

        @Test
        public void shouldDeleteObjectsWithTypeMappingBlocking() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithTypeMapping();

            final DeleteResults<TestItem> deleteResults = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .prepare()
                    .executeAsBlocking();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(deleteResults);
        }

        @Test
        public void shouldDeleteObjectsWithTypeMappingAsFlowable() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithTypeMapping();

            final Flowable<DeleteResults<TestItem>> flowable = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .prepare()
                    .asRxFlowable(BackpressureStrategy.MISSING);

            deleteStub.verifyBehaviorForDeleteMultipleObjects(flowable);
        }

        @Test
        public void shouldDeleteObjectsWithTypeMappingAsSingle() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithTypeMapping();

            final Single<DeleteResults<TestItem>> single = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .prepare()
                    .asRxSingle();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(single);
        }

        @Test
        public void shouldDeleteObjectsWithTypeMappingAsCompletable() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithTypeMapping();

            final Completable completable = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .prepare()
                    .asRxCompletable();

            deleteStub.verifyBehaviorForDeleteMultipleObjects(completable);
        }
    }

    public static class NoTypeMappingError {

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderBlocking() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.LowLevel lowLevel = mock(StorIOContentResolver.LowLevel.class);

            when(storIOContentResolver.lowLevel()).thenReturn(lowLevel);

            when(storIOContentResolver.delete()).thenReturn(new PreparedDelete.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance(), TestItem.newInstance());

            final PreparedDelete<DeleteResults<TestItem>, Collection<TestItem>> preparedDelete = storIOContentResolver
                    .delete()
                    .objects(items)
                    .prepare();

            try {
                preparedDelete.executeAsBlocking();
                failBecauseExceptionWasNotThrown(StorIOException.class);
            } catch (StorIOException expected) {
                // it's okay, no type mapping was found
                assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
            }

            verify(storIOContentResolver).delete();
            verify(storIOContentResolver).lowLevel();
            verify(storIOContentResolver).interceptors();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOContentResolver, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderAsFlowable() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.LowLevel lowLevel = mock(StorIOContentResolver.LowLevel.class);

            when(storIOContentResolver.lowLevel()).thenReturn(lowLevel);

            when(storIOContentResolver.delete()).thenReturn(new PreparedDelete.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance(), TestItem.newInstance());

            final TestSubscriber<DeleteResults<TestItem>> testSubscriber = new TestSubscriber<DeleteResults<TestItem>>();

            storIOContentResolver
                    .delete()
                    .objects(items)
                    .prepare()
                    .asRxFlowable(BackpressureStrategy.MISSING)
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.errors().get(0))
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOContentResolver).delete();
            verify(storIOContentResolver).lowLevel();
            verify(storIOContentResolver).interceptors();
            verify(storIOContentResolver).defaultRxScheduler();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOContentResolver, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderAsSingle() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.LowLevel lowLevel = mock(StorIOContentResolver.LowLevel.class);

            when(storIOContentResolver.lowLevel()).thenReturn(lowLevel);

            when(storIOContentResolver.delete()).thenReturn(new PreparedDelete.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance("test item 1"), TestItem.newInstance("test item 2"));

            final TestObserver<DeleteResults<TestItem>> testObserver = new TestObserver<DeleteResults<TestItem>>();

            storIOContentResolver
                    .delete()
                    .objects(items)
                    .prepare()
                    .asRxSingle()
                    .subscribe(testObserver);

            testObserver.awaitTerminalEvent();

            testObserver.assertNoValues();
            Throwable error = testObserver.errors().get(0);

            assertThat(error)
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessage("Error has occurred during Delete operation. objects = [TestItem{data='test item 1'}, TestItem{data='test item 2'}]");

            assertThat(error.getCause()).hasMessage("One of the objects from the collection does not have type mapping: object = TestItem{data='test item 1'}, object.class = class com.pushtorefresh.storio3.contentresolver.operations.delete.TestItem,ContentProvider was not affected by this operation, please add type mapping for this type");

            verify(storIOContentResolver).delete();
            verify(storIOContentResolver).lowLevel();
            verify(storIOContentResolver).interceptors();
            verify(storIOContentResolver).defaultRxScheduler();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOContentResolver, lowLevel);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderAsCompletable() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.LowLevel lowLevel = mock(StorIOContentResolver.LowLevel.class);

            when(storIOContentResolver.lowLevel()).thenReturn(lowLevel);

            when(storIOContentResolver.delete()).thenReturn(new PreparedDelete.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance("test item 1"), TestItem.newInstance("test item 2"));

            final TestObserver<DeleteResults<TestItem>> testObserver = new TestObserver<DeleteResults<TestItem>>();

            storIOContentResolver
                    .delete()
                    .objects(items)
                    .prepare()
                    .asRxCompletable()
                    .subscribe(testObserver);

            testObserver.awaitTerminalEvent();

            testObserver.assertNoValues();
            Throwable error = testObserver.errors().get(0);

            assertThat(error)
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessage("Error has occurred during Delete operation. objects = [TestItem{data='test item 1'}, TestItem{data='test item 2'}]");

            assertThat(error.getCause()).hasMessage("One of the objects from the collection does not have type mapping: object = TestItem{data='test item 1'}, object.class = class com.pushtorefresh.storio3.contentresolver.operations.delete.TestItem,ContentProvider was not affected by this operation, please add type mapping for this type");

            verify(storIOContentResolver).delete();
            verify(storIOContentResolver).lowLevel();
            verify(storIOContentResolver).interceptors();
            verify(storIOContentResolver).defaultRxScheduler();
            verify(lowLevel).typeMapping(TestItem.class);
            verify(lowLevel, never()).delete(any(DeleteQuery.class));
            verifyNoMoreInteractions(storIOContentResolver, lowLevel);
        }
    }

    public static class OtherTests {

        @Test
        public void shouldReturnItemsInGetData() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();

            final PreparedDeleteCollectionOfObjects<TestItem> operation = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            assertThat(operation.getData()).isEqualTo(deleteStub.items);
        }

        @Test
        public void deleteCollectionOfObjectsFlowableExecutesOnSpecifiedScheduler() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOContentResolver);

            final PreparedDeleteCollectionOfObjects<TestItem> operation = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsFlowable(operation);
        }

        @Test
        public void deleteCollectionOfObjectsSingleExecutesOnSpecifiedScheduler() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOContentResolver);

            final PreparedDeleteCollectionOfObjects<TestItem> operation = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsSingle(operation);
        }

        @Test
        public void deleteCollectionOfObjectsCompletableExecutesOnSpecifiedScheduler() {
            final DeleteObjectsStub deleteStub = DeleteObjectsStub.newInstanceForDeleteMultipleObjectsWithoutTypeMapping();
            final SchedulerChecker schedulerChecker = SchedulerChecker.create(deleteStub.storIOContentResolver);

            final PreparedDeleteCollectionOfObjects<TestItem> operation = deleteStub.storIOContentResolver
                    .delete()
                    .objects(deleteStub.items)
                    .withDeleteResolver(deleteStub.deleteResolver)
                    .prepare();

            schedulerChecker.checkAsCompletable(operation);
        }
    }
}
