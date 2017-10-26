package com.pryv;

/**
 *
 * Pryv class, contains the configuration of the API.
 *
 * @author ik
 *
 */
public class Pryv {

  public static String DOMAIN = "pryv.me";
  public static String USERNAME = "javalib";
  public static String REGISTRATION_URL = "https://reg." + DOMAIN + "/access";
  public static String URL = "https://" + USERNAME + "." + DOMAIN;

  private static boolean supervisorActivated = true;

  /**
   * set the domain
   *
   * @param domain
   */
  public static void setDomain(String domain) {
    DOMAIN = domain;
    updateUrls();
  }

  /**
   * set the username
   *
   * @param username
   */
  public static void setUsername(String username) {
    USERNAME = username;
    updateUrls();
  }

  private static void updateUrls() {
    REGISTRATION_URL = "https://reg." + DOMAIN + "/access";
    URL = "https://" + USERNAME + "." + DOMAIN;
  }

  /**
   * This function returns if the Supervisor is activated or not.
   *
   * @return
   */
  public static boolean isSupervisorActive() {
    return supervisorActivated;
  }

  /**
   * activates the Supervisor
   */
  public static void activatedSupervisor() {
    supervisorActivated = true;
  }

  /**
   * stop using the Supervisor
   */
  public static void deactivateSupervisor() {
    supervisorActivated = false;
  }

}
