package com.pryv.auth;

import java.applet.Applet;
import java.awt.Desktop;
import java.io.IOException;
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
public class AuthBrowserView extends Applet implements AuthView {

	public AuthBrowserView() {
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

	@Override
	public void displayLoginView(String loginURL) {
		try {
			URL url = new URL(loginURL);
			openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onAuthSuccess(String username, String token) {
		// TODO: called from AuthController
	}

	@Override
	public void onAuthError(String message) {
		// TODO: called from AuthController
	}

	@Override
	public void onAuthRefused(int reasonId, String message, String detail) {
		// TODO: called from AuthController
	}

}
