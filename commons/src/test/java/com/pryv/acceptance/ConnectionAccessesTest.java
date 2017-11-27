package com.pryv.acceptance;

import com.pryv.Connection;
import com.pryv.exceptions.ApiException;
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
    private static Access access;
    private static Double creationTime;
    private static String accessToken;

    private static Connection connection;

    @BeforeClass
    public static void testCreateAccess() throws IOException, ApiException {

        connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN);

        Access newAccess = new Access()
                .setName(accessName)
                .addPermission(new Permission(streamId, permissionLevel, null))
                .setType(accessType);

        Long requestTime = System.currentTimeMillis()/1000;
        access = connection.accesses.create(newAccess);

        assertNotNull(access);
        assertNotNull(access.getId());
        accessToken = access.getToken();
        assertNotNull(accessToken);
        creationTime = access.getCreated();
        assertEquals(creationTime, requestTime.doubleValue(), 180);
        assertEquals(access.getCreatedBy(), TestCredentials.TOKENID);
        assertEquals(access.getModified(), creationTime);
        assertEquals(access.getModifiedBy(), TestCredentials.TOKENID);
        assertEquals(access.getType(), accessType);
        assertEquals(access.getName(), accessName);
        ArrayList<Permission> permissions = access.getPermissions();
        assertNotNull(permissions);
        assertTrue(permissions.size() > 0);
        Permission firstPermission = permissions.get(0);
        assertNotNull(firstPermission);
        assertEquals(firstPermission.getStreamId(), streamId);
        assertEquals(firstPermission.getLevel(), permissionLevel);
    }

    @AfterClass
    public static void testDeleteAccess() throws IOException, ApiException {
        Access deletedAccess = connection.accesses.delete(access);
        assertTrue(deletedAccess.isDeleted());
    }

    @Test
    public void testGetAccess() throws IOException, ApiException {
        List<Access> retrievedAccesses = connection.accesses.get();
        assertNotNull(retrievedAccesses);
        assertTrue(retrievedAccesses.size() > 0);
        Boolean found = false;
        for (Access retrievedAccess: retrievedAccesses) {
            if(retrievedAccess.getId().equals(access.getId())) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testUpdateAccess() throws IOException, ApiException {
        String newAccessName = "newAccess";
        String newStreamId = "newStreamId";
        Permission.Level newPermissionLevel = Permission.Level.contribute;
        ArrayList<Permission> newPermissions = new ArrayList<>();
        newPermissions.add(new Permission(newStreamId, newPermissionLevel, null));

        access.setName(newAccessName)
                .setPermissions(newPermissions);

        Long updateTime = System.currentTimeMillis()/1000;
        Access updatedAccess = connection.accesses.update(access);

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
