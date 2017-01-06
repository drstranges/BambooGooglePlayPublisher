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

import com.google.api.client.http.AbstractInputStreamContent;
import com.google.api.client.http.FileContent;
import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.api.services.androidpublisher.AndroidPublisher;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Apks.Upload;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Commit;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Insert;
import com.google.api.services.androidpublisher.AndroidPublisher.Edits.Tracks.Update;
import com.google.api.services.androidpublisher.model.Apk;
import com.google.api.services.androidpublisher.model.AppEdit;
import com.google.api.services.androidpublisher.model.Track;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uploads an apk to the alpha track.
 */
public class UploadApkUtils {

    private static final Log log = LogFactory.getLog(UploadApkUtils.class);

    /**
     * @param applicationName     The name of your application. If the application name is
     *                            {@code null} or blank, the application will log a warning. Suggested
     *                            format is "MyCompany-Application/1.0".
     * @param packageName         the package name of the app
     * @param serviceAccountEmail the service account email
     * @param p12KeyPath          the service account key.p12 file path
     * @param apkPath             the apk file path of the apk to upload
     * @param track               The track for uploading the apk, can be 'alpha', beta', 'production' or 'rollout'
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static void uploadApk(String applicationName, final String packageName, String serviceAccountEmail, String p12KeyPath, String apkPath, final String track) throws IOException, GeneralSecurityException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName), "applicationName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(packageName), "packageName cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(serviceAccountEmail), "serviceAccountEmail cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(p12KeyPath), "p12KeyPath cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(apkPath), "apkPath cannot be null or empty!");
        Preconditions.checkArgument(!Strings.isNullOrEmpty(track), "track cannot be null or empty!");

        // Create the API service.
        AndroidPublisher service = AndroidPublisherHelper.init(applicationName, serviceAccountEmail, p12KeyPath);
        final Edits edits = service.edits();

        // Create a new edit to make changes to your listing.
        Insert editRequest = edits.insert(packageName, null /* no content */);
        AppEdit edit = editRequest.execute();
        final String editId = edit.getId();
        log.info(String.format("Created edit with id: %s", editId));

        // Upload new apk to developer console
        final AbstractInputStreamContent apkFile =
                new FileContent(AndroidPublisherHelper.MIME_TYPE_APK, new File(apkPath));
        Upload uploadRequest = edits
                .apks()
                .upload(packageName, editId, apkFile);
        Apk apk = uploadRequest.execute();
        log.info(String.format("Version code %d has been uploaded", apk.getVersionCode()));

        // Assign apk to the track.
        List<Integer> apkVersionCodes = new ArrayList<Integer>();
        apkVersionCodes.add(apk.getVersionCode());
        Update updateTrackRequest = edits
                .tracks()
                .update(packageName,
                        editId,
                        track,
                        new Track().setVersionCodes(apkVersionCodes));
        Track updatedTrack = updateTrackRequest.execute();
        log.info(String.format("Track %s has been updated.", updatedTrack.getTrack()));

        // Commit changes for edit.
        Commit commitRequest = edits.commit(packageName, editId);
        AppEdit appEdit = commitRequest.execute();
        String appEditId = appEdit.getId();
        log.info(String.format("App edit with id %s has been committed", appEditId));
        log.info(String.format("\nPUBLISH \"%s\" SUCCESSFUL\n", track));
    }
}
