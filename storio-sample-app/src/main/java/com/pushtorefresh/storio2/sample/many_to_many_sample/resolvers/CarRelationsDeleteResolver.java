package com.pushtorefresh.storio2.sample.many_to_many_sample.resolvers;

import android.support.annotation.NonNull;

import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.Car;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.CarStorIOSQLiteDeleteResolver;
import com.pushtorefresh.storio2.sample.many_to_many_sample.entities.PersonCarRelationTable;
import com.pushtorefresh.storio2.sqlite.StorIOSQLite;
import com.pushtorefresh.storio2.sqlite.operations.delete.DeleteResult;
import com.pushtorefresh.storio2.sqlite.queries.DeleteQuery;

public class CarRelationsDeleteResolver extends CarStorIOSQLiteDeleteResolver {

    @Override
    @NonNull
    public DeleteResult performDelete(@NonNull StorIOSQLite storIOSQLite, @NonNull Car object) {
        final StorIOSQLite.LowLevel lowLevel = storIOSQLite.lowLevel();
        lowLevel.beginTransaction();
        try {
            final DeleteResult deleteResult = super.performDelete(storIOSQLite, object);

            storIOSQLite.delete()
                    .byQuery(DeleteQuery.builder()
                            .table(PersonCarRelationTable.TABLE)
                            .where(PersonCarRelationTable.COLUMN_CAR_ID + " = ?")
                            .whereArgs(object.id())
                            .build())
                    .prepare()
                    .executeAsBlocking();

            lowLevel.setTransactionSuccessful();

            return deleteResult;
        } finally {
            lowLevel.endTransaction();
        }
    }
}