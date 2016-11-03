/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import android.os.ResultReceiver;

import com.ca.mas.core.auth.ble.BluetoothLePeripheralCallback;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.service.AuthenticationProvider;

import org.json.JSONObject;

import java.net.URI;

/**
 * <p>Top-level interface for the Mobile SSO SDK.</p>
 * Use {@link MobileSsoFactory} to obtain an implementation of this.
 */
public interface MobileSso {

    /**
     * Submit a API request to be processed asynchronously.
     *
     * <ul>
     * <li>The response to the request will eventually be delivered to the specified result receiver.</li>
     * <li>This method returns immediately to the calling thread.</li>
     * <li>An activity may be started if a device lock code needs to be configured or if the user must be prompted for a username and password.</li>
     * </ul>
     *
     * @param request        the request to send.  Required.
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error.  Required.
     *                       The result code is defined under {@link com.ca.mas.core.service.MssoIntents} RESULT_CODE_*,
     *                       To retrieve the error message from the returned Bundle with key
     *                       {@link com.ca.mas.core.service.MssoIntents#RESULT_ERROR_MESSAGE}.
     *                       <p>
     *                       A helper class {@link MAGResultReceiver} defined a standard interface to capture the result
     *                       of the API request.
     *                       </p>
     * @return the request ID, which can be used to cancel the request, to cancel the request please refer to
     * {@link #cancelRequest(long)}
     */

    long processRequest(MAGRequest request, ResultReceiver resultReceiver);

    /**
     * <p>Authenticates a user with a username and password. The existing user session will be logged out and authenticated with the provided username
     * and password.</p>
     *
     * <p>The response to the request will eventually be delivered to the specified result receiver.</p>
     * <p>This method returns immediately to the calling thread</p>
     *
     * @param username       The username to authenticate with
     * @param password       The password to authenticate with
     * @param resultReceiver The resultReceiver to notify when a response is available, or if there is an error. Required.
     */
    void authenticate(String username, char[] password, MAGResultReceiver<JSONObject> resultReceiver);

    /**
     * Sets the {@link MobileSsoListener} that will receive various notifications and requests for MAG Client.
     *
     * @param mobileSsoListener an implementation of MobileSsoListener
     */
    void setMobileSsoListener(MobileSsoListener mobileSsoListener);

    /**
     * <p>Requests that any pending queued requests be processed.</p>
     * <p>This can be called from an activity's onResume() method to ensure that
     * any pending requests waiting for an initial unlock code on the device get a chance to continue.</p>
     * <p>This method returns immediately to the calling thread.</p>
     * <p>An activity may be started if a device lock code (still) needs to be configured
     * or if the user must be prompted for a username and password.</p>
     */
    void processPendingRequests();

    /**
     * Cancels the specified request ID. If the response notification has not already been delivered (or is not already in progress)
     * by the time this method executes, a response notification will never occur for the specified request ID.
     *
     * @param requestId the request ID to cancel.
     */
    void cancelRequest(long requestId);

    /**
     * <p>Log out the current user and all SSO apps on this device, leaving the device registered, and
     * optionally informing the token server of the logout.</p>
     * <p>This method takes no action if the use is already logged out.</p>
     * <p>This method destroys the access token and cached password (if any). If SSO is enabled, this method also
     * removes the ID token from the shared token store.</p>
     * <p>If contactServer is true, this method additionally makes a best-effort attempt to notify the server that the
     * access token (and possibly ID token) should be invalidated.</p>
     * <p>If the server needs to be contact, this will be done on the current thread. As this may take some time,
     * callers running on the UI thread and passing true for contactServer should consider running this method
     * within an AsyncTask.</p>
     * <b>NOTE:</b> It is extremely important to make at least one attempt to inform the server
     * of the logout before destroying the tokens client side to try and prevent the server
     * from getting out of sync with the client. <b>Pass contactServer=false only if
     * absolutely necessary</b> (such as to avoid blocking the GUI if you have already made
     * at least one attempt to contact the server).
     *
     * @param contactServer true to make a single best-effort attempt to notify the server of the logout so that
     *                      it can revoke the tokens.  This may fail if we lack network connectivity.
     *                      <br>
     *                      false to destroy the tokens client side but make no effort to inform the server that
     *                      it needs to revoke them.
     *                      <br>
     *                      This option does nothing if there is no ID token (whether or not SSO is enabled).
     * @throws com.ca.mas.core.context.MssoException if contactServer is true, and SSO is enabled, and there is an error while attempting
     *                                               to notify the server of the logout.
     */
    void logout(boolean contactServer);

