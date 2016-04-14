package com.pryv.auth;

import java.util.List;

import com.pryv.model.Permission;
import com.pryv.utils.Logger;

/**
 *
 * High-level used to Authenticate the user. Upon success, provides a Connection
 * with the appropriate username and token.
 *
 * @author ik
 *
 */
public class AuthController {

	private String requestingAppId;
	private List<Permission> permissions;
	private AuthView view;
	private AuthModel model;
	// optional
	private String language = "en";
	private String returnURL = "";

	private Logger logger = Logger.getInstance();

	/**
	 *
	 * @param pRequestingAppId
	 *            Your app's identifier
	 * @param pPermissions
	 *            Array of permission request objects
	 * @param pLang
	 *            optional: The two-letter ISO (639-1) code of the language in
	 *            which to display user instructions, if possible. Default: en.
	 * @param pReturnURL
	 *            optional: The URL to redirect the user to after auth completes
	 * @param pView
	 *            the view in which the URL for login is displayed
	 */
	public AuthController(String pRequestingAppId, List<Permission> pPermissions, String pLang, String pReturnURL,
			AuthView pView) {
		requestingAppId = pRequestingAppId;
		permissions = pPermissions;
		if (pLang != null) {
			language = pLang;
		}
		if (pReturnURL != null) {
			returnURL = pReturnURL;
		}
		view = pView;
	}

	public void signIn() {
		model = new AuthModel(this, requestingAppId, permissions, language, returnURL);
		model.startLogin();
	}

	public void onSuccess(String username, String token) {
		view.onAuthSuccess(username, token);
		// acquire ref to new Connection, instanciated
	}

	public void onError(String message) {
		logger.log("AuthControllerImpl: failure: message=" + message);
		view.onAuthError("AuthController: failure: message=" + message);
	}

	public void onRefused(int reasonId, String message, String detail) {
		logger.log("AuthControllerImpl: refused: reasonId=" + reasonId + ", message=" + message + ", detail=" + detail);
		view.onAuthRefused(reasonId, message, detail);
	}

	public void displayLoginView(String url) {
		view.displayLoginView(url);
	}

}
