package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.model.Access;
import com.pryv.model.Permission;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionAccessesTest {

    private static String accessName = "testAccess";
    private static String streamId = "testStreamId";
    private static String accessType = "shared";
    private static Permission.Level permissionLevel = Permission.Level.read;
    private static String accessId;
    private static Double creationTime;
    private static String accessToken;

    private static Connection connection;

    @BeforeClass
    public static void testCreateAccess() throws IOException {

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN);

        Access newAccess = new Access()
                .setName(accessName)
                .addPermission(new Permission(streamId, permissionLevel, null))
                .setType(accessType);

        Long requestTime = System.currentTimeMillis()/1000;
        Access createdAccess = connection.accesses.create(newAccess);

        assertNotNull(createdAccess);
        accessId = createdAccess.getId();
        assertNotNull(accessId);
        accessToken = createdAccess.getToken();
        assertNotNull(accessToken);
        creationTime = createdAccess.getCreated();
        assertEquals(creationTime, requestTime.doubleValue(), 180);
        assertEquals(createdAccess.getCreatedBy(), TestCredentials.TOKENID);
        assertEquals(createdAccess.getModified(), creationTime);
        assertEquals(createdAccess.getModifiedBy(), TestCredentials.TOKENID);
        assertEquals(createdAccess.getType(), accessType);
        assertEquals(createdAccess.getName(), accessName);
        ArrayList<Permission> permissions = createdAccess.getPermissions();
        assertNotNull(permissions);
        assertTrue(permissions.size() > 0);
        Permission firstPermission = permissions.get(0);
        assertNotNull(firstPermission);
        assertEquals(firstPermission.getStreamId(), streamId);
        assertEquals(firstPermission.getLevel(), permissionLevel);
    }

    @AfterClass
    public static void testDeleteAccess() throws IOException {
        String deletionId = connection.accesses.delete(accessId);
        assertEquals(deletionId, accessId);
    }

    @Test
    public void testGetAccess() throws IOException {
        List<Access> retrievedAccesses = connection.accesses.get();
        assertNotNull(retrievedAccesses);
        assertTrue(retrievedAccesses.size() > 0);
        Boolean found = false;
        for (Access access: retrievedAccesses) {
            if(access.getId().equals(accessId)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testUpdateAccess() throws IOException {
        String newAccessName = "newAccess";
        String newStreamId = "newStreamId";
        Permission.Level newPermissionLevel = Permission.Level.contribute;

        Access newAccess = new Access()
                .setName(newAccessName)
                .addPermission(new Permission(newStreamId, newPermissionLevel, null))
                .setId(accessId);

        Long updateTime = System.currentTimeMillis()/1000;
        Access updatedAccess = connection.accesses.update(newAccess);

        assertNotNull(updatedAccess);
        assertEquals(updatedAccess.getName(), newAccessName);
        ArrayList<Permission> permissions = updatedAccess.getPermissions();
        assertNotNull(permissions);
        assertTrue(permissions.size() > 0);
        Permission firstPermission = permissions.get(0);
        assertNotNull(firstPermission);
        assertEquals(firstPermission.getStreamId(), newStreamId);
        assertEquals(firstPermission.getLevel(), newPermissionLevel);
        assertEquals(updatedAccess.getCreated(), creationTime);
        assertEquals(updatedAccess.getCreatedBy(), TestCredentials.TOKENID);
        assertEquals(updatedAccess.getModifiedBy(), TestCredentials.TOKENID);
        assertEquals(updatedAccess.getModified(), updateTime, 180);
        assertEquals(updatedAccess.getType(), accessType);
        assertEquals(updatedAccess.getToken(), accessToken);
    }

}
