package com.pushtorefresh.storio.common.annotations.processor.introspection;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public enum JavaType {
    BOOLEAN("INTEGER"),
    BOOLEAN_OBJECT("INTEGER"),
    SHORT("INTEGER"),
    SHORT_OBJECT("INTEGER"),
    INTEGER("INTEGER"),
    INTEGER_OBJECT("INTEGER"),
    LONG("INTEGER"),
    LONG_OBJECT("INTEGER"),
    FLOAT("REAL"),
    FLOAT_OBJECT("REAL"),
    DOUBLE("REAL"),
    DOUBLE_OBJECT("REAL"),
    STRING("TEXT"),
    BYTE_ARRAY("BLOB");

    JavaType(String sqlType) {
        this.sqlType = sqlType;
    }

    private String sqlType;

    @NotNull
    public static JavaType from(@NotNull TypeMirror typeMirror) {
        final TypeKind typeKind = typeMirror.getKind();
        final String typeName = typeMirror.toString(); // fqn of type, for example java.lang.String

        if (typeKind == TypeKind.BOOLEAN) {
            return BOOLEAN;
        } else if (Boolean.class.getCanonicalName().equals(typeName)) {
            return BOOLEAN_OBJECT;
        } else if (typeKind == TypeKind.SHORT) {
            return SHORT;
        } else if (Short.class.getCanonicalName().equals(typeName)) {
            return SHORT_OBJECT;
        } else if (typeKind == TypeKind.INT) {
            return INTEGER;
        } else if (Integer.class.getCanonicalName().equals(typeName)) {
            return INTEGER_OBJECT;
        } else if (typeKind == TypeKind.LONG) {
            return LONG;
        } else if (Long.class.getCanonicalName().equals(typeName)) {
            return LONG_OBJECT;
        } else if (typeKind == TypeKind.FLOAT) {
            return FLOAT;
        } else if (Float.class.getCanonicalName().equals(typeName)) {
            return FLOAT_OBJECT;
        } else if (typeKind == TypeKind.DOUBLE) {
            return DOUBLE;
        } else if (Double.class.getCanonicalName().equals(typeName)) {
            return DOUBLE_OBJECT;
        } else if (String.class.getCanonicalName().equals(typeName)) {
            return STRING;
        } else if (byte[].class.getCanonicalName().equals(typeName)) {
            return BYTE_ARRAY;
        } else {
            throw new IllegalArgumentException("Unsupported type: " + typeMirror);
        }
    }

    public boolean isBoxedType() {
        switch (this) {
            case BOOLEAN_OBJECT:
            case SHORT_OBJECT:
            case INTEGER_OBJECT:
            case LONG_OBJECT:
            case FLOAT_OBJECT:
            case DOUBLE_OBJECT:
                return true;
            default:
                return false;
        }
    }

    public String getSqlType() {
        return sqlType;
    }
}
