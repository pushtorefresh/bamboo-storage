package com.pushtorefresh.storio2.contentresolver.operations.put;

import android.content.ContentValues;

import com.pushtorefresh.storio2.contentresolver.operations.SchedulerChecker;

import org.junit.Test;

import rx.Completable;
import rx.Observable;
import rx.Single;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class PreparedPutContentValuesIterableTest {

    @Test
    public void shouldReturnContentValuesInGetData() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();

        final PreparedPutContentValuesIterable operation = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare();

        assertThat(operation.getData()).isEqualTo(putStub.contentValues);
    }

    @Test
    public void putContentValuesIterableBlocking() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();

        final PutResults<ContentValues> putResults = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare()
                .executeAsBlocking();

        putStub.verifyBehaviorForMultipleContentValues(putResults);
    }

    @Test
    public void putContentValuesIterableObservable() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();

        final Observable<PutResults<ContentValues>> putResultsObservable = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare()
                .asRxObservable();

        putStub.verifyBehaviorForMultipleContentValues(putResultsObservable);
    }

    @Test
    public void putContentValuesIterableSingle() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();

        final Single<PutResults<ContentValues>> putResultsSingle = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare()
                .asRxSingle();

        putStub.verifyBehaviorForMultipleContentValues(putResultsSingle);
    }

    @Test
    public void putContentValuesIterableCompletable() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();

        final Completable completable = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare()
                .asRxCompletable();

        putStub.verifyBehaviorForMultipleContentValues(completable);
    }

    @Test
    public void putContentValuesIterableObservableExecutesOnSpecifiedScheduler() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(putStub.storIOContentResolver);

        final PreparedPutContentValuesIterable operation = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare();

        schedulerChecker.checkAsObservable(operation);
    }

    @Test
    public void putContentValuesIterableSingleExecutesOnSpecifiedScheduler() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(putStub.storIOContentResolver);

        final PreparedPutContentValuesIterable operation = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare();

        schedulerChecker.checkAsSingle(operation);
    }

    @Test
    public void putContentValuesIterableCompletableExecutesOnSpecifiedScheduler() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForMultipleContentValues();
        final SchedulerChecker schedulerChecker = SchedulerChecker.create(putStub.storIOContentResolver);

        final PreparedPutContentValuesIterable operation = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues)
                .withPutResolver(putStub.putResolver)
                .prepare();

        schedulerChecker.checkAsCompletable(operation);
    }
}
