package com.pushtorefresh.bamboostorage;

import android.content.ContentValues;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.annotation.NonNull;

/**
 * IBambooStorableItem is an abstraction for items representation in BambooStorage
 * <p/>
 * <p>DEFAULT PUBLIC CONSTRUCTOR IS REQUIRED for creating class instance in BambooStorage
 * for later filling it with Cursor</p>
 *
 * @author Artem Zinnatullin [artem.zinnatullin@gmail.com]
 */
public interface IBambooStorableItem {

    /**
     * Default internal id of the item, if internal item id == default, it means, that it was not stored in the storage
     */
    long DEFAULT_INTERNAL_ITEM_ID = 0;

    /**
     * Returns internal item id
     * If id is less or equals zero, StorableItem was never saved in the storage
     *
     * @return internal item id
     */
    long getInternalId();

    /**
     * Sets internal item internalId
     *
     * @param internalId internal item internalId
     */
    void setInternalId(final long internalId);

    /**
     * Converts this object to ContentValues
     *
     * @param res which can be needed for toContentValues process
     * @return ContentValues representation of StorableItem
     */
    @NonNull ContentValues toContentValues(@NonNull Resources res);

    /**
     * Fills StorableItem's fields from cursor
     *
     * @param cursor with item data
     */
    void fillFromCursor(@NonNull Cursor cursor);
}
