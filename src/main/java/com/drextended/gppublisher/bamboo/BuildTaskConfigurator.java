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

/**
 * Created by Romka on 06.01.2017.
 */
public class BuildTaskConfigurator extends AbstractTaskConfigurator {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String PACKAGE_NAME = "packageName";
    public static final String JSON_KEY_PATH = "jsonKeyPath";
    public static final String APK_PATH = "apkPath";
    public static final String TRACK = "track";
    public static final String TRACK_TYPES = "trackTypes";

    public static final String TRACK_ALPHA = "alpha";
    public static final String TRACK_BETA = "beta";
    public static final String TRACK_PRODUCTION = "production";
    public static final String TRACK_ROLLOUT = "rollout";
    private static final Map<String, String> TRACK_MAP = ImmutableMap.<String, String> builder()
            .put(TRACK_ALPHA, TRACK_ALPHA)
            .put(TRACK_BETA, TRACK_BETA)
            .put(TRACK_PRODUCTION, TRACK_PRODUCTION)
            .put(TRACK_ROLLOUT, TRACK_ROLLOUT)
            .build();

    public static final String DEFAULT_TRACK = TRACK_ALPHA;

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APPLICATION_NAME, params.getString(APPLICATION_NAME));
        config.put(PACKAGE_NAME, params.getString(PACKAGE_NAME));
        config.put(JSON_KEY_PATH, params.getString(JSON_KEY_PATH));
        config.put(APK_PATH, params.getString(APK_PATH));
        config.put(TRACK, params.getString(TRACK));

        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, DEFAULT_TRACK);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put(APPLICATION_NAME, taskDefinition.getConfiguration().get(APPLICATION_NAME));
        context.put(PACKAGE_NAME, taskDefinition.getConfiguration().get(PACKAGE_NAME));
        context.put(JSON_KEY_PATH, taskDefinition.getConfiguration().get(JSON_KEY_PATH));
        context.put(APK_PATH, taskDefinition.getConfiguration().get(APK_PATH));
        context.put(TRACK, taskDefinition.getConfiguration().get(TRACK));
        context.put(TRACK_TYPES, TRACK_MAP);
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, APPLICATION_NAME);
        validateNotEmpty(params, errorCollection, PACKAGE_NAME);
        validateNotEmpty(params, errorCollection, JSON_KEY_PATH);
        validateNotEmpty(params, errorCollection, APK_PATH);
        validateNotEmpty(params, errorCollection, TRACK);
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value)) {
            errorCollection.addError(key, "This field can't be empty");
        } else if (APK_PATH.equals(key) && !value.endsWith(".apk")) {
            errorCollection.addError(key, "Should be path to *.apk file");
        }
    }
}
