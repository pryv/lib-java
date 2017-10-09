package com.pryv.acceptance;

import com.jayway.awaitility.Awaitility;
import com.pryv.Connection;
import com.pryv.Filter;
import com.pryv.database.DBinitCallback;
import com.pryv.interfaces.CreateCallback;
import com.pryv.interfaces.DeleteCallback;
import com.pryv.interfaces.GetCallback;
import com.pryv.interfaces.UpdateCallback;
import com.pryv.model.Access;
import com.pryv.model.ApiResource;
import com.pryv.model.Permission;
import com.pryv.utils.Logger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;

import resources.TestCredentials;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionAccessesTest {

    private static Logger logger = Logger.getInstance();

    private static GetCallback getCallback;
    private static CreateCallback createCallback;
    private static UpdateCallback updateCallback;
    private static DeleteCallback deleteCallback;

    private static List<Access> accesses;
    private static Access access;

    private static String accessName = "testAccess";
    private static String streamId = "testStreamId";
    private static Permission.Level permissionLevel = Permission.Level.read;
    private static String deletedId;
    private static String accessId;

    private static boolean apiSuccess = false;
    private static boolean apiError = false;

    private static Connection connection;

    @BeforeClass
    public static void testCreateAccess() throws Exception {

        instantiateCallbacks();

        connection =
                new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN, TestCredentials.DOMAIN, true, new DBinitCallback());

        connection.setupCacheScope(new Filter());

        apiSuccess = false;

        Access newAccess = new Access();
        newAccess.setName(accessName);
        newAccess.addPermission(new Permission(streamId, permissionLevel, null));
        connection.accesses.create(newAccess, createCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        assertTrue(apiSuccess);
        assertNotNull(access);
        accessId = access.getId();
        assertNotNull(accessId);
        assertNotNull(access.getToken());
        assertNotNull(access.getCreated());
        assertNotNull(access.getCreatedBy());
        assertNotNull(access.getModified());
        assertNotNull(access.getModifiedBy());
        assertNotNull(access.getType());
        assertEquals(access.getName(), accessName);
        assertEquals(access.getPermissions().get(0).getStreamId(), streamId);
        assertEquals(access.getPermissions().get(0).getLevel(), permissionLevel);
    }

    @AfterClass
    public static void testDeleteAccess() throws Exception {
        connection.accesses.delete(accessId, deleteCallback);
        Awaitility.await().until(hasApiResult());
        assertEquals(deletedId, accessId);
    }

    @Before
    public void beforeEachTest() {
        accesses = null;
        access = null;
        apiSuccess = false;
        apiError = false;
    }

    @Test
    public void testGetAccess() {
        connection.accesses.get(getCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        assertTrue(apiSuccess);
        assertNotNull(accesses);
        assertTrue(accesses.size() > 0);
        Boolean found = false;
        for (Access access: accesses) {
            if(access.getId().equals(accessId)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testUpdateAccess() {
        Access newAccess = new Access();
        String newAccessName = "newAccess";
        String newStreamId = "newStreamId";
        Permission.Level newPermissionLevel = Permission.Level.contribute;
        newAccess.setName(newAccessName);
        newAccess.addPermission(new Permission(newStreamId, newPermissionLevel, null));

        connection.accesses.update(accessId, newAccess ,updateCallback);
        Awaitility.await().until(hasApiResult());
        assertFalse(apiError);
        assertTrue(apiSuccess);
        assertNotNull(access);
        assertEquals(access.getName(), newAccessName);
        assertEquals(access.getPermissions().get(0).getStreamId(), newStreamId);
        assertEquals(access.getPermissions().get(0).getLevel(), newPermissionLevel);
    }

    private static Callable<Boolean> hasApiResult() {
        return new Callable<Boolean>() {

            @Override
            public Boolean call() throws Exception {
                return (apiSuccess || apiError);
            }
        };
    }

    private static void instantiateCallbacks() {
        getCallback = new GetCallback() {

            @Override
            public void onSuccess(String successMessage, List resources, Double serverTime) {
                accesses = resources;
                apiSuccess = true;
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                logger.log("Error during Access retrieval: " + errorMessage);
                apiError = true;
            }
        };
        createCallback = new CreateCallback() {
            @Override
            public void onSuccess(String successMessage, ApiResource resource, Double serverTime) {
                access = (Access) resource;
                apiSuccess = true;
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                logger.log("Error during Access creation: " + errorMessage);
                apiError = true;
            }
        };
        updateCallback = new UpdateCallback() {
            @Override
            public void onSuccess(String successMessage, ApiResource resource, Double serverTime) {
                access = (Access) resource;
                apiSuccess = true;
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                logger.log("Error during Access update: " + errorMessage);
                apiError = true;
            }
        };
        deleteCallback = new DeleteCallback() {
            @Override
            public void onSuccess(String successMessage, String id, Double serverTime) {
                deletedId = id;
                apiSuccess = true;
            }

            @Override
            public void onError(String errorMessage, Double serverTime) {
                logger.log("Error during Access deletion: " + errorMessage);
                apiError = true;
            }
        };
    }
}
