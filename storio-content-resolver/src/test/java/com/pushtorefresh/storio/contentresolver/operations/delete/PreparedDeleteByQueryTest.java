package com.pushtorefresh.storio.contentresolver.operations.delete;

import org.junit.Test;

import rx.Observable;
import rx.Single;

public class PreparedDeleteByQueryTest {

    @Test
    public void shouldDeleteByQueryBlocking() {
        final DeleteByQueryStub deleteStub = DeleteByQueryStub.newInstance();

        final DeleteResult deleteResult = deleteStub.storIOContentResolver
                .delete()
                .byQuery(deleteStub.deleteQuery)
                .withDeleteResolver(deleteStub.deleteResolver)
                .prepare()
                .executeAsBlocking();

        deleteStub.verifyBehavior(deleteResult);
    }

    @Test
    public void shouldDeleteByQueryAsObservable() {
        final DeleteByQueryStub deleteStub = DeleteByQueryStub.newInstance();

        final Observable<DeleteResult> deleteResultObservable = deleteStub.storIOContentResolver
                .delete()
                .byQuery(deleteStub.deleteQuery)
                .withDeleteResolver(deleteStub.deleteResolver)
                .prepare()
                .createObservable();

        deleteStub.verifyBehavior(deleteResultObservable);
    }

    @Test
    public void shouldDeleteByQueryAsSingle() {
        final DeleteByQueryStub deleteStub = DeleteByQueryStub.newInstance();

        final Single<DeleteResult> deleteResultSingle = deleteStub.storIOContentResolver
                .delete()
                .byQuery(deleteStub.deleteQuery)
                .withDeleteResolver(deleteStub.deleteResolver)
                .prepare()
                .asRxSingle();

        deleteStub.verifyBehavior(deleteResultSingle);
    }
}
