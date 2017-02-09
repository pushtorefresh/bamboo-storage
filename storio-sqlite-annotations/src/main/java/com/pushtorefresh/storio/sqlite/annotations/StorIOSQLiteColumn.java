package com.pushtorefresh.storio.sqlite.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for marking field of some class as SQLite column
 */
@Target({FIELD, METHOD})
@Retention(RUNTIME) // we allow users to write reflection based code to work with annotation
public @interface StorIOSQLiteColumn {

    /**
     * Required: specifies column name
     *
     * @return non-null column name
     */
    String name();

    /**
     * Optional: marks column as key, so it will be used to identify rows for Put and Delete Operations
     *
     * @return true if column is key, false otherwise
     */
    boolean key() default false;

    /**
     * Optional: indicates that field should not be serialized when its value is {@code null}
     * Should be used for not primitive types only
     *
     * @return true if column with {@code null} value should be ignored, false otherwise
     */
    boolean ignoreNull() default false;

    /**
     * Optional: Indicates version of database on which column was added. It is used for generating sql to create table.
     * If version is 1 (default value) then code for creating column will be generated in createTable method.
     * Other values will create column code in updateTable section only for databases that version is lower than this field
     * Negative values are prohibited.
     *
     * @return version of database on which column was added.
     */
    int version() default 1;
}
