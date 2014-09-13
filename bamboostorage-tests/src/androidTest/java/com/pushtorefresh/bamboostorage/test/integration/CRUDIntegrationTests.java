package com.pushtorefresh.bamboostorage.test.integration;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.pushtorefresh.bamboostorage.BambooStorage;
import com.pushtorefresh.bamboostorage.test.app.TestStorableItem;

import java.util.List;
import java.util.Random;

/**
 * @author Artem Zinnatullin [artem.zinnatullin@gmail.com]
 */
public class CRUDIntegrationTests extends AndroidTestCase {

    private final Random mRandom = new Random(System.currentTimeMillis());

    private BambooStorage mBambooStorage;

    @Override protected void setUp() throws Exception {
        super.setUp();

        if (mBambooStorage == null) {
            mBambooStorage = new BambooStorage(getContext(), "com.pushtorefresh.bamboostorage.test");
        }

        // clearing storage before each test
        removeAllTestStorableItems();
    }

    private void removeAllTestStorableItems() {
        mBambooStorage.removeAllOfType(TestStorableItem.class);
        assertEquals(0, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testAdd() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        assertTrue(storableItem.getInternalId() > 0);
        assertTrue(mBambooStorage.contains(storableItem));
    }

    public void testAddSameItemTwice() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);
        mBambooStorage.add(storableItem);

        assertEquals(2, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testUpdate() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        TestStorableItem storedItem = mBambooStorage.getByInternalId(TestStorableItem.class, storableItem.getInternalId());
        assertNotNull(storedItem);
        assertEquals(storableItem, storedItem);

        storedItem.setTestStringField("updated");
        storedItem.setTestIntField(mRandom.nextInt());
        storedItem.setTestLongField(mRandom.nextLong());

        int countOfUpdatedItems = mBambooStorage.update(storedItem);

        assertEquals(1, countOfUpdatedItems);
        assertEquals(1, mBambooStorage.countOfItems(storableItem.getClass()));

        TestStorableItem updatedStoredItem = mBambooStorage.getByInternalId(storableItem.getClass(), storableItem.getInternalId());

        assertEquals(storedItem, updatedStoredItem);
    }

    public void testAddOrUpdateShouldUpdate() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        TestStorableItem storedItem = mBambooStorage.getByInternalId(storableItem.getClass(), storableItem.getInternalId());
        assertNotNull(storedItem);
        assertEquals(storableItem, storedItem);

        storedItem.setTestStringField("should update");
        storedItem.setTestIntField(mRandom.nextInt());
        storedItem.setTestLongField(mRandom.nextLong());

        boolean trueIfAddedFalseIfUpdated = mBambooStorage.addOrUpdate(storedItem);

        assertFalse(trueIfAddedFalseIfUpdated);
        assertEquals(1, mBambooStorage.countOfItems(storableItem.getClass()));

        TestStorableItem updatedStoredItem = mBambooStorage.getByInternalId(storableItem.getClass(), storableItem.getInternalId());

