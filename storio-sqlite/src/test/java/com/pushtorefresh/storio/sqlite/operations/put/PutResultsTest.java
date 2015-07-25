package com.pushtorefresh.storio.sqlite.operations.put;

import com.pushtorefresh.storio.test.ToStringChecker;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import nl.jqno.equalsverifier.EqualsVerifier;

import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class PutResultsTest {

    @Test
    public void numberOfInsertsShouldBeZero() {
        final Map<String, PutResult> putResultMap = singletonMap(
                "key",
                PutResult.newUpdateResult(1, "affected_table")
        );

        final PutResults<String> putResults = PutResults.newInstance(putResultMap);

        assertEquals(0, putResults.numberOfInserts());

        // We cache this value, so let's test that cache works too
        // (coverage tool will report it as untested branch if we won't check this)
        assertEquals(0, putResults.numberOfInserts());
    }

    @Test
    public void numberOfUpdatesShouldBeZero() {
        final Map<String, PutResult> putResultMap = singletonMap(
                "key",
                PutResult.newInsertResult(1L, "affected_table")
        );

        final PutResults<String> putResults = PutResults.newInstance(putResultMap);

        assertEquals(0, putResults.numberOfUpdates());

        // We cache this value, so let's test that cache works too
        // (coverage tool will report it as untested branch if we won't check this)
        assertEquals(0, putResults.numberOfUpdates());
    }

    @Test
    public void mixOfInsertsAndUpdatesShouldBeHandledCorrectly() {
        final Map<String, PutResult> putResultMap = new HashMap<String, PutResult>();

        // 3 inserts + 2 updates (notice, that one of the updates updated 5 rows!)

        putResultMap.put("insert1", PutResult.newInsertResult(1L, "affected_table"));
        putResultMap.put("update1", PutResult.newUpdateResult(5, "affected_table"));
        putResultMap.put("insert2", PutResult.newInsertResult(2L, "affected_table2"));
        putResultMap.put("update2", PutResult.newUpdateResult(1, "affected_table"));
        putResultMap.put("insert3", PutResult.newInsertResult(2L, "test_table"));

        final PutResults<String> putResults = PutResults.newInstance(putResultMap);

        assertEquals(3, putResults.numberOfInserts());
        assertEquals(6, putResults.numberOfUpdates());
    }

    @Test
    public void verifyEqualsAndHashCodeImplementation() {
        EqualsVerifier
                .forClass(PutResults.class)
                .allFieldsShouldBeUsed()
                .verify();
    }

    @Test
    public void checkToStringImplementation() {
        ToStringChecker
                .forClass(PutResults.class)
                .check();
    }
}
