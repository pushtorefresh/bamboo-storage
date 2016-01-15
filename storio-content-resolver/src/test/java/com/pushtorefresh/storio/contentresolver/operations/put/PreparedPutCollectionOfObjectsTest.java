package com.pushtorefresh.storio.contentresolver.operations.put;

import android.content.ContentValues;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.InsertQuery;
import com.pushtorefresh.storio.contentresolver.queries.UpdateQuery;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PreparedPutCollectionOfObjectsTest {

    public static class WithoutTypeMapping {

        @Test
        public void shouldPutObjectsWithoutTypeMappingBlocking() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithoutTypeMapping();

            final PutResults<TestItem> putResults = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .withPutResolver(putStub.putResolver)
                    .prepare()
                    .executeAsBlocking();

            putStub.verifyBehaviorForMultipleObjects(putResults);
        }

        @Test
        public void shouldPutObjectsWithoutTypeMappingAsObservable() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithoutTypeMapping();

            final Observable<PutResults<TestItem>> observable = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .withPutResolver(putStub.putResolver)
                    .prepare()
                    .createObservable();

            putStub.verifyBehaviorForMultipleObjects(observable);
        }

        @Test
        public void shouldPutObjectsWithoutTypeMappingAsSingle() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithoutTypeMapping();

            final Single<PutResults<TestItem>> single = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .withPutResolver(putStub.putResolver)
                    .prepare()
                    .asRxSingle();

            putStub.verifyBehaviorForMultipleObjects(single);
        }
    }

    public static class WithTypeMapping {

        @Test
        public void shouldPutObjectsWithTypeMappingBlocking() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithTypeMapping();

            final PutResults<TestItem> putResults = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .prepare()
                    .executeAsBlocking();

            putStub.verifyBehaviorForMultipleObjects(putResults);
        }

        @Test
        public void shouldPutObjectsWithTypeMappingAsObservable() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithTypeMapping();

            final Observable<PutResults<TestItem>> observable = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .prepare()
                    .createObservable();

            putStub.verifyBehaviorForMultipleObjects(observable);
        }

        @Test
        public void shouldPutObjectsWithTypeMappingAsSingle() {
            final PutObjectsStub putStub = PutObjectsStub.newPutStubForMultipleObjectsWithTypeMapping();

            final Single<PutResults<TestItem>> single = putStub.storIOContentResolver
                    .put()
                    .objects(putStub.items)
                    .prepare()
                    .asRxSingle();

            putStub.verifyBehaviorForMultipleObjects(single);
        }
    }

    public static class NoTypeMappingError {

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderBlocking() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.put()).thenReturn(new PreparedPut.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance(), TestItem.newInstance());

            final PreparedPut<PutResults<TestItem>> preparedPut = storIOContentResolver
                    .put()
                    .objects(items)
                    .prepare();

            try {
                preparedPut.executeAsBlocking();
                failBecauseExceptionWasNotThrown(StorIOException.class);
            } catch (StorIOException expected) {
                // it's okay, no type mapping was found
                assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
            }

            verify(storIOContentResolver).put();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).insert(any(InsertQuery.class), any(ContentValues.class));
            verify(internal, never()).update(any(UpdateQuery.class), any(ContentValues.class));
            verifyNoMoreInteractions(storIOContentResolver, internal);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderAsObservable() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.put()).thenReturn(new PreparedPut.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance(), TestItem.newInstance());

            final TestSubscriber<PutResults<TestItem>> testSubscriber = new TestSubscriber<PutResults<TestItem>>();

            storIOContentResolver
                    .put()
                    .objects(items)
                    .prepare()
                    .createObservable()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0))
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOContentResolver).put();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).insert(any(InsertQuery.class), any(ContentValues.class));
            verify(internal, never()).update(any(UpdateQuery.class), any(ContentValues.class));
            verifyNoMoreInteractions(storIOContentResolver, internal);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAffectingContentProviderAsSingle() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.put()).thenReturn(new PreparedPut.Builder(storIOContentResolver));

            final List<TestItem> items = asList(TestItem.newInstance(), TestItem.newInstance());

            final TestSubscriber<PutResults<TestItem>> testSubscriber = new TestSubscriber<PutResults<TestItem>>();

            storIOContentResolver
                    .put()
                    .objects(items)
                    .prepare()
                    .asRxSingle()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0))
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOContentResolver).put();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).insert(any(InsertQuery.class), any(ContentValues.class));
            verify(internal, never()).update(any(UpdateQuery.class), any(ContentValues.class));
            verifyNoMoreInteractions(storIOContentResolver, internal);
        }
    }
}
