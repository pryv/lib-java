package com.pryv.unit;

import com.pryv.model.Access;

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * Created by thiebaudmodoux on 15.11.17.
 */

public class AccessTest {
    @Test
    public void testCloneMutableFields() {
        Access immutableAccess = new Access();
        immutableAccess.setId("immutable");
        immutableAccess.setToken("immutable");
        immutableAccess.setType("immutable");
        immutableAccess.setCreated(0.0);
        immutableAccess.setCreatedBy("immutable");
        immutableAccess.setModified(0.0);
        immutableAccess.setModifiedBy("immutable");
        immutableAccess.setLastUsed(0.0);
        Access mutableAccess = immutableAccess.cloneMutableFields();
        assertNull(mutableAccess.getId());
        assertNull(mutableAccess.getToken());
        assertNull(mutableAccess.getType());
        assertNull(mutableAccess.getCreated());
        assertNull(mutableAccess.getCreatedBy());
        assertNull(mutableAccess.getModified());
        assertNull(mutableAccess.getModifiedBy());
        assertNull(mutableAccess.getLastUsed());
    }
}
