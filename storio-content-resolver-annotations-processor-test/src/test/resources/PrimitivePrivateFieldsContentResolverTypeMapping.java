package com.pushtorefresh.storio2.contentresolver.annotations;

import com.pushtorefresh.storio2.contentresolver.ContentResolverTypeMapping;

/**
 * Generated mapping with collection of resolvers
 */
public class PrimitivePrivateFieldsContentResolverTypeMapping extends ContentResolverTypeMapping<PrimitivePrivateFields> {
    public PrimitivePrivateFieldsContentResolverTypeMapping() {
        super(new PrimitivePrivateFieldsStorIOContentResolverPutResolver(),
                new PrimitivePrivateFieldsStorIOContentResolverGetResolver(),
                new PrimitivePrivateFieldsStorIOContentResolverDeleteResolver());
    }
}
