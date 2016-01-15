package com.pushtorefresh.storio.contentresolver.operations.put;

import org.junit.Test;

import rx.Observable;
import rx.Single;

public class PreparedPutContentValuesTest {

    @Test
    public void putContentValuesBlocking() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForOneContentValues();

        final PutResult putResult = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues.get(0))
                .withPutResolver(putStub.putResolver)
                .prepare()
                .executeAsBlocking();

        putStub.verifyBehaviorForOneContentValues(putResult);
    }

    @Test
    public void putContentValuesObservable() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForOneContentValues();

        final Observable<PutResult> putResultObservable = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues.get(0))
                .withPutResolver(putStub.putResolver)
                .prepare()
                .createObservable();

        putStub.verifyBehaviorForOneContentValues(putResultObservable);
    }

    @Test
    public void putContentValuesSingle() {
        final PutContentValuesStub putStub = PutContentValuesStub.newPutStubForOneContentValues();

        final Single<PutResult> putResultSingle = putStub.storIOContentResolver
                .put()
                .contentValues(putStub.contentValues.get(0))
                .withPutResolver(putStub.putResolver)
                .prepare()
                .asRxSingle();

        putStub.verifyBehaviorForOneContentValues(putResultSingle);
    }
}
