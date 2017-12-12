package com.pushtorefresh.storio3.contentresolver.operations.put;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.pushtorefresh.storio3.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio3.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio3.test.FlowableBehaviorChecker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

// stub class to avoid violation of DRY in tests
class PutObjectsStub {

    @NonNull
    final StorIOContentResolver storIOContentResolver;

    @NonNull
    private final StorIOContentResolver.LowLevel lowLevel;

    @NonNull
    final List<TestItem> items;

    @NonNull
    final PutResolver<TestItem> putResolver;

    @NonNull
    private final ContentResolverTypeMapping<TestItem> typeMapping;

    @NonNull
    private final Map<TestItem, PutResult> itemsToPutResultsMap;

    private final boolean withTypeMapping;

    @SuppressWarnings("unchecked")
    private PutObjectsStub(boolean withTypeMapping, int numberOfTestItems) {
        this.withTypeMapping = withTypeMapping;

        storIOContentResolver = mock(StorIOContentResolver.class);
        lowLevel = mock(StorIOContentResolver.LowLevel.class);

        when(storIOContentResolver.lowLevel())
                .thenReturn(lowLevel);

        when(storIOContentResolver.put())
                .thenReturn(new PreparedPut.Builder(storIOContentResolver));

        items = new ArrayList<TestItem>(numberOfTestItems);

        for (int i = 0; i < numberOfTestItems; i++) {
            final TestItem testItem = TestItem.newInstance();
            items.add(testItem);
        }

        putResolver = (PutResolver<TestItem>) mock(PutResolver.class);
        itemsToPutResultsMap = new HashMap<TestItem, PutResult>(numberOfTestItems);

        for (final TestItem testItem : items) {
            final PutResult putResult = PutResult.newInsertResult(mock(Uri.class), mock(Uri.class));
            itemsToPutResultsMap.put(testItem, putResult);

            when(putResolver.performPut(storIOContentResolver, testItem))
                    .thenReturn(putResult);
        }

        typeMapping = mock(ContentResolverTypeMapping.class);

        if (withTypeMapping) {
            when(lowLevel.typeMapping(TestItem.class)).thenReturn(typeMapping);
            when(typeMapping.putResolver()).thenReturn(putResolver);
        }
    }

    @NonNull
    public static PutObjectsStub newPutStubForOneObjectWithoutTypeMapping() {
        return new PutObjectsStub(false, 1);
    }

    @NonNull
    public static PutObjectsStub newPutStubForOneObjectWithTypeMapping() {
        return new PutObjectsStub(true, 1);
    }

    @NonNull
    public static PutObjectsStub newPutStubForMultipleObjectsWithoutTypeMapping() {
        return new PutObjectsStub(false, 3);
    }

    @NonNull
    public static PutObjectsStub newPutStubForMultipleObjectsWithTypeMapping() {
        return new PutObjectsStub(true, 3);
    }

    void verifyBehaviorForMultipleObjects(@NonNull PutResults<TestItem> putResults) {
        if (items.size() > 1 || withTypeMapping) {
            // should be called only once because of Performance!
            verify(storIOContentResolver).lowLevel();
        } else {
            verify(storIOContentResolver, never()).lowLevel();
        }

        // should be called only once
        verify(storIOContentResolver).put();

        // should be called only once
        verify(storIOContentResolver).interceptors();

        // number of calls to putResolver's performPut() should be equal to number of objects
        verify(putResolver, times(items.size())).performPut(eq(storIOContentResolver), any(TestItem.class));

        for (final TestItem testItem : items) {
            // checks that put was performed for each item
            verify(putResolver, times(1)).performPut(storIOContentResolver, testItem);

            final PutResult expectedPutResult = itemsToPutResultsMap.get(testItem);
            assertThat(putResults.results().get(testItem)).isEqualTo(expectedPutResult);
        }

        assertThat(putResults.results()).hasSize(itemsToPutResultsMap.size());

        if (withTypeMapping) {
            verify(lowLevel, times(items.size())).typeMapping(TestItem.class);
            verify(typeMapping, times(items.size())).putResolver();
        }

        verifyNoMoreInteractions(storIOContentResolver, lowLevel, typeMapping, putResolver);
    }

    void verifyBehaviorForMultipleObjects(@NonNull Flowable<PutResults<TestItem>> putResultsFlowable) {
        new FlowableBehaviorChecker<PutResults<TestItem>>()
                .flowable(putResultsFlowable)
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<PutResults<TestItem>>() {
                    @Override
                    public void accept(@NonNull PutResults<TestItem> putResults) throws Exception {
                        verify(storIOContentResolver).defaultRxScheduler();
                        verifyBehaviorForMultipleObjects(putResults);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyBehaviorForMultipleObjects(@NonNull Single<PutResults<TestItem>> putResultsSingle) {
        new FlowableBehaviorChecker<PutResults<TestItem>>()
                .flowable(putResultsSingle.toFlowable())
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<PutResults<TestItem>>() {
                    @Override
                    public void accept(@NonNull PutResults<TestItem> putResults) throws Exception {
                        verify(storIOContentResolver).defaultRxScheduler();
                        verifyBehaviorForMultipleObjects(putResults);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyBehaviorForMultipleObjects(@NonNull Completable completable) {
        verifyBehaviorForMultipleObjects(completable.<PutResults<TestItem>>toFlowable());
    }

    void verifyBehaviorForOneObject(@NonNull PutResult putResult) {
        Map<TestItem, PutResult> putResultsMap = new HashMap<TestItem, PutResult>(1);
        putResultsMap.put(items.get(0), putResult);
        verifyBehaviorForMultipleObjects(PutResults.newInstance(putResultsMap));
    }

    void verifyBehaviorForOneObject(@NonNull Flowable<PutResult> putResultFlowable) {
        new FlowableBehaviorChecker<PutResult>()
                .flowable(putResultFlowable)
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<PutResult>() {
                    @Override
                    public void accept(@NonNull PutResult putResult) throws Exception {
                        verify(storIOContentResolver).defaultRxScheduler();
                        verifyBehaviorForOneObject(putResult);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyBehaviorForOneObject(@NonNull Single<PutResult> putResultSingle) {
        new FlowableBehaviorChecker<PutResult>()
                .flowable(putResultSingle.toFlowable())
                .expectedNumberOfEmissions(1)
                .testAction(new Consumer<PutResult>() {
                    @Override
                    public void accept(@NonNull PutResult putResult) throws Exception {
                        verify(storIOContentResolver).defaultRxScheduler();
                        verifyBehaviorForOneObject(putResult);
                    }
                })
                .checkBehaviorOfFlowable();
    }

    void verifyBehaviorForOneObject(@NonNull Completable completable) {
        verifyBehaviorForOneObject(completable.<PutResult>toFlowable());
    }
}
