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
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks.Update;
import com.google.api.services.androidpublisher.model.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uploads an apk to the alpha track.
 */
public class UploadApkUtils {

    /**
     * @param buildLogger
     * @param applicationName       The name of your application. If the application name is
     *                              {@code null} or blank, the application will log a warning. Suggested
     *                              format is "MyCompany-Application/1.0".
     * @param packageName           the package name of the app
     * @param jsonKeyPath           the service account secret json file path
     * @param apkPath               the apk file path of the apk to upload
     * @param deobfuscationFilePath the deobfuscation file of the specified APK
     * @param recentChangesListings the recent changes in format: [BCP47 Language Code]:[recent changes file path].
     *                              Multiple listing thought comma. Sample: en-US:C:\temp\listing_en.txt
     * @param track                 The track for uploading the apk, can be 'alpha', beta', 'production' or 'rollout'
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void uploadApk(BuildLogger buildLogger, String applicationName, final String packageName, String jsonKeyPath, String apkPath, String deobfuscationFilePath, String recentChangesListings, final String track) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName), "applicationName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(packageName), "packageName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(jsonKeyPath), "jsonKeyPath cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(apkPath), "apkPath cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(track), "track cannot be null or empty!");

        buildLogger.addBuildLogEntry("Create the API service.");
        AndroidPublisher service = AndroidPublisherHelper.init(buildLogger, applicationName, jsonKeyPath);
        final Edits edits = service.edits();

        buildLogger.addBuildLogEntry("Create a new edit to make changes to your listing.");
        Insert editRequest = edits.insert(packageName, null);
        AppEdit edit = editRequest.execute();
        final String editId = edit.getId();
        buildLogger.addBuildLogEntry(String.format("Created edit with id: %s", editId));

        buildLogger.addBuildLogEntry("Uploading new apk to developer console...");
        final AbstractInputStreamContent apkFile =
                new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, new File(apkPath));
        Edits.Apks.Upload uploadRequest = edits
                .apks()
                .upload(packageName, editId, apkFile);
        Apk apk = uploadRequest.execute();
        buildLogger.addBuildLogEntry(String.format("Version code %s has been uploaded", apk.getVersionCode()));

        if (!Strings.isNullOrEmpty(deobfuscationFilePath)) {
            buildLogger.addBuildLogEntry("Uploading new mapping file to developer console...");
            final AbstractInputStreamContent deobfuscationFile =
                    new FileContent("application/octet-stream", new File(deobfuscationFilePath));
            Edits.Deobfuscationfiles.Upload dfUploadRequest = edits
                    .deobfuscationfiles()
                    .upload(packageName, editId, apk.getVersionCode(), "proguard", deobfuscationFile);
            DeobfuscationFilesUploadResponse deobfuscationFilesUploadResponse =
                    dfUploadRequest.execute();
            buildLogger.addBuildLogEntry("Mapping has been uploaded " + deobfuscationFilesUploadResponse);
        }

        if (!Strings.isNullOrEmpty(recentChangesListings)) {

            String[] rcParts = recentChangesListings.split(",");
            for (String rcPart : rcParts) {
                String[] rcPieces = rcPart.split(":");
                if (rcPieces.length < 2) {
                    buildLogger.addBuildLogEntry("Wrong recent changes entry: " + rcPart);
                    continue;
                }
                String languageCode = rcPieces[0];
                String recentChangesFilePath = rcPieces[1];
                if (Strings.isNullOrEmpty(languageCode) || Strings.isNullOrEmpty(recentChangesFilePath)) {
                    buildLogger.addBuildLogEntry("Wrong recent changes entry: " + rcPart + ", lang = " + languageCode +
                            ", path = " + recentChangesFilePath);
                    continue;
                }
                buildLogger.addBuildLogEntry("Update recent changes listing ...");
                FileInputStream inputStream = new FileInputStream(recentChangesFilePath);
                String recentChanges = null;
                try {
                    recentChanges = IOUtils.toString(inputStream);
                } finally {
                    inputStream.close();
                }
                if (Strings.isNullOrEmpty(recentChanges)) {
                    buildLogger.addBuildLogEntry("Not found recent changes for language: " + languageCode);
                    continue;
                }

                ApkListing apkListing = new ApkListing();
                apkListing.setLanguage(languageCode);
                apkListing.setRecentChanges(recentChanges);

                Edits.Apklistings.Update apkListingRequest = edits
                        .apklistings()
                        .update(packageName, editId, apk.getVersionCode(), languageCode, apkListing);
                apkListingRequest.execute();

                buildLogger.addBuildLogEntry("Recent Changes has been uploaded for language: " + languageCode);
            }
        }

        buildLogger.addBuildLogEntry("Assigning apk to the " + track + " track");
        List<Integer> apkVersionCodes = new ArrayList<Integer>();
        apkVersionCodes.add(apk.getVersionCode());
        Update updateTrackRequest = edits
                .tracks()
                .update(packageName,
                        editId,
                        track,
                        new Track().setVersionCodes(apkVersionCodes));
        Track updatedTrack = updateTrackRequest.execute();
        buildLogger.addBuildLogEntry(String.format("Track \"%s\" has been updated.", updatedTrack.getTrack()));

        buildLogger.addBuildLogEntry("Commit changes for edit.");
        Commit commitRequest = edits.commit(packageName, editId);
        AppEdit appEdit = commitRequest.execute();
        String appEditId = appEdit.getId();
        buildLogger.addBuildLogEntry(String.format("App edit with id %s has been committed!", appEditId));
        buildLogger.addBuildLogEntry("==========\n\n PUBLISHED SUCCESSFUL \n\n==========");
    }
}
