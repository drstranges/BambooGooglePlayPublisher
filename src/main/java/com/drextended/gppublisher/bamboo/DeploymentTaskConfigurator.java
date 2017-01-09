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

import com.atlassian.bamboo.build.Job;
import com.atlassian.bamboo.collections.ActionParametersMap;
import com.atlassian.bamboo.deployments.DeploymentTaskContextHelper;
import com.atlassian.bamboo.deployments.environments.Environment;
import com.atlassian.bamboo.plan.PlanKeys;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinition;
import com.atlassian.bamboo.plan.artifact.ArtifactDefinitionManager;
import com.atlassian.bamboo.plan.artifact.ImmutableArtifactSubscription;
import com.atlassian.bamboo.plan.cache.CachedPlanManager;
import com.atlassian.bamboo.plan.cache.ImmutableJob;
import com.atlassian.bamboo.plan.cache.ImmutablePlan;
import com.atlassian.bamboo.plugin.ArtifactDownloaderTaskConfigurationHelper;
import com.atlassian.bamboo.plugin.BambooPluginUtils;
import com.atlassian.bamboo.task.AbstractTaskConfigurator;
import com.atlassian.bamboo.task.TaskConfigConstants;
import com.atlassian.bamboo.task.TaskContextHelper;
import com.atlassian.bamboo.task.TaskDefinition;
import com.atlassian.bamboo.util.BambooConstants;
import com.atlassian.bamboo.util.Narrow;
import com.atlassian.bamboo.utils.error.ErrorCollection;
import com.atlassian.bamboo.v2.build.agent.capability.Requirement;
import com.atlassian.bamboo.webwork.util.WwSelectOption;
import com.atlassian.plugin.predicate.PluginKeyPatternsPredicate;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.ApplicationProperties;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Romka on 06.01.2017.
 */
public class DeploymentTaskConfigurator extends AbstractTaskConfigurator {
    public static final String APPLICATION_NAME = "applicationName";
    public static final String PACKAGE_NAME = "packageName";
    public static final String JSON_KEY_PATH = "jsonKeyPath";
    public static final String APK_PATH = "apkPath";
    public static final String APK_ARTIFACT = "apkArtifact";
    public static final String APK_ARTIFACT_LIST = "apkArtifactList";
    public static final String TRACK = "track";
    public static final String TRACK_TYPES = "trackTypes";

    public static final String TRACK_ALPHA = "alpha";
    public static final String TRACK_BETA = "beta";
    public static final String TRACK_PRODUCTION = "production";
    public static final String TRACK_ROLLOUT = "rollout";
    private static final Map<String, String> TRACK_MAP = ImmutableMap.<String, String>builder()
            .put(TRACK_ALPHA, TRACK_ALPHA)
            .put(TRACK_BETA, TRACK_BETA)
            .put(TRACK_PRODUCTION, TRACK_PRODUCTION)
            .put(TRACK_ROLLOUT, TRACK_ROLLOUT)
            .build();

