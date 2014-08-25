package com.pryv;

/**
 *
 * high-level class
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
  public boolean isCacheActive() {
    return cacheActivated;
  }

  /**
   * This function returns if the online module is activated or not.
   *
   * @return
   */
  public boolean isOnlineActive() {
    return onlineActivated;
  }

  /**
   * This function returns if the Supervisor is activated or not.
   *
   * @return
   */
  public boolean isSupervisorActive() {
    return supervisorActivated;
  }

  /**
   * activates the cache
   */
  public void activateCache() {
    cacheActivated = true;
  }

  /**
   * stop using the cache
   */
  public void deactivateCache() {
    cacheActivated = false;
  }

  /**
   * activates the online module
   */
  public void activateOnline() {
    onlineActivated = true;
  }

  /**
   * Stops using the online module
   */
  public void deactivateOnline() {
    onlineActivated = false;
  }

  /**
   * activates the Supervisor
   */
  public void activatedSupervisor() {
    supervisorActivated = true;
  }

  /**
   * stop using the Supervisor
   */
  public void deactivateSupervisor() {
    supervisorActivated = false;
  }

}
