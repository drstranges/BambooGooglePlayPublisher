package com.drextended.gppublisher.bamboo;

import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by Romka on 06.01.2017.
 */
public class TaskConfigurator extends AbstractTaskConfigurator {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String PACKAGE_NAME = "packageName";
    public static final String SERVICE_ACCOUNT_EMAIL = "serviceAccountEmail";
    public static final String P12_KEY_PATH = "p12KeyPath";
    public static final String APK_PATH = "apkPath";
    public static final String TRACK = "track";

    public static final String DEFAULT_TRACK = "alpha";

    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APPLICATION_NAME, params.getString(APPLICATION_NAME));
        config.put(PACKAGE_NAME, params.getString(PACKAGE_NAME));
        config.put(SERVICE_ACCOUNT_EMAIL, params.getString(SERVICE_ACCOUNT_EMAIL));
        config.put(P12_KEY_PATH, params.getString(P12_KEY_PATH));
        config.put(APK_PATH, params.getString(APK_PATH));
        config.put(TRACK, params.getString(TRACK));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TRACK, DEFAULT_TRACK);
    }

    @Override
    public void populateContextForEdit(@NotNull final Map<String, Object> context, @NotNull final TaskDefinition taskDefinition) {
        super.populateContextForEdit(context, taskDefinition);
        context.put(APPLICATION_NAME, taskDefinition.getConfiguration().get(APPLICATION_NAME));
        context.put(PACKAGE_NAME, taskDefinition.getConfiguration().get(PACKAGE_NAME));
        context.put(SERVICE_ACCOUNT_EMAIL, taskDefinition.getConfiguration().get(SERVICE_ACCOUNT_EMAIL));
        context.put(P12_KEY_PATH, taskDefinition.getConfiguration().get(P12_KEY_PATH));
        context.put(APK_PATH, taskDefinition.getConfiguration().get(APK_PATH));
        context.put(TRACK, taskDefinition.getConfiguration().get(TRACK));
    }

    @Override
    public void validate(@NotNull final ActionParametersMap params, @NotNull final ErrorCollection errorCollection) {
        super.validate(params, errorCollection);

        validateNotEmpty(params, errorCollection, APPLICATION_NAME);
        validateNotEmpty(params, errorCollection, PACKAGE_NAME);
        validateNotEmpty(params, errorCollection, SERVICE_ACCOUNT_EMAIL);
        validateNotEmpty(params, errorCollection, P12_KEY_PATH);
        validateNotEmpty(params, errorCollection, APK_PATH);
        validateNotEmpty(params, errorCollection, TRACK);
    }

    private void validateNotEmpty(@NotNull ActionParametersMap params, @NotNull final ErrorCollection errorCollection, @NotNull String key) {
        final String value = params.getString(key);
        if (StringUtils.isEmpty(value)) {
            errorCollection.addError(key, "This field can't be empty");
        }
    }
}
