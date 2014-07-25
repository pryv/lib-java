package com.pryv.auth;

import java.applet.Applet;
import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.pryv.Connection;

/**
 *
 * Web view that is displayed to enter credentials
 *
 * @author ik
 *
 */
public class AuthBrowserView extends Applet implements AuthView {
  private AuthController controller;

  public AuthBrowserView(AuthController pController) {
    controller = pController;
  }

  public static void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
      try {
        desktop.browse(uri);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void displayLoginVew(String loginURL) {
    try {
      URL url = new URL(loginURL);
      openWebpage(url.toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  // unused
  public void onDisplaySuccess(Connection c) {
  }

  public void onDisplayFailure() {
  }

}
