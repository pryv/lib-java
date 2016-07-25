package com.pryv.unit;

import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.pryv.Connection;
import com.pryv.database.DBinitCallback;

public class ConnectionTest {

  private Connection connection;

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  @Before
  public void setUp() throws Exception {
    connection = new Connection("testUID2", "token", "pryv.io", true, new DBinitCallback());
  }

  @Test
  public void testIdCachingGeneration() {
    String idCaching = connection.generateCacheFolderName();
    System.out.println("test id caching generation: generated: " + idCaching);
    assertNotNull(idCaching);
  }

}
