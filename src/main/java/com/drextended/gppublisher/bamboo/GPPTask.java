package com.drextended.gppublisher.bamboo;

import com.atlassian.bamboo.build.logger.BuildLogger;
import com.atlassian.bamboo.task.*;
import org.jetbrains.annotations.NotNull;

public class GPPTask implements TaskType {

    @NotNull
    public TaskResult execute(final TaskContext taskContext) throws TaskException {
        final TaskResultBuilder builder = TaskResultBuilder.newBuilder(taskContext).failed(); //Initially set to Failed.
        final BuildLogger buildLogger = taskContext.getBuildLogger();

        final String applicationName = taskContext.getConfigurationMap().get("applicationName");
        final String packageName = taskContext.getConfigurationMap().get("packageName");
        final String serviceAccountEmail = taskContext.getConfigurationMap().get("serviceAccountEmail");
        final String p12KeyPath = taskContext.getConfigurationMap().get("p12KeyPath");
        final String apkPath = taskContext.getConfigurationMap().get("apkPath");
        final String track = taskContext.getConfigurationMap().get("track");

        buildLogger.addBuildLogEntry("Start deploy task for app " + applicationName);




        if (true) {
            builder.success();
        }

        final TaskResult result = builder.build();

        return result;
    }
}