    /**
     * <p>Clear all tokens in the shared token store.</p>
     * <b>NOTE: You should not normally use this method.</b>
     * This method destroys the client private key, effectively un-registering the device, and should only be used
     * for testing or recovery purposes.
     * <p>If you just wish to log out the current SSO user see the {@link #logout} method instead.</p>
     */
    void destroyAllPersistentTokens();

    /**
     * <p>Remove this device registration from the server.  The token server will identify the device making the request
     * by its TLS client certificate.</p>
     * <p>This does not affect the local cached access token,
     * cached username and password, or the shared token storage in any way.  The client will continue to attempt
     * to present its TLS client certificate on future calls to the token server or a web API endpoint.</p>
     * To destroy the client-side record of the device registration, call {@link #destroyAllPersistentTokens()}.
     * <p>The communication with the token server will occur on the current thread.  As this may take some time,
     * callers running on the UI thread should consider running this method within an AsyncTask.</p>
     *
     * @throws com.ca.mas.core.context.MssoException if there is an error while attempting to tell the token server to unregister this device.
     */
    void removeDeviceRegistration();

    /**
     * Check if the App has already been logged in.
     *
     * @return true if the access token has been acquired, false if the access Token is not available
     */
    boolean isAppLogon();

    /**
     * Check if the user has already been logged in.
     *
     * @return true if the id token has been acquired and cached, false if the id token is not available
     */
    boolean isLogin();

    /**
     * Retrieve the cached user profile.
     *
     * @return The user profile that has bee acquireda and cached, or null if empty
     */
    String getUserProfile();

    /**
     * Logs off the App by removing cached access token. This forces the next request to obtain a new access token.
     */
    @Deprecated
    void logoffApp();

    /**
     * Logs off the device by removing the device registration from the server and removing
     * the cached ID token and access token from the device.
     * Refer to {@link #removeDeviceRegistration()} instead.
     */
    @Deprecated
    void logoutDevice();

    /**
     * Checks if the device has already been registered.
     *
     * @return true if device registered has already completed and a client cert chain and ID token are present in the token store.
     * false if registration is required.
     */
    boolean isDeviceRegistered();

    /**
     * Provides the configuration detail for the MobileSso.
     *
     * @return The Configuration Provider
     */
    ConfigurationProvider getConfigurationProvider();

    /**
     * Performs a remote authorization with the provider URL. (For Example QRCode)
     *
     * @param url            The temporary URL to enable the remote session.
     * @param resultReceiver the resultReceiver to notify when a response is available, or if there is an error.  Required.
     */
    void authorize(String url, ResultReceiver resultReceiver);

    /**
     * This method is used by a device to start a BLE session sharing in a peripheral role.
     * Register your callback to receive events and errors during the session sharing.
     *
     * @param callback Register your callback to receive event and error during the session sharing.
     */
    void startBleSessionSharing(BluetoothLePeripheralCallback callback);

    /**
     * Stops the Bluetooth LE session sharing.
     */
    void stopBleSessionSharing();

    /**
     * Retrieves the absolute URI for the given relative path based on the provided SDK configuration.
     * For path /my/endpoint, the result URI will be https://<host>:<port>/my/endpoint.
     *
     * @param relativePath the relative path to the resource.
     * @return the absolute URI.
     */
    URI getURI(String relativePath);

    /**
     * Retrieves the prefix configured for the MAG based on the provided configuration to the SDK.
     *
     * @return the prefix configured for MAG
     */
    String getPrefix();


    /**
     * Retrieves the Authentication Providers from the server.
     * Authentication providers will not be retrieved if the user is already authenticated.
     */
    AuthenticationProvider getAuthenticationProvider() throws Exception;

}
