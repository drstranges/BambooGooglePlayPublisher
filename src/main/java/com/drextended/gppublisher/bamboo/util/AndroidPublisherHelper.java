/*
 *  Copyright Roman Donchenko. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.drextended.gppublisher.bamboo.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link AndroidPublisherHelper#init(String, String, String)} method. This will run
 * all precondition checks.
 * </p>
 * @
 */
public class AndroidPublisherHelper {

    private static final Log log = LogFactory.getLog(AndroidPublisherHelper.class);

    static final String MIME_TYPE_APK = "application/vnd.android.package-archive";

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Authorizes using service account
     *
     * @param serviceAccountEmail Service account email
     * @param p12KeyPath          Path to the private key file
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static Credential authorizeWithServiceAccount(String serviceAccountEmail, String p12KeyPath)
            throws GeneralSecurityException, IOException {
        log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail));

        // Build service account credential.
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(HTTP_TRANSPORT)
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(serviceAccountEmail)
                .setServiceAccountScopes(
                        Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER))
                .setServiceAccountPrivateKeyFromP12File(new File(p12KeyPath))
                .build();
        return credential;
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName     the name of the application
     * @param serviceAccountEmail the Service Account Email
     * @param p12KeyPath          Path to the private key file
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    protected static AndroidPublisher init(String applicationName, String serviceAccountEmail, String p12KeyPath) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
                "applicationName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceAccountEmail),
                "serviceAccountEmail cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(p12KeyPath),
                "p12KeyPath cannot be null or empty!");

        // Authorization.
        newTrustedTransport();
        Credential credential = authorizeWithServiceAccount(serviceAccountEmail, p12KeyPath);

        // Set up and return API client.
        return new AndroidPublisher.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(applicationName)
                .build();
    }

    private static void newTrustedTransport() throws GeneralSecurityException,
            IOException {
        if (null == HTTP_TRANSPORT) {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        }
    }

}