        assertEquals(storedItem, updatedStoredItem);
    }

    public void testAddOrUpdateShouldAdd() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        storableItem.setInternalId(TestStorableItem.DEFAULT_INTERNAL_ITEM_ID);
        storableItem.setTestStringField("should add new item");
        storableItem.setTestIntField(mRandom.nextInt());
        storableItem.setTestLongField(mRandom.nextLong());

        boolean trueIfAddedFalseIfUpdated = mBambooStorage.addOrUpdate(storableItem);

        assertTrue(trueIfAddedFalseIfUpdated);
        assertEquals(2, mBambooStorage.countOfItems(storableItem.getClass()));

        assertEquals(storableItem, mBambooStorage.getLast(storableItem.getClass()));
    }

    public void testGetByInternalIdPositive() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        TestStorableItem storedItem = mBambooStorage.getByInternalId(storableItem.getClass(), storableItem.getInternalId());

        assertNotSame(storableItem, storedItem);
        assertEquals(storableItem, storedItem);
    }

    public void testGetByInternalIdNegative() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);

        // NOTICE: internal_id + 1
        TestStorableItem storedItem = mBambooStorage.getByInternalId(storableItem.getClass(), storableItem.getInternalId() + 1);

        assertNull(storedItem);
    }

    public void testGetAsListByClassOnly() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        List<TestStorableItem> list = mBambooStorage.getAsList(TestStorableItem.class);

        assertEquals(3, list.size());

        assertEquals(storableItem0, list.get(0));
        assertEquals(storableItem1, list.get(1));
        assertEquals(storableItem2, list.get(2));
    }

    public void testGetAsListWithWhereCondition() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));

        List<TestStorableItem> list = mBambooStorage.getAsList(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(storableItem0.getTestIntField()) }
        );

        assertEquals(1, list.size());

        assertEquals(storableItem0, list.get(0));
    }

    public void testGetAsCursorWithoutWhere() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        Cursor cursor = mBambooStorage.getAsCursor(TestStorableItem.class, null, null, null);

        assertNotNull(cursor);
        assertEquals(3, cursor.getCount());
        cursor.close();
    }

    public void testGetAsCursorWithWhere() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        Cursor cursor = mBambooStorage.getAsCursor(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(storableItem0.getTestIntField()) },
                null
        );

        assertNotNull(cursor);
        assertEquals(1, cursor.getCount());

        cursor.moveToFirst();

        assertEquals(storableItem0, BambooStorage.createStorableItemFromCursor(TestStorableItem.class, cursor));

        cursor.close();
    }

    public void testGetFirstWithWherePositive() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);

        // NOTICE, two elements with same WHERE field value
        // BambooStorage should return FIRST element
        storableItem1.setTestIntField(1).setTestStringField("first");
        storableItem2.setTestIntField(1).setTestStringField("second");

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        TestStorableItem storedItem = mBambooStorage.getFirst(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(storableItem1.getTestIntField() )},
                null
        );

        assertFalse(storableItem2.equals(storedItem));
        assertEquals(storableItem1, storedItem);
    }

    public void testGetFirstWithWhereNegative() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        TestStorableItem storedItem = mBambooStorage.getFirst(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(4) },
                null
        );

        assertNull(storedItem);
    }

    public void testGetFirstWithoutWherePositive() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);

        TestStorableItem firstStoredItem = mBambooStorage.getFirst(TestStorableItem.class);

        assertEquals(storableItem0, firstStoredItem);
        assertFalse(storableItem1.equals(firstStoredItem));
    }

    public void testGetFirstWithoutWhereNegative() {
        TestStorableItem firstStoredItem = mBambooStorage.getFirst(TestStorableItem.class);
        assertNull(firstStoredItem);
    }

    public void testGetLastWithWherePositive() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);

        // NOTICE, two elements with same WHERE field value
        // BambooStorage should return SECOND element
        storableItem1.setTestIntField(1).setTestStringField("first");
        storableItem2.setTestIntField(2).setTestStringField("second");

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        TestStorableItem lastItem = mBambooStorage.getLast(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(storableItem2.getTestIntField()) },
                null
        );

        assertFalse(storableItem1.equals(lastItem));
        assertEquals(storableItem2, lastItem);
    }

    public void testGetLastWithWhereNegative() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        TestStorableItem lastItem = mBambooStorage.getLast(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(3) },
                null
        );

        assertNull(lastItem);
    }

    public void testGetLastWithoutWherePositive() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        TestStorableItem lastItem = mBambooStorage.getLast(TestStorableItem.class);

        assertEquals(storableItem2, lastItem);
    }

    public void testGetLastWithoutWhereNegative() {
        assertNull(mBambooStorage.getLast(TestStorableItem.class));
    }

    public void testRemoveWithoutWherePositive() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem);
        assertTrue(mBambooStorage.contains(storableItem));

        assertEquals(1, mBambooStorage.remove(storableItem));
        assertFalse(mBambooStorage.contains(storableItem));
    }

    public void testRemoveWithoutWhereNegative() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();

        assertFalse(mBambooStorage.contains(storableItem));
        assertEquals(0, mBambooStorage.remove(storableItem));
        assertFalse(mBambooStorage.contains(storableItem));
    }

    public void testRemoveWithWherePositive() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        assertTrue(mBambooStorage.contains(storableItem1));
        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));

        assertEquals(1, mBambooStorage.remove(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(storableItem1.getTestIntField()) }
        ));

        assertFalse(mBambooStorage.contains(storableItem1));
        assertEquals(2, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testRemoveWithWhereNegative() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        storableItem0.setTestIntField(0);
        storableItem1.setTestIntField(1);
        storableItem2.setTestIntField(2);

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));

        assertEquals(0, mBambooStorage.remove(
                TestStorableItem.class,
                TestStorableItem.TableInfo.TEST_INT_FIELD + " = ?",
                new String[] { String.valueOf(4) }
        ));

        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testRemoveAllOfType() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));
        assertEquals(3, mBambooStorage.removeAllOfType(TestStorableItem.class));
        assertEquals(0, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testContainsPositive() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();
        mBambooStorage.add(storableItem);
        assertTrue(mBambooStorage.contains(storableItem));
    }

    public void testContainsNegative() {
        TestStorableItem storableItem = TestStorableItemFactory.newRandomItem();
        // not putting item to the storage
        assertFalse(mBambooStorage.contains(storableItem));
    }

    public void testCountOfItems0() {
        assertEquals(0, mBambooStorage.countOfItems(TestStorableItem.class));
    }

    public void testCountOfItems3() {
        TestStorableItem storableItem0 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem1 = TestStorableItemFactory.newRandomItem();
        TestStorableItem storableItem2 = TestStorableItemFactory.newRandomItem();

        mBambooStorage.add(storableItem0);
        mBambooStorage.add(storableItem1);
        mBambooStorage.add(storableItem2);

        assertEquals(3, mBambooStorage.countOfItems(TestStorableItem.class));
    }
}
