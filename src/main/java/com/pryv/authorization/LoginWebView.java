package com.pryv.authorization;

import java.applet.Applet;
import java.awt.Desktop;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 *
 * Web view that is displayed to enter credentials
 *
 * @author ik
 *
 */
public class LoginWebView extends Applet implements LoginView {
  private LoginController controller;

  public LoginWebView(LoginController pController) {
    controller = pController;
  }


  public static void openWebpage(URI uri) {
    Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
    if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
        try {
            desktop.browse(uri);
        } catch (Exception e) {
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

}
