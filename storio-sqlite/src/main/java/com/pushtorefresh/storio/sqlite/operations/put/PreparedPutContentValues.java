package com.pushtorefresh.storio.sqlite.operations.put;

import android.content.ContentValues;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.pushtorefresh.storio.StorIOException;
import com.pushtorefresh.storio.operations.internal.OnSubscribeExecuteAsBlocking;
import com.pushtorefresh.storio.operations.internal.OnSubscribeExecuteAsBlockingSingle;
import com.pushtorefresh.storio.sqlite.Changes;
import com.pushtorefresh.storio.sqlite.StorIOSQLite;

import rx.Observable;
import rx.Single;
import rx.schedulers.Schedulers;

import static com.pushtorefresh.storio.internal.Checks.checkNotNull;
import static com.pushtorefresh.storio.internal.Environment.throwExceptionIfRxJavaIsNotAvailable;

/**
 * Prepared Put Operation for {@link StorIOSQLite}.
 */
public class PreparedPutContentValues extends PreparedPut<PutResult> {

    @NonNull
    private final ContentValues contentValues;

    @NonNull
    private final PutResolver<ContentValues> putResolver;

    PreparedPutContentValues(@NonNull StorIOSQLite storIOSQLite, @NonNull ContentValues contentValues, @NonNull PutResolver<ContentValues> putResolver) {
        super(storIOSQLite);
        this.contentValues = contentValues;
        this.putResolver = putResolver;
    }

    /**
     * Executes Put Operation immediately in current thread.
     * <p>
     * Notice: This is blocking I/O operation that should not be executed on the Main Thread,
     * it can cause ANR (Activity Not Responding dialog), block the UI and drop animations frames.
     * So please, call this method on some background thread. See {@link WorkerThread}.
     *
     * @return non-null result of Put Operation.
     */
    @WorkerThread
    @NonNull
    @Override
    public PutResult executeAsBlocking() {
        try {
            final PutResult putResult = putResolver.performPut(storIOSQLite, contentValues);
            if (putResult.wasInserted() || putResult.wasUpdated()) {
                storIOSQLite.internal().notifyAboutChanges(Changes.newInstance(putResult.affectedTables()));
            }
            return putResult;
        } catch (Exception exception) {
            throw new StorIOException(exception);
        }
    }

    /**
     * Creates {@link Observable} which will perform Put Operation and send result to observer.
     * <p>
     * Returned {@link Observable} will be "Cold Observable", which means that it performs
     * put only after subscribing to it. Also, it emits the result once.
     * <p>
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link Schedulers#io()}.</dd>
     * </dl>
     *
     * @return non-null {@link Observable} which will perform Put Operation.
     * and send result to observer.
     * @deprecated (will be removed in 2.0) please use {@link #asRxObservable()}.
     */
    @NonNull
    @CheckResult
    @Override
    public Observable<PutResult> createObservable() {
        return asRxObservable();
    }

    /**
     * Creates {@link Observable} which will perform Put Operation and send result to observer.
     * <p>
     * Returned {@link Observable} will be "Cold Observable", which means that it performs
     * put only after subscribing to it. Also, it emits the result once.
     * <p>
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link Schedulers#io()}.</dd>
     * </dl>
     *
     * @return non-null {@link Observable} which will perform Put Operation.
     * and send result to observer.
     */
    @NonNull
    @CheckResult
    @Override
    public Observable<PutResult> asRxObservable() {
        throwExceptionIfRxJavaIsNotAvailable("asRxObservable()");

        return Observable
                .create(OnSubscribeExecuteAsBlocking.newInstance(this))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Creates {@link Single} which will perform Put Operation lazily when somebody subscribes to it and send result to observer.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link Schedulers#io()}.</dd>
     * </dl>
     *
     * @return non-null {@link Single} which will perform Put Operation.
     * And send result to observer.
     */
    @NonNull
    @CheckResult
    @Override
    public Single<PutResult> asRxSingle() {
        throwExceptionIfRxJavaIsNotAvailable("asRxSingle()");

        return Single.create(OnSubscribeExecuteAsBlockingSingle.newInstance(this))
                .subscribeOn(Schedulers.io());
    }

    /**
     * Builder for {@link PreparedPutContentValues}.
     */
    public static class Builder {

        @NonNull
        private final StorIOSQLite storIOSQLite;

        @NonNull
        private final ContentValues contentValues;

        Builder(@NonNull StorIOSQLite storIOSQLite, @NonNull ContentValues contentValues) {
            this.storIOSQLite = storIOSQLite;
            this.contentValues = contentValues;
        }

        /**
         * Required: Specifies {@link PutResolver} for Put Operation
         * which allows you to customize behavior of Put Operation.
         *
         * @param putResolver put resolver.
         * @return builder.
         * @see DefaultPutResolver
         */
        @NonNull
        public CompleteBuilder withPutResolver(@NonNull PutResolver<ContentValues> putResolver) {
            checkNotNull(putResolver, "Please specify put resolver");

            return new CompleteBuilder(
                    storIOSQLite,
                    contentValues,
                    putResolver
            );
        }
    }

    /**
     * Compile-time safe part of {@link Builder}.
     */
    public static class CompleteBuilder {

        @NonNull
        private final StorIOSQLite storIOSQLite;

        @NonNull
        private final ContentValues contentValues;

        @NonNull
        private final PutResolver<ContentValues> putResolver;

        CompleteBuilder(@NonNull StorIOSQLite storIOSQLite, @NonNull ContentValues contentValues, @NonNull PutResolver<ContentValues> putResolver) {
            this.storIOSQLite = storIOSQLite;
            this.contentValues = contentValues;
            this.putResolver = putResolver;
        }

        /**
         * Prepares Put Operation.
         *
         * @return {@link PreparedPutContentValues} instance.
         */
        @NonNull
        public PreparedPutContentValues prepare() {
            return new PreparedPutContentValues(
                    storIOSQLite,
                    contentValues,
                    putResolver
            );
        }
    }
}
