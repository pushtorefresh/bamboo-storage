package com.pushtorefresh.storio.sqlite.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for marking field of some class as SQLite column
 */
@Target(FIELD)
@Retention(RUNTIME) // we allow users to write reflection based code to work with annotation
public @interface StorIOSQLiteColumn {

    /**
     * Required: specifies column name
     *
     * @return not-null column name
     */
    String name();

    /**
     * Optional: marks column as key, so it will be used to identify rows for Put and Delete Operations
     *
     * @return true if column is key, false otherwise
     */
    boolean key() default false;
}
