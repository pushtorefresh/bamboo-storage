package com.pushtorefresh.storio2.operations.internal;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio2.operations.PreparedOperation;

import rx.functions.Func1;

/**
 * Required to avoid problems with ClassLoader when RxJava is not in ClassPath
 * We can not use anonymous classes from RxJava directly in StorIO, ClassLoader won't be happy :(
 * <p>
 * For internal usage only!
 */
public final class MapSomethingToExecuteAsBlocking<Something, Result, Data> implements Func1<Something, Result> {

    @NonNull
    private final PreparedOperation<Result, Data> preparedOperation;

    private MapSomethingToExecuteAsBlocking(@NonNull PreparedOperation<Result, Data> preparedOperation) {
        this.preparedOperation = preparedOperation;
    }

    /**
     * Creates new instance of {@link MapSomethingToExecuteAsBlocking}
     *
     * @param preparedOperation non-null instance of {@link PreparedOperation} which will be used to react on calls to rx function
     * @param <Something>       type of map argument
     * @param <Result>          type of result of rx map function
     * @return new instance of {@link MapSomethingToExecuteAsBlocking}
     */
    @NonNull
    public static <Something, Result, Data> Func1<Something, Result> newInstance(@NonNull PreparedOperation<Result, Data> preparedOperation) {
        return new MapSomethingToExecuteAsBlocking<Something, Result, Data>(preparedOperation);
    }

    @Override
    public Result call(Something changes) {
        return preparedOperation.executeAsBlocking();
    }
}
