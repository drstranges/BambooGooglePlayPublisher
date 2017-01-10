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
import com.atlassian.bamboo.task.*;
import com.drextended.gppublisher.bamboo.util.UploadApkUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class GPPBuildTask implements TaskType {

    @NotNull
    public TaskResult execute(final TaskContext taskContext) throws TaskException {
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext).failed(); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String applicationName = taskContext.getConfigurationMap().get("applicationName");
        final String packageName = taskContext.getConfigurationMap().get("packageName");
        final String jsonKeyPath = taskContext.getConfigurationMap().get("jsonKeyPath");
        final String apkPath = taskContext.getConfigurationMap().get("apkPath");
        final String track = taskContext.getConfigurationMap().get("track");
        final String deobfuscationFilePath = taskContext.getConfigurationMap().get("deobfuscationFilePath");
        final String recentChangesListings = taskContext.getConfigurationMap().get("recentChangesListings");

        buildLogger.addBuildLogEntry("Start deploy task for app " + applicationName);

        try {
            UploadApkUtils.uploadApk(buildLogger,
                    applicationName,
                    packageName,
                    jsonKeyPath,
                    apkPath,
                    deobfuscationFilePath,
                    recentChangesListings,
                    track);
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