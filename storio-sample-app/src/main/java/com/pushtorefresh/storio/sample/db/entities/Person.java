package com.pushtorefresh.storio.sample.db.entities;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.pushtorefresh.storio.sample.db.tables.PersonsTable;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteColumn;
import com.pushtorefresh.storio.sqlite.annotations.StorIOSQLiteType;

import java.util.ArrayList;
import java.util.List;

@StorIOSQLiteType(table = PersonsTable.TABLE)
public class Person {

    /**
     * If object was not inserted into db, id will be null
     */
    @Nullable
    @StorIOSQLiteColumn(name = PersonsTable.COLUMN_ID, key = true)
    Long id;

    @NonNull
    @StorIOSQLiteColumn(name = PersonsTable.COLUMN_NICK)
    String name;

    @NonNull
    List<Car> cars = new ArrayList<Car>();

    // leave default constructor for AutoGenerated code!
    Person() {}

    private Person(@Nullable Long id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public static Person newPerson(@Nullable Long id, @NonNull String name) {
        return new Person(id, name);
    }

    @NonNull
    public static Person newPerson(@NonNull String name) {
        return new Person(null, name);
    }

    @Nullable
    public Long getId() {
        return id;
    }

    public void setId(@Nullable Long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public List<Car> getCars() {
        return cars;
    }

    public void setCars(@NonNull List<Car> cars) {
        this.cars = cars;
    }
}
