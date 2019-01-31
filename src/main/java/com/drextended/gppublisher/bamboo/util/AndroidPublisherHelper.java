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
import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisherScopes;
import com.google.api.services.androidpublisher.model.*;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Helper class to initialize the publisher APIs client library.
 * <p>
 * Before making any calls to the API through the client library you need to
 * call the {@link #init()} method.
 * This will run all precondition checks.
 * </p>
 */
public class AndroidPublisherHelper {

    static final String MIME_TYPE_APK = "application/vnd.android.package-archive";
    public static final String TRACK_NONE = "none";
    public static final String TRACK_INTERNAL = "internal";
    public static final String TRACK_ALPHA = "alpha";
    public static final String TRACK_BETA = "beta";
    public static final String TRACK_PRODUCTION = "production";
    public static final String TRACK_ROLLOUT = "rollout";
    public static final String TRACK_CUSTOM = "custom";

    private final File mWorkingDirectory;
    private final BuildLogger mLogger;
    private final String mApplicationName;
    private final String mPackageName;
    private final boolean mFindJsonKeyInFile;
    private final String mJsonKeyPath;
    private final String mJsonKeyContent;
    private final String mApkPath;
    private final String mDeobfuscationFilePath;
    private final String mRecentChangesListings;
    private final String mTrack;
    private final String mRolloutFractionString;
    private String mTrackCustomNames;

    private AndroidPublisher mAndroidPublisher;
    private File mApkFile;
    private File mDeobfuscationFile;
    private List<LocalizedText> mReleaseNotes;
    private Double mRolloutFraction;
    private String[] mCustomTracks;

    /**
     * @param workingDirectory
     * @param buildLogger
     * @param applicationName       The name of your application. If the application name is
     *                              {@code null} or blank, the application will log a warning. Suggested
     *                              format is "MyCompany-Application/1.0".
     * @param packageName           the package name of the app
     * @param findJsonKeyInFile
     * @param jsonKeyPath           the service account secret json file path
     * @param apkPath               the apk file path of the apk to upload
     * @param deobfuscationFilePath the deobfuscation file of the specified APK
     * @param recentChangesListings the recent changes in format: [BCP47 Language Code]:[recent changes file path].
     *                              Multiple listing thought comma. Sample: en-US:C:\temp\listing_en.txt
     * @param track                 The track for uploading the apk, can be 'alpha', beta', 'production' or 'rollout'
     * @param rolloutFraction       The rollout fraction
     * @param trackCustomNames      Comma separated track names for `custom` track
     */
    public AndroidPublisherHelper(
            File workingDirectory,
            BuildLogger buildLogger,
            String applicationName,
            String packageName,
            boolean findJsonKeyInFile,
            String jsonKeyPath,
            String jsonKeyContent,
            String apkPath,
            String deobfuscationFilePath,
            String recentChangesListings,
            String track,
            String rolloutFraction,
            String trackCustomNames
    ) {
        mWorkingDirectory = workingDirectory;
        mLogger = buildLogger;
        mApplicationName = applicationName;
        mPackageName = packageName;
        mFindJsonKeyInFile = findJsonKeyInFile;
        mJsonKeyPath = jsonKeyPath;
        mJsonKeyContent = jsonKeyContent;
        mApkPath = apkPath;
        mDeobfuscationFilePath = deobfuscationFilePath;
        mRecentChangesListings = recentChangesListings;
        mTrack = track;
        mRolloutFractionString = rolloutFraction;
        mTrackCustomNames = trackCustomNames;
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public void init() throws IOException, GeneralSecurityException, IllegalArgumentException {
        mLogger.addBuildLogEntry("Initializing...");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mApplicationName), "Application name cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mPackageName), "Package name cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mTrack), "Track cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(mApkPath), "Apk path cannot be null or empty!");

        if (TRACK_ROLLOUT.equals(mTrack)) {
            try {
                mRolloutFraction = Double.parseDouble(mRolloutFractionString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("User fraction cannot be parsed as double: " + mRolloutFractionString);
            }
        } else if (TRACK_CUSTOM.equals(mTrack)) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(mTrackCustomNames), "Not specified names for custom tracks!");
            mCustomTracks = mTrackCustomNames.split(",\\s*");
        }

        String apkFullPath = relativeToFullPath(mApkPath);
        mApkFile = new File(apkFullPath);
        Preconditions.checkArgument(mApkFile.exists(), "Apk file not found in path: " + apkFullPath);
        if (!Strings.isNullOrEmpty(mDeobfuscationFilePath)) {
            String deobfuscationFullPath = relativeToFullPath(mDeobfuscationFilePath);
            mDeobfuscationFile = new File(deobfuscationFullPath);
            Preconditions.checkArgument(mDeobfuscationFile.exists(), "Mapping (deobfuscation) file not found in path: " + deobfuscationFullPath);
        }

        final InputStream jsonKeyInputStream;
        if (mFindJsonKeyInFile) {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(mJsonKeyPath), "Secret json key path cannot be null or empty!");
            String jsonKeyFullPath = relativeToFullPath(mJsonKeyPath);
            File jsonKeyFile = new File(jsonKeyFullPath);
            Preconditions.checkArgument(jsonKeyFile.exists(), "Secret json key file not found in path: " + jsonKeyFullPath);
            jsonKeyInputStream = new FileInputStream(jsonKeyFile);
        } else {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(mJsonKeyContent), "Secret json key content cannot be null or empty!");
            jsonKeyInputStream = IOUtils.toInputStream(mJsonKeyContent);
        }

        if (!Strings.isNullOrEmpty(mRecentChangesListings)) {
            String[] rcParts = mRecentChangesListings.trim().split("\\s*,\\s*");
            mReleaseNotes = new ArrayList<LocalizedText>(rcParts.length);
            for (String rcPart : rcParts) {
                String[] rcPieces = rcPart.split("\\s*::\\s*");

                Preconditions.checkArgument(rcPieces.length == 2, "Wrong recent changes entry: " + rcPart);

                String languageCode = rcPieces[0];
                String recentChangesFilePath = relativeToFullPath(rcPieces[1]);
                Preconditions.checkArgument(!Strings.isNullOrEmpty(languageCode) && !Strings.isNullOrEmpty(recentChangesFilePath),
                        "Wrong recent changes entry: " + rcPart + ", lang = " + languageCode + ", path = " + recentChangesFilePath);

                File rcFile = new File(recentChangesFilePath);
                Preconditions.checkArgument(rcFile.exists(),
                        "Recent changes file for language \"" + languageCode + "\" not found in path: " + recentChangesFilePath);

                FileInputStream inputStream = new FileInputStream(rcFile);
                String recentChanges = null;
                try {
                    recentChanges = IOUtils.toString(inputStream);
                } finally {
                    inputStream.close();
                }

                mReleaseNotes.add(
                        new LocalizedText().setLanguage(languageCode).setText(recentChanges)
                );
            }
        }
        mLogger.addBuildLogEntry("Initialized successfully!");

        mLogger.addBuildLogEntry("Creating AndroidPublisher Api Service...");
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = GoogleCredential.fromStream(jsonKeyInputStream, httpTransport, jsonFactory)
                .createScoped(Collections.singleton(AndroidPublisherScopes.ANDROIDPUBLISHER));
        mAndroidPublisher = new AndroidPublisher.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(mApplicationName)
                .build();
        mLogger.addBuildLogEntry("AndroidPublisher Api Service created!");
    }

    private String relativeToFullPath(String path) {
        if (path != null && !new File(path).isAbsolute()) {
            return new File(mWorkingDirectory, path).getAbsolutePath();
        }
        return path;
    }

    /**
     * Publishes apk file on Google Play
     * @throws IOException
     * @throws GeneralSecurityException
     * @throws IllegalArgumentException
     */
    public void makeInsertRequest() throws IOException, GeneralSecurityException, IllegalArgumentException {
        Preconditions.checkArgument(mApkFile != null && mApkFile.exists(), "Apk file not found in path: " + mApkPath);

        mLogger.addBuildLogEntry("Creating a new edit session...");
        final AndroidPublisher.Edits edits = mAndroidPublisher.edits();
        AndroidPublisher.Edits.Insert editRequest = edits.insert(mPackageName, null);
        AppEdit edit = editRequest.execute();
        final String editId = edit.getId();
        mLogger.addBuildLogEntry(String.format("Created edit session with id: %s", editId));

        mLogger.addBuildLogEntry("Uploading new apk file...");
        final AbstractInputStreamContent apkFile = new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, mApkFile);
        Apk apk = edits.apks()
                .upload(mPackageName, editId, apkFile)
                .execute();
        Integer apkVersionCode = apk.getVersionCode();
        mLogger.addBuildLogEntry(String.format("Apk file with version code %s has been uploaded!", apkVersionCode));

        if (mDeobfuscationFile != null) {
            mLogger.addBuildLogEntry("Uploading new mapping file...");
            Preconditions.checkArgument(mDeobfuscationFile.exists(), "Mapping (deobfuscation) file not found in path: " + mDeobfuscationFilePath);
            final AbstractInputStreamContent deobfuscationFile = new FileContent("application/octet-stream", mDeobfuscationFile);
            edits.deobfuscationfiles()
                    .upload(mPackageName, editId, apkVersionCode, "proguard", deobfuscationFile)
                    .execute();
            mLogger.addBuildLogEntry("Mapping has been uploaded!");
        }

        if (TRACK_NONE.equals(mTrack)) {
            mLogger.addBuildLogEntry("Track was not set, so apk will not be assigned to any track...");
        } else if (TRACK_CUSTOM.equals(mTrack)){
            for (String customTrack : mCustomTracks) {
                assignToTrack(edits, editId, apkVersionCode, customTrack);
            }
        } else {
            assignToTrack(edits, editId, apkVersionCode, mTrack);
        }
        mLogger.addBuildLogEntry("Committing changes for edit...");
        AppEdit appEdit = edits.commit(mPackageName, editId)
                .execute();
        mLogger.addBuildLogEntry(String.format("App edit with id %s has been committed!", appEdit.getId()));
        mLogger.addBuildLogEntry("=\n\n==================\n\n PUBLISHED SUCCESSFUL \n\n==================\n\n");
    }

    private void assignToTrack(AndroidPublisher.Edits edits, String editId, Integer apkVersionCode, String trackName) throws IOException {
        mLogger.addBuildLogEntry("Assigning release to the track: " + trackName);

        TrackRelease release = new TrackRelease()
                .setVersionCodes(Collections.singletonList(Long.valueOf(apkVersionCode)))
                .setStatus("completed")
                .setReleaseNotes(mReleaseNotes);

        if (TRACK_ROLLOUT.equals(trackName)) {
            release = release.setUserFraction(mRolloutFraction);
        }

        Track trackContent = new Track()
                .setTrack(trackName)
                .setReleases(Collections.singletonList(release));

        edits.tracks()
                .update(mPackageName, editId, trackName, trackContent)
                .execute();

        mLogger.addBuildLogEntry("Release successfully assigning to the track: " + trackName);
    }
}