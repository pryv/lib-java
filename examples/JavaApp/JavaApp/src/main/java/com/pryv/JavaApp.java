package com.pryv;

import resources.TestCredentials;

/**
 * Test Java App
 *
 */
public class JavaApp {
  public static void main(String[] args) {
    Pryv.setStaging();
    Connection connection = new Connection(TestCredentials.USERNAME, TestCredentials.TOKEN);
    connection.get(connection);

    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    connection.get(connection);
  }
}
