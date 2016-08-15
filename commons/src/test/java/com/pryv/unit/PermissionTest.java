package com.pryv.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pryv.model.Permission;
import com.pryv.utils.JsonConverter;

/**
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
  private String stringTestPermission;
  private ObjectMapper mapper = new ObjectMapper();

  @Before
  public void setUp() throws Exception {
    testPermission = new Permission(streamId, perm, defaultName);
    stringTestPermission = JsonConverter.toJson(testPermission);
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
    Permission permFromJson = null;
    try {
      System.out.println(stringTestPermission);
      permFromJson = mapper.readValue(stringTestPermission, Permission.class);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    assertEquals(streamId, permFromJson.getStreamId());
    assertEquals(perm, permFromJson.getLevel());
    assertEquals(defaultName, permFromJson.getDefaultName());
  }
}
