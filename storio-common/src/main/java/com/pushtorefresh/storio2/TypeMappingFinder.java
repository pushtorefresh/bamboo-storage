package com.pushtorefresh.storio2;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio2.internal.TypeMapping;

import java.util.Map;

/**
 * Interface for search type mapping.
 */
public interface TypeMappingFinder {
    @Nullable
    <T> TypeMapping<T> findTypeMapping(@NonNull Class<T> type);

    void directTypeMapping(@Nullable Map<Class<?>, ? extends TypeMapping<?>> directTypeMapping);

    @Nullable
    Map<Class<?>, ? extends TypeMapping<?>> directTypeMapping();
}
