package com.pushtorefresh.storio2.sqlite.annotations;

import com.pushtorefresh.storio2.sqlite.SQLiteTypeMapping;

/**
 * Generated mapping with collection of resolvers.
 */
public class PrimitivePrivateFieldsSQLiteTypeMapping extends SQLiteTypeMapping<PrimitivePrivateFields> {
    public PrimitivePrivateFieldsSQLiteTypeMapping() {
        super(new PrimitivePrivateFieldsStorIOSQLitePutResolver(),
                new PrimitivePrivateFieldsStorIOSQLiteGetResolver(),
                new PrimitivePrivateFieldsStorIOSQLiteDeleteResolver());
    }
}