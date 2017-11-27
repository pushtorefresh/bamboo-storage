package com.pushtorefresh.storio3.contentresolver.annotations;

import android.support.annotation.NonNull;
import com.pushtorefresh.storio3.contentresolver.operations.delete.DefaultDeleteResolver;
import com.pushtorefresh.storio3.contentresolver.queries.DeleteQuery;
import java.lang.Override;

/**
 * Generated resolver for Delete Operation.
 */
public class BoxedTypesMethodsFactoryMethodIgnoreNullStorIOContentResolverDeleteResolver extends DefaultDeleteResolver<BoxedTypesMethodsFactoryMethodIgnoreNull> {
    /**
     * {@inheritDoc}
     */
    @Override
    @NonNull
    public DeleteQuery mapToDeleteQuery(@NonNull BoxedTypesMethodsFactoryMethodIgnoreNull object) {
        return DeleteQuery.builder()
            .uri("content://uri")
            .where("field4 = ?")
            .whereArgs(object.getField4())
            .build();
    }
}