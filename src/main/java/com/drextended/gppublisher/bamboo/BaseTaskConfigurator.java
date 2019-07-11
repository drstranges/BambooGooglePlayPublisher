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
package com.drextended.gppublisher.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static com.drextended.gppublisher.bamboo.util.AndroidPublisherHelper.*;

/**
 * Created by Romka on 06.01.2017.
 */
public class BaseTaskConfigurator extends AbstractTaskConfigurator {

    public static final String APPLICATION_NAME = "applicationName";
    public static final String PACKAGE_NAME = "packageName";
    public static final String JSON_KEY_PATH = "jsonKeyPath";
    public static final String JSON_KEY_CONTENT = "jsonKeyContent";
    public static final String FIND_JSON_KEY_IN_FILE = "findJsonKeyInFile";
    public static final String APK_PATH = "apkPath";
    public static final String DEOBFUSCATION_FILE_PATH = "deobfuscationFilePath";
    public static final String RECENT_CHANGES_LISTINGS = "recentChangesListings";
//    public static final String APK_ARTIFACT = "apkArtifact";
//    public static final String APK_ARTIFACT_LIST = "apkArtifactList";
    public static final String TRACK = "track";
    public static final String TRACK_TYPES = "trackTypes";

    public static final String TRACK_CUSTOM_NAMES = "trackCustomNames";

    public static final String ROLLOUT_FRACTION = "rolloutFraction";
    public static final String ROLLOUT_FRACTION_DEFAULT = "0.1"; // Acceptable values are 0.05, 0.1, 0.2, and 0.5
    private static final Map<String, String> TRACK_MAP = ImmutableMap.<String, String>builder()
            .put(TRACK_NONE, TRACK_NONE)
            .put(TRACK_INTERNAL, TRACK_INTERNAL)
            .put(TRACK_ALPHA, TRACK_ALPHA)
            .put(TRACK_BETA, TRACK_BETA)
            .put(TRACK_PRODUCTION, TRACK_PRODUCTION)
            .put(TRACK_ROLLOUT, TRACK_ROLLOUT)
            .put(TRACK_CUSTOM, TRACK_CUSTOM)
            .build();

    public static final String DEFAULT_TRACK = TRACK_INTERNAL;
    public static final String DEFAULT_CUSTOM_TRACK_NAMES = TRACK_INTERNAL + ", custom1, custom2";

    public BaseTaskConfigurator() {
    }

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APPLICATION_NAME, params.getString(APPLICATION_NAME));
        config.put(PACKAGE_NAME, params.getString(PACKAGE_NAME));
        config.put(FIND_JSON_KEY_IN_FILE, params.getString(FIND_JSON_KEY_IN_FILE));
        config.put(JSON_KEY_PATH, params.getString(JSON_KEY_PATH));
        config.put(JSON_KEY_CONTENT, params.getString(JSON_KEY_CONTENT));
        config.put(APK_PATH, params.getString(APK_PATH));
        config.put(DEOBFUSCATION_FILE_PATH, params.getString(DEOBFUSCATION_FILE_PATH));
        config.put(RECENT_CHANGES_LISTINGS, params.getString(RECENT_CHANGES_LISTINGS));
        config.put(TRACK, params.getString(TRACK));
        config.put(TRACK_CUSTOM_NAMES, params.getString(TRACK_CUSTOM_NAMES));
        config.put(ROLLOUT_FRACTION, params.getString(ROLLOUT_FRACTION));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, DEFAULT_TRACK);
        context.put(TRACK_CUSTOM_NAMES, DEFAULT_CUSTOM_TRACK_NAMES);
        context.put(FIND_JSON_KEY_IN_FILE, false);
        context.put(ROLLOUT_FRACTION, ROLLOUT_FRACTION_DEFAULT);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put(APPLICATION_NAME, taskDefinition.getConfiguration().get(APPLICATION_NAME));
        context.put(PACKAGE_NAME, taskDefinition.getConfiguration().get(PACKAGE_NAME));
        context.put(FIND_JSON_KEY_IN_FILE, taskDefinition.getConfiguration().get(FIND_JSON_KEY_IN_FILE));
        context.put(JSON_KEY_CONTENT, taskDefinition.getConfiguration().get(JSON_KEY_CONTENT));
        context.put(JSON_KEY_PATH, taskDefinition.getConfiguration().get(JSON_KEY_PATH));
        context.put(APK_PATH, taskDefinition.getConfiguration().get(APK_PATH));
        context.put(DEOBFUSCATION_FILE_PATH, taskDefinition.getConfiguration().get(DEOBFUSCATION_FILE_PATH));
        context.put(RECENT_CHANGES_LISTINGS, taskDefinition.getConfiguration().get(RECENT_CHANGES_LISTINGS));
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, taskDefinition.getConfiguration().get(TRACK));
        String fraction = taskDefinition.getConfiguration().get(ROLLOUT_FRACTION);
        context.put(ROLLOUT_FRACTION, fraction != null ? fraction : ROLLOUT_FRACTION_DEFAULT);
        context.put(TRACK_CUSTOM_NAMES, taskDefinition.getConfiguration().get(TRACK_CUSTOM_NAMES));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, APPLICATION_NAME);
        validateNotEmpty(params, errorCollection, PACKAGE_NAME);
        if (params.getBoolean(FIND_JSON_KEY_IN_FILE)) {
            validateNotEmpty(params, errorCollection, JSON_KEY_PATH);
        } else {
            validateNotEmpty(params, errorCollection, JSON_KEY_CONTENT);
        }
        validateNotEmpty(params, errorCollection, APK_PATH);
        final String apkPath = params.getString(APK_PATH);
        if (apkPath == null || (!apkPath.endsWith(".apk") && !apkPath.endsWith(".aab"))) {
            errorCollection.addError(APK_PATH, "Should be path to *.apk or *.aab file");
        }

        validateNotEmpty(params, errorCollection, TRACK);
        String track = params.getString(TRACK);
        if (TRACK_ROLLOUT.equals(track)) {
            String rolloutFraction = params.getString(ROLLOUT_FRACTION);
            if (StringUtils.isEmpty(rolloutFraction)) {
                errorCollection.addError(rolloutFraction, "This field can't be empty");
            } else {
                try {
                    //noinspection ConstantConditions
                    double fraction = Double.parseDouble(rolloutFraction);
                    if (fraction < 0 || fraction >= 1) {
                        errorCollection.addError(ROLLOUT_FRACTION, "User fraction must be in range (0 <= fraction < 1)");
                    }
                } catch (NumberFormatException ex) {
                    errorCollection.addError(ROLLOUT_FRACTION, "User fraction cannot be parsed as double");
                }
            }
        } else if (TRACK_CUSTOM.equals(track)) {
            validateNotEmpty(params, errorCollection, TRACK_CUSTOM_NAMES);
        }
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value)) {
            errorCollection.addError(key, "This field can't be empty");
        }
    }

}
