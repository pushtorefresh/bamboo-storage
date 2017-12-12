package com.pushtorefresh.storio3.sqlite.operations.execute;

import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import com.pushtorefresh.storio3.StorIOException;
import com.pushtorefresh.storio3.operations.PreparedOperation;
import com.pushtorefresh.storio3.sqlite.Changes;
import com.pushtorefresh.storio3.Interceptor;
import com.pushtorefresh.storio3.sqlite.StorIOSQLite;
import com.pushtorefresh.storio3.sqlite.operations.internal.RxJavaUtils;
import com.pushtorefresh.storio3.sqlite.queries.RawQuery;

import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;

import static com.pushtorefresh.storio3.internal.Checks.checkNotNull;
import static com.pushtorefresh.storio3.impl.ChainImpl.buildChain;

/**
 * Prepared Execute SQL Operation for {@link StorIOSQLite}.
 */
public class PreparedExecuteSQL implements PreparedOperation<Object, Object, RawQuery> {

    @NonNull
    private final StorIOSQLite storIOSQLite;

    @NonNull
    private final RawQuery rawQuery;

    PreparedExecuteSQL(@NonNull StorIOSQLite storIOSQLite, @NonNull RawQuery rawQuery) {
        this.storIOSQLite = storIOSQLite;
        this.rawQuery = rawQuery;
    }

    /**
     * Executes SQL Operation immediately in current thread.
     * <p>
     * Notice: This is blocking I/O operation that should not be executed on the Main Thread,
     * it can cause ANR (Activity Not Responding dialog), block the UI and drop animations frames.
     * So please, call this method on some background thread. See {@link WorkerThread}.
     *
     * @return just a new instance of {@link Object}, actually Execute SQL should return {@code void},
     * but we can not return instance of {@link Void} so we just return {@link Object}
     * and you don't have to deal with {@code null}.
     */
    @WorkerThread
    @NonNull
    @Override
    public Object executeAsBlocking() {
        return buildChain(storIOSQLite.interceptors(), new RealCallInterceptor())
                .proceed(this);
    }

    /**
     * Creates {@link Flowable} which will perform Execute SQL Operation
     * and send result to observer.
     * <p>
     * Returned {@link Flowable} will be "Cold Flowable", which means that it performs
     * execution of SQL only after subscribing to it. Also, it emits the result once.
     * <p>
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link StorIOSQLite#defaultRxScheduler()} if not {@code null}.</dd>
     * </dl>
     *
     * @return non-null {@link Flowable} which will perform Delete Operation
     * and send result to observer. Result: just a new instance of {@link Object},
     * actually Execute SQL should return {@code void},
     * but we can not return instance of {@link Void} so we just return {@link Object}
     * and you don't have to deal with {@code null}.
     */
    @NonNull
    @CheckResult
    @Override
    public Flowable<Object> asRxFlowable(@NonNull BackpressureStrategy backpressureStrategy) {
        return RxJavaUtils.createFlowable(storIOSQLite, this, backpressureStrategy);
    }

    /**
     * Creates {@link Single} which will perform Execute SQL Operation lazily when somebody subscribes to it and send result to observer.
     * <dl>
     * <dt><b>Scheduler:</b></dt>
     * <dd>Operates on {@link StorIOSQLite#defaultRxScheduler()} if not {@code null}.</dd>
     * </dl>
     *
     * @return non-null {@link Single} which will perform Execute SQL Operation.
     * And send result to observer.
     */
    @NonNull
    @CheckResult
    @Override
    public Single<Object> asRxSingle() {
        return RxJavaUtils.createSingle(storIOSQLite, this);
    }

    private class RealCallInterceptor implements Interceptor {
        @NonNull
        @Override
        public <Result, WrappedResult, Data> Result intercept(@NonNull PreparedOperation<Result, WrappedResult, Data> operation, @NonNull Chain chain) {
            try {
                final StorIOSQLite.LowLevel lowLevel = storIOSQLite.lowLevel();
                lowLevel.executeSQL(rawQuery);

                final Set<String> affectedTables = rawQuery.affectsTables();
                final Set<String> affectedTags = rawQuery.affectsTags();

                if (!affectedTables.isEmpty() || !affectedTags.isEmpty()) {
                    lowLevel.notifyAboutChanges(Changes.newInstance(affectedTables, affectedTags));
                }

                //noinspection unchecked
                return (Result) new Object();
            } catch (Exception exception) {
                throw new StorIOException("Error has occurred during ExecuteSQL operation. query = " + rawQuery, exception);
            }
        }
    }

    @NonNull
    @Override
    public RawQuery getData() {
        return rawQuery;
    }

    /**
     * Builder for {@link PreparedExecuteSQL}.
     */
    public static class Builder {

        @NonNull
        private final StorIOSQLite storIOSQLite;

        public Builder(@NonNull StorIOSQLite storIOSQLite) {
            this.storIOSQLite = storIOSQLite;
        }

        /**
         * Required: Specifies query for ExecSql Operation.
         *
         * @param rawQuery any SQL query that you want to execute, but please, be careful with it.
         *                 Don't forget that you can set affected tables to the {@link RawQuery},
         *                 so ExecSQL operation will send notification about changes in that tables.
         * @return builder.
         */
        @NonNull
        public CompleteBuilder withQuery(@NonNull RawQuery rawQuery) {
            checkNotNull(rawQuery, "Please set query object");
            return new CompleteBuilder(storIOSQLite, rawQuery);
        }
    }

    /**
     * Compile-time safe part of {@link Builder}.
     */
    public static class CompleteBuilder {

        @NonNull
        private final StorIOSQLite storIOSQLite;

        @NonNull
        private final RawQuery rawQuery;

        CompleteBuilder(@NonNull StorIOSQLite storIOSQLite, @NonNull RawQuery rawQuery) {
            this.storIOSQLite = storIOSQLite;
            this.rawQuery = rawQuery;
        }

        /**
         * Prepares ExecSql Operation.
         *
         * @return {@link PreparedExecuteSQL} instance.
         */
        @NonNull
        public PreparedExecuteSQL prepare() {
            return new PreparedExecuteSQL(
                    storIOSQLite,
                    rawQuery
            );
        }
    }
}
