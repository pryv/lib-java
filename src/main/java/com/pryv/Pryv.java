package com.pryv;

/**
 *
 * Pryv class, contains the configuration of the API.
 *
 * @author ik
 *
 */
public class Pryv {

  public static String REGISTRATION_URL = "https://reg.pryv.io/access";
  public static String API_DOMAIN = "pryv.io";
  public static final String DATABASE_NAME = "pryv-sqlite.db";

  private static boolean cacheActivated = true;
  private static boolean onlineActivated = true;
  private static boolean supervisorActivated = true;

  /**
   * makes the library use staging mode from online.
   */
  public static void setStaging() {
    REGISTRATION_URL = "https://reg.pryv.in/access";
    API_DOMAIN = "pryv.in";
  }

  /**
   * This function returns if the cache is activated or not.
   *
   * @return
   */
  public static boolean isCacheActive() {
    return cacheActivated;
  }

  /**
   * This function returns if the online module is activated or not.
   *
   * @return
   */
  public static boolean isOnlineActive() {
    return onlineActivated;
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
   * activates the cache
   */
  public static void activateCache() {
    cacheActivated = true;
  }

  /**
   * stop using the cache
   */
  public static void deactivateCache() {
    cacheActivated = false;
  }

  /**
   * activates the online module
   */
  public static void activateOnline() {
    onlineActivated = true;
  }

  /**
   * Stops using the online module
   */
  public static void deactivateOnline() {
    onlineActivated = false;
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
