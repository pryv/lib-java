package com.pryv.unit;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.ConnectionOld;
import com.pryv.api.database.DBinitCallback;

public class ConnectionTest {

  private ConnectionOld connection;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    connection = new ConnectionOld("testUID2", "token", new DBinitCallback());
  }

  @Test
  public void testIdCachingGeneration() {
    String idCaching = connection.getIdCaching();
    System.out.println("test id caching generation: generated: " + idCaching);
    assertNotNull(idCaching);
  }

}
