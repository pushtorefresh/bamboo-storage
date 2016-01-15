package com.pushtorefresh.storio.contentresolver.operations.get;

import android.database.Cursor;
import android.net.Uri;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.contentresolver.Changes;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.List;

import rx.Observable;
import rx.Single;
import rx.observers.TestSubscriber;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(Enclosed.class)
public class PreparedGetListOfObjectsTest {

    public static class WithoutTypeMapping {

        @Test
        public void shouldGetListOfObjectsWithoutTypeMappingBlocking() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithoutTypeMapping();

            final List<TestItem> testItems = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .withGetResolver(getStub.getResolver)
                    .prepare()
                    .executeAsBlocking();

            getStub.verifyBehavior(testItems);
        }

        @Test
        public void shouldGetListOfObjectsWithoutTypeMappingAsObservable() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithoutTypeMapping();

            final Observable<List<TestItem>> testItemsObservable = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .withGetResolver(getStub.getResolver)
                    .prepare()
                    .createObservable()
                    .take(1);

            getStub.verifyBehavior(testItemsObservable);
        }

        @Test
        public void shouldGetListOfObjectsWithoutTypeMappingAsSingle() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithoutTypeMapping();

            final Single<List<TestItem>> testItemsSingle = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .withGetResolver(getStub.getResolver)
                    .prepare()
                    .asRxSingle();

            getStub.verifyBehavior(testItemsSingle);
        }
    }

    public static class WithTypeMapping {

        @Test
        public void shouldGetListOfObjectsWithTypeMappingBlocking() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithTypeMapping();

            final List<TestItem> testItems = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .prepare()
                    .executeAsBlocking();

            getStub.verifyBehavior(testItems);
        }

        @Test
        public void shouldGetListOfObjectsWithTypeMappingAsObservable() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithTypeMapping();

            final Observable<List<TestItem>> testItemsObservable = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .prepare()
                    .createObservable()
                    .take(1);

            getStub.verifyBehavior(testItemsObservable);
        }

        @Test
        public void shouldGetListOfObjectsWithTypeMappingAsSingle() {
            final GetObjectsStub getStub = GetObjectsStub.newStubWithTypeMapping();

            final Single<List<TestItem>> testItemsSingle = getStub.storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(getStub.query)
                    .prepare()
                    .asRxSingle();

            getStub.verifyBehavior(testItemsSingle);
        }
    }

    public static class NoTypeMappingError {

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAccessingContentProviderBlocking() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.get()).thenReturn(new PreparedGet.Builder(storIOContentResolver));

            final PreparedGet<List<TestItem>> preparedGet = storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(Query.builder().uri(mock(Uri.class)).build())
                    .prepare();

            try {
                preparedGet.executeAsBlocking();
                failBecauseExceptionWasNotThrown(StorIOException.class);
            } catch (StorIOException expected) {
                // it's okay, no type mapping was found
                assertThat(expected).hasCauseInstanceOf(IllegalStateException.class);
            }

            verify(storIOContentResolver).get();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).query(any(Query.class));
            verifyNoMoreInteractions(storIOContentResolver, internal);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAccessingContentProviderAsObservable() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.get()).thenReturn(new PreparedGet.Builder(storIOContentResolver));

            when(storIOContentResolver.observeChangesOfUri(any(Uri.class)))
                    .thenReturn(Observable.<Changes>empty());

            final TestSubscriber<List<TestItem>> testSubscriber = new TestSubscriber<List<TestItem>>();

            storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(Query.builder().uri(mock(Uri.class)).build())
                    .prepare()
                    .createObservable()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0))
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class);

            verify(storIOContentResolver).get();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).query(any(Query.class));
            verify(storIOContentResolver).observeChangesOfUri(any(Uri.class));

            verifyNoMoreInteractions(storIOContentResolver, internal);
        }

        @Test
        public void shouldThrowExceptionIfNoTypeMappingWasFoundWithoutAccessingContentProviderAsSingle() {
            final StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);
            final StorIOContentResolver.Internal internal = mock(StorIOContentResolver.Internal.class);

            when(storIOContentResolver.internal()).thenReturn(internal);

            when(storIOContentResolver.get()).thenReturn(new PreparedGet.Builder(storIOContentResolver));

            final TestSubscriber<List<TestItem>> testSubscriber = new TestSubscriber<List<TestItem>>();

            storIOContentResolver
                    .get()
                    .listOfObjects(TestItem.class)
                    .withQuery(Query.builder().uri(mock(Uri.class)).build())
                    .prepare()
                    .asRxSingle()
                    .subscribe(testSubscriber);

            testSubscriber.awaitTerminalEvent();
            testSubscriber.assertNoValues();
            assertThat(testSubscriber.getOnErrorEvents().get(0))
                    .isInstanceOf(StorIOException.class)
                    .hasCauseInstanceOf(IllegalStateException.class)
                    .hasMessage(IllegalStateException.class.getName() + ": This type does not have type mapping: type = " + TestItem.class + ",ContentProvider was not touched by this operation, please add type mapping for this type");

            verify(storIOContentResolver).get();
            verify(storIOContentResolver).internal();
            verify(internal).typeMapping(TestItem.class);
            verify(internal, never()).query(any(Query.class));

            verifyNoMoreInteractions(storIOContentResolver, internal);
        }
    }

    // With Enclosed runner we can not have tests in root class
    public static class OtherTests {

        @Test
        public void shouldCloseCursorInCaseOfException() {
            StorIOContentResolver storIOContentResolver = mock(StorIOContentResolver.class);

            Query query = Query.builder()
                    .uri(mock(Uri.class))
                    .build();

            //noinspection unchecked
            GetResolver<Object> getResolver = mock(GetResolver.class);

            Cursor cursor = mock(Cursor.class);

            when(getResolver.performGet(storIOContentResolver, query))
                    .thenReturn(cursor);

            when(getResolver.mapFromCursor(cursor))
                    .thenThrow(new IllegalStateException("Breaking execution"));

            when(cursor.getCount()).thenReturn(1);

            when(cursor.moveToNext()).thenReturn(true);

            try {
                new PreparedGetListOfObjects.Builder<Object>(storIOContentResolver, Object.class)
                        .withQuery(query)
                        .withGetResolver(getResolver)
                        .prepare()
                        .executeAsBlocking();

                failBecauseExceptionWasNotThrown(StorIOException.class);
            } catch (StorIOException expected) {
                assertThat(expected.getCause())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Breaking execution");

                // Main check: in case of exception cursor must be closed
                verify(cursor).close();

                verify(cursor).getCount();
                verify(cursor).moveToNext();

                verifyNoMoreInteractions(storIOContentResolver, cursor);
            }
        }
    }
}
