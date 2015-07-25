package com.pushtorefresh.storio.contentresolver.impl;

import android.content.ContentResolver;
import android.net.Uri;

import com.pushtorefresh.storio.contentresolver.ContentResolverTypeMapping;
import com.pushtorefresh.storio.contentresolver.StorIOContentResolver;
import com.pushtorefresh.storio.contentresolver.operations.delete.DeleteResolver;
import com.pushtorefresh.storio.contentresolver.operations.get.GetResolver;
import com.pushtorefresh.storio.contentresolver.operations.put.PutResolver;
import com.pushtorefresh.storio.contentresolver.queries.Query;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DefaultStorIOContentResolverTest {

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void nullContentResolver() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(null);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions"})
    @Test(expected = NullPointerException.class)
    public void addTypeMappingNullType() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(null, ContentResolverTypeMapping.builder()
                        .putResolver(mock(PutResolver.class))
                        .getResolver(mock(GetResolver.class))
                        .deleteResolver(mock(DeleteResolver.class))
                        .build());
    }

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void addTypeMappingNullMapping() {
        DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Object.class, null);
    }

    @Test
    public void shouldReturnNullIfNoTypeMappingsRegistered() {
        class TestItem {

        }

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .build();

        assertNull(storIOContentResolver.internal().typeMapping(TestItem.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnNullIfNotTypeMappingRegisteredForType() {
        class TestItem {

        }

        class Entity {

        }

        final ContentResolverTypeMapping<Entity> entityContentResolverTypeMapping = ContentResolverTypeMapping.<Entity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Entity.class, entityContentResolverTypeMapping)
                .build();

        assertSame(entityContentResolverTypeMapping, storIOContentResolver.internal().typeMapping(Entity.class));

        assertNull(storIOContentResolver.internal().typeMapping(TestItem.class));
    }

    @Test
    public void directTypeMappingShouldWork() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItem.class));
    }

    @Test
    public void indirectTypeMappingShouldWork() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        class TestItemSubclass extends TestItem {

        }

        // Direct type mapping should still work
        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItem.class));

        // Indirect type mapping should give same type mapping as for parent class
        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItemSubclass.class));
    }

    @Test
    public void indirectTypeMappingShouldCacheValue() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .build();

        class TestItemSubclass extends TestItem {

        }

        // Indirect type mapping should give same type mapping as for parent class
        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItemSubclass.class));

        // Next call should be faster (we can not check this exactly)
        // But test coverage tool will check that we executed cache branch
        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItemSubclass.class));
    }

    @Test
    public void typeMappingShouldWorkInCaseOfMoreConcreteTypeMapping() {
        class TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItem> typeMapping = ContentResolverTypeMapping.<TestItem>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        class TestItemSubclass extends TestItem {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<TestItemSubclass> subclassTypeMapping = ContentResolverTypeMapping.<TestItemSubclass>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(TestItem.class, typeMapping)
                .addTypeMapping(TestItemSubclass.class, subclassTypeMapping)
                .build();

        // Parent class should have its own type mapping
        assertSame(typeMapping, storIOContentResolver.internal().typeMapping(TestItem.class));

        // Child class should have its own type mapping
        assertSame(subclassTypeMapping, storIOContentResolver.internal().typeMapping(TestItemSubclass.class));
    }

    @Test
    public void typeMappingShouldFindIndirectTypeMappingInCaseOfComplexInheritance() {
        // Good test case — inheritance with AutoValue/AutoParcel

        class Entity {

        }

        class AutoValue_Entity extends Entity {

        }

        class ConcreteEntity extends Entity {

        }

        class AutoValue_ConcreteEntity extends ConcreteEntity {

        }

        //noinspection unchecked
        final ContentResolverTypeMapping<Entity> entitySQLiteTypeMapping = ContentResolverTypeMapping.<Entity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        //noinspection unchecked
        final ContentResolverTypeMapping<ConcreteEntity> concreteEntitySQLiteTypeMapping = ContentResolverTypeMapping.<ConcreteEntity>builder()
                .putResolver(mock(PutResolver.class))
                .getResolver(mock(GetResolver.class))
                .deleteResolver(mock(DeleteResolver.class))
                .build();

        final StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(mock(ContentResolver.class))
                .addTypeMapping(Entity.class, entitySQLiteTypeMapping)
                .addTypeMapping(ConcreteEntity.class, concreteEntitySQLiteTypeMapping)
                .build();

        // Direct type mapping for Entity should work
        assertSame(entitySQLiteTypeMapping, storIOContentResolver.internal().typeMapping(Entity.class));

        // Direct type mapping for ConcreteEntity should work
        assertSame(concreteEntitySQLiteTypeMapping, storIOContentResolver.internal().typeMapping(ConcreteEntity.class));

        // Indirect type mapping for AutoValue_Entity should get type mapping for Entity
        assertSame(entitySQLiteTypeMapping, storIOContentResolver.internal().typeMapping(AutoValue_Entity.class));

        // Indirect type mapping for AutoValue_ConcreteEntity should get type mapping for ConcreteEntity, not for Entity!
        assertSame(concreteEntitySQLiteTypeMapping, storIOContentResolver.internal().typeMapping(AutoValue_ConcreteEntity.class));
    }

    @Test
    public void shouldThrowExceptionIfContentResolverReturnsNull() {
        ContentResolver contentResolver = mock(ContentResolver.class);

        StorIOContentResolver storIOContentResolver = DefaultStorIOContentResolver.builder()
                .contentResolver(contentResolver)
                .build();

        Query query = Query.builder()
                .uri(mock(Uri.class))
                .build();

        when(contentResolver
                .query(any(Uri.class), any(String[].class), anyString(), any(String[].class), anyString()))
                .thenReturn(null); // Notice, we return null instead of Cursor

        try {
            storIOContentResolver
                    .internal()
                    .query(query);
        } catch (IllegalStateException expected) {
            assertEquals("Cursor returned by content provider is null", expected.getMessage());
        }
    }
}
