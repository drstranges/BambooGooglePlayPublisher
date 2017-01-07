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

import com.atlassian.bamboo.build.logger.BuildLogger;
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

import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link AndroidPublisherHelper#init(BuildLogger, String, String)} method.
 * This will run all precondition checks.
 * </p>
 */
public class AndroidPublisherHelper {

    static final String MIME_TYPE_APK = "application/vnd.android.package-archive";

    /**
     * Authorizes using service account
     *
     * @param httpTransport
     * @param jsonFactory
     * @param jsonKeyPath   Path to the json key file
     * @return
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private static Credential authorizeWithServiceAccountFromJson(HttpTransport httpTransport, JsonFactory jsonFactory, String jsonKeyPath)
            throws GeneralSecurityException, IOException {

        return GoogleCredential.fromStream(new FileInputStream(jsonKeyPath), httpTransport, jsonFactory)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param buildLogger
     * @param applicationName the name of the application
     * @param jsonKeyPath     Path to the private json key file
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    protected static AndroidPublisher init(BuildLogger buildLogger, String applicationName, String jsonKeyPath) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
                "applicationName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonKeyPath),
                "jsonKeyPath cannot be null or empty!");

        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        buildLogger.addBuildLogEntry("Authorizing using secret json...");
        Credential credential = authorizeWithServiceAccountFromJson(httpTransport, jsonFactory, jsonKeyPath);
        buildLogger.addBuildLogEntry("Authorized successfully");

        // Set up and return API client.
        return new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(applicationName)
                .build();
    }

}