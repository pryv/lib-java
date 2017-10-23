package com.pryv.api;

/**
 * OnlineManager manages data from online Pryv API
 *
 * @author ik
 */
public class OnlineManager {

    private HttpClient httpClient;

    private String apiUrl;
    private String tokenUrlArgument;

    /**
     * Constructor for online module
     *
     * @param pUrl            the url of the remote API
     * @param token           the token passed with each request for auth
     */
    public OnlineManager(String pUrl, String token) {
        apiUrl = pUrl;
        tokenUrlArgument = "?auth=" + token;
    }

    public HttpClient getHttpClient() {
        if(httpClient == null) {
            httpClient = new HttpClient(apiUrl, tokenUrlArgument);
        }
        return httpClient;
    }

}
