package com.pryv.auth;

import com.pryv.Pryv;
import com.pryv.model.Permission;

import java.util.List;

/**
 *
 * encloses parameters sent to server upon authentication
 *
 * @author ik
 *
 */
public class AuthenticationRequest {

	private String serverURL;
	private String requestingAppId;
	private List<Permission> requestedPermissions;
	private String languageCode;
	private String returnURL;

	public AuthenticationRequest(String pRequestingAppId, List<Permission> pRequestedPermissions, String pLang,
			String pReturnURL) {
		serverURL = Pryv.REGISTRATION_URL;
		requestingAppId = pRequestingAppId;
		requestedPermissions = pRequestedPermissions;
		languageCode = pLang;
		returnURL = pReturnURL;
	}

	public String getServerURL() {
		return serverURL;
	}

	public String getRequestingAppId() {
		return requestingAppId;
	}

	public List<Permission> getRequestedPermissions() {
		return requestedPermissions;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public String getReturnURL() {
		return returnURL;
	}
}
