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

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.deployments.execution.DeploymentTaskContext;
import com.atlassian.bamboo.deployments.execution.DeploymentTaskType;
import com.atlassian.bamboo.task.TaskException;
import com.atlassian.bamboo.task.TaskResult;
import com.atlassian.bamboo.task.TaskResultBuilder;
import com.drextended.gppublisher.bamboo.util.AndroidPublisherHelper;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static com.drextended.gppublisher.bamboo.BaseTaskConfigurator.*;

public class GPPDeploymentTask implements DeploymentTaskType {

    @NotNull
    public TaskResult execute(@NotNull DeploymentTaskContext taskContext) throws TaskException {
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext).failed(); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String applicationName = taskContext.getConfigurationMap().get(APPLICATION_NAME);
        final String packageName = taskContext.getConfigurationMap().get(PACKAGE_NAME);
        final String jsonKeyPath = taskContext.getConfigurationMap().get(JSON_KEY_PATH);
        final String jsonKeyContent = taskContext.getConfigurationMap().get(JSON_KEY_CONTENT);
        final boolean findJsonKeyInFile = taskContext.getConfigurationMap().getAsBoolean(FIND_JSON_KEY_IN_FILE);
        final String apkPath = taskContext.getConfigurationMap().get(APK_PATH);
        final String deobfuscationFilePath = taskContext.getConfigurationMap().get(DEOBFUSCATION_FILE_PATH);
        final String recentChangesListings = taskContext.getConfigurationMap().get(RECENT_CHANGES_LISTINGS);
        final String track = taskContext.getConfigurationMap().get(TRACK);
        final String rolloutFraction = taskContext.getConfigurationMap().get(ROLLOUT_FRACTION);
        final String trackCustomNames = taskContext.getConfigurationMap().get(TRACK_CUSTOM_NAMES);

        buildLogger.addBuildLogEntry("Start deploy task for app " + applicationName);

        try {
            AndroidPublisherHelper helper = new AndroidPublisherHelper(
                    taskContext.getWorkingDirectory(),
                    buildLogger,
                    applicationName,
                    packageName,
                    findJsonKeyInFile,
                    jsonKeyPath,
                    jsonKeyContent,
                    apkPath,
                    deobfuscationFilePath,
                    recentChangesListings,
                    track,
                    rolloutFraction,
                    trackCustomNames
            );
            helper.init();
            helper.makeInsertRequest();

            builder.success();
        } catch (IOException ex) {
            buildLogger.addBuildLogEntry("Exception: " + ex.getMessage());
            builder.failed();
        } catch (GeneralSecurityException ex) {
            builder.failed();
            buildLogger.addBuildLogEntry("Exception: " + ex.getMessage());
        }

        return builder.build();
    }
}