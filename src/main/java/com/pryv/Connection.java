package com.pryv;

import java.net.URL;

import com.pryv.api.EventManager;
import com.pryv.api.StreamManager;

/**
 *
 * Pryv API connection
 *
 * @author ik
 *
 */
public class Connection implements StreamManager, EventManager {

  private String username;
  private String token;
  private String apiDomain = Pryv.API_DOMAIN; // pryv.io or pryv.in
  private String apiScheme = "https"; // https

  /**
   * @return API endpoint
   */
  public URL getURL() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.apiScheme);
    // ...
    // apiScheme://username.apiDomain/
    return null;
  }

  public String getUsername() {
    return username;
  }

  public String getToken() {
    return token;
  }

  public String getApiDomain() {
    return apiDomain;
  }

  public String getApiScheme() {
    return apiScheme;
  }

}