    public static final String DEFAULT_TRACK = TRACK_ALPHA;

//    [@ww.select labelKey="com.drextended.gppublisher.bamboo.apkArtifact" name="apkArtifact" list="apkArtifactList" required='true'/]
//    @ComponentImport
//    private ArtifactDefinitionManager artifactDefinitionManager;
//    @ComponentImport
//    private CachedPlanManager cachedPlanManager;
//
//    @Inject
//    public DeploymentTaskConfigurator(ArtifactDefinitionManager artifactDefinitionManager, CachedPlanManager cachedPlanManager) {
//        this.artifactDefinitionManager = artifactDefinitionManager;
//        this.cachedPlanManager = cachedPlanManager;
//    }

//    @SuppressWarnings("UnusedDeclaration")
//    public void setArtifactDefinitionManager(final ArtifactDefinitionManager artifactDefinitionManager) {
//        this.artifactDefinitionManager = artifactDefinitionManager;
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public void setCachedPlanManager(final CachedPlanManager cachedPlanManager) {
//        this.cachedPlanManager = cachedPlanManager;
//    }


    @NotNull
    @Override
    public Map<String, String> generateTaskConfigMap(@NotNull final ActionParametersMap params, @Nullable final TaskDefinition previousTaskDefinition) {
        final Map<String, String> config = super.generateTaskConfigMap(params, previousTaskDefinition);
        config.put(APPLICATION_NAME, params.getString(APPLICATION_NAME));
        config.put(PACKAGE_NAME, params.getString(PACKAGE_NAME));
        config.put(JSON_KEY_PATH, params.getString(JSON_KEY_PATH));
        config.put(APK_PATH, params.getString(APK_PATH));
        config.put(APK_ARTIFACT, params.getString(APK_ARTIFACT));
        config.put(TRACK, params.getString(TRACK));
        return config;
    }

    @Override
    public void populateContextForCreate(@NotNull final Map<String, Object> context) {
        super.populateContextForCreate(context);
        context.put(TRACK_TYPES, TRACK_MAP);
        context.put(TRACK, DEFAULT_TRACK);

//        context.put(APK_ARTIFACT_LIST, addArtifactData(context, null));
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

        context.put(APK_ARTIFACT, taskDefinition.getConfiguration().get(APK_ARTIFACT));
//        context.put(APK_ARTIFACT_LIST, addArtifactData(context, taskDefinition));
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

    @Override
    public Set<Requirement> calculateRequirements(@NotNull TaskDefinition taskDefinition, @NotNull Job job) {
//        job.getArtifactDefinitions()
        return super.calculateRequirements(taskDefinition, job);
    }

//    private List<WwSelectOption> addArtifactData(Map<String, Object> context, @Nullable TaskDefinition definitionOfTaskBeingEdited) {
//
//        final ImmutableJob job = Narrow.reinterpret(TaskContextHelper.getPlan(context), ImmutableJob.class);
//
//        final List<WwSelectOption> artifactsToDeploy = Lists.newArrayList();
//
////        final String sharedArtifactsGroup = getI18nBean().getText("com.drextended.gppublisher.bamboo.apk.artifact.shared");
//        final String sharedArtifactsGroup = "Shared artifacts";
//
//        if (job != null) {
//            for (ImmutableArtifactSubscription artifactSubscription : job.getArtifactSubscriptions()) {
//                artifactsToDeploy.add(new WwSelectOption(artifactSubscription.getName(), sharedArtifactsGroup,
//                        String.valueOf(artifactSubscription.getId())));
//            }
//
//            addArtifactsFromDownloaderTasks(job.getBuildDefinition().getTaskDefinitions(), definitionOfTaskBeingEdited, artifactsToDeploy);
//        } else {
//            final Environment environment = DeploymentTaskContextHelper.getEnvironment(context);
//            if (environment != null) {
//                addArtifactsFromDownloaderTasks(environment.getTaskDefinitions(), definitionOfTaskBeingEdited, artifactsToDeploy);
//            }
//        }
//
//        return artifactsToDeploy;
//    }

//    private void addArtifactsFromDownloaderTasks(List<TaskDefinition> taskDefinitions,
//                                                 @Nullable TaskDefinition definitionOfTaskBeingEdited, List<WwSelectOption> artifactsToDeploy) {
//
//        for (TaskDefinition task : taskDefinitions) {
//            if (task.equals(definitionOfTaskBeingEdited)) {
//                break;
//            }
//
//            if (task.getPluginKey().equals(BambooPluginUtils.ARTIFACT_DOWNLOAD_TASK_MODULE_KEY) && task.isEnabled()) {
//                final Map<String, String> taskConfiguration = task.getConfiguration();
//                final String sourcePlanKey = ArtifactDownloaderTaskConfigurationHelper.getSourcePlanKey(taskConfiguration);
//                if (sourcePlanKey != null) {
//                    final ImmutablePlan plan = cachedPlanManager.getPlanByKey(PlanKeys.getPlanKey(sourcePlanKey));
//                    if (plan != null) {
//                        final String artifactsFromOtherPlans = "Plan artifacts: " + plan.getName();
//
//                        for (String artifactIdKey : ArtifactDownloaderTaskConfigurationHelper.getArtifactKeys(taskConfiguration)) {
//                            long artifactId = Long.valueOf(taskConfiguration.get(artifactIdKey));
//                            final int transferId = ArtifactDownloaderTaskConfigurationHelper.getIndexFromKey(artifactIdKey);
//
//                            if (artifactId >= 0) {
//                                final ArtifactDefinition artifactDefinition = artifactDefinitionManager.findArtifactDefinition(artifactId);
//                                if (artifactDefinition != null) {
//                                    String artifactName = "Artifact " + sourcePlanKey + ":" + artifactDefinition.getName();
//                                    String selectValue = String.valueOf(artifactDefinition.getId());
////                        String selectValue = AvailableArtifact.fromTransferTask(artifactDefinition.getId(), task,
////                                transferId, artifactDefinition.getName()).toString();
//                                    artifactsToDeploy.add(new WwSelectOption(artifactName, artifactsFromOtherPlans, selectValue));
//                                }
//                            }
//
//                        }
//                    }
//                }
//            }
//        }
//    }

}
