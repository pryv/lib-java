package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.pryv.api.model.Permission;

/**
 *
 * unit test for Permission object
 *
 * @author ik
 *
 */
public class PermissionTest {

  private String streamId = "abc";
  private Permission.Level perm = Permission.Level.contribute;
  private String defaultName = "ddd";
  private Permission testPermission;
  private Gson gson = new Gson();
  private String stringTestPermission;

  @Before
  public void setUp() throws Exception {
    testPermission = new Permission(streamId, perm, defaultName);
    stringTestPermission = gson.toJson(testPermission);
  }

  @Test
  public void testCreationNotNull() {
    assertNotNull(testPermission);
  }

  @Test
  public void testCreateJsonFromPermission() {
    assertNotNull(stringTestPermission);
  }

  @Test
  public void testCreatPermissionFromJson() {
    Permission permFromJson = gson.fromJson(stringTestPermission, Permission.class);
    assertEquals(streamId, permFromJson.getStreamId());
    assertEquals(perm, permFromJson.getLevel());
    assertEquals(defaultName, permFromJson.getDefaultName());
  }
}
