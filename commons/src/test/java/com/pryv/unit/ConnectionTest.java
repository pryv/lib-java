package com.pryv.unit;

import com.pryv.Connection;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

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
    connection = new Connection("testUID2", "token", "pryv.io");
  }

}
