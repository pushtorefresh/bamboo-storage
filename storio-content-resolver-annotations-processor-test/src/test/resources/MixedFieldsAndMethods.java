package com.pushtorefresh.storio2.contentresolver.annotations;

@StorIOContentResolverType(uri = "content://uri")
public class MixedFieldsAndMethods {

    @StorIOContentResolverColumn(name = "id", key = true)
    long id;

    @StorIOContentResolverColumn(name = "name")
    String name() {
        return "name";
    }
}