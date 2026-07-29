/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.ccv2.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import sap.commerce.toolset.ai.mcp.McpConstants
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper

class CCv2McpToolset : McpToolset {

    @McpTool(name = "sap_commerce_ccv2_list_subscriptions")
    @McpDescription(
        """Lists all CCv2 subscriptions configured in the plugin's CCv2 application settings.
        |Use this tool first to discover available subscription identifiers before calling other CCv2 tools.
        |Returns a JSON object: {"total", "items": [{"id", "name"}]}.
        |If only one subscription is configured you may omit subscriptionId from other tools — the first is used automatically."""
    )
    suspend fun listSubscriptions(
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val subscriptions = CCv2McpService.getInstance().listSubscriptions()
        return mapper.map(subscriptions)
    }

    @McpTool(name = "sap_commerce_ccv2_list_environments")
    @McpDescription(
        """Lists all CCv2 environments for a subscription.
        |Returns a JSON object: {"subscription", "total", "items": [{"code", "name", "type", "status", "deploymentStatus", "deploymentAllowed", "link"}]}.
        |Common type values: DEV, STAGING, PROD. Common status values: AVAILABLE, PROVISIONING, TERMINATED."""
    )
    suspend fun listEnvironments(
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val environments = CCv2McpService.getInstance().listEnvironments(subscriptionId)
        return mapper.map(environments)
    }

    @McpTool(name = "sap_commerce_ccv2_list_builds")
    @McpDescription(
        """Lists recent CCv2 builds for a subscription (excludes deleted builds).
        |Returns a JSON object: {"subscription", "total", "items": [{"code", "name", "branch", "status", "appCode", "appDefVersion", "createdBy", "startTime", "endTime", "buildVersion", "deployed", "link"}]}.
        |Common status values: RUNNING, SUCCESS, FAIL. Use 'top' to limit results."""
    )
    suspend fun listBuilds(
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(CCv2McpConstants.Descriptions.BUILD_TOP)
        top: Int = 20,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val builds = CCv2McpService.getInstance().listBuilds(subscriptionId, top)
        return mapper.map(builds)
    }

    @McpTool(name = "sap_commerce_ccv2_get_build")
    @McpDescription(
        """Retrieves details for a specific CCv2 build by its code.
        |Returns a JSON object: {"code", "name", "branch", "status", "appCode", "appDefVersion", "createdBy", "startTime", "endTime", "buildVersion", "deployed", "link"}.
        |Use sap_commerce_ccv2_list_builds to discover build codes."""
    )
    suspend fun getBuild(
        @McpDescription(CCv2McpConstants.Descriptions.BUILD_CODE)
        buildCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val build = CCv2McpService.getInstance().getBuild(subscriptionId, buildCode)
        return mapper.map(build)
    }

    @McpTool(name = "sap_commerce_ccv2_create_build")
    @McpDescription(
        """Triggers a new CCv2 build from the specified Git branch.
        |Returns a JSON object: {"subscription", "success", "code", "message"} where 'code' is the assigned build code.
        |Track the build's progress with sap_commerce_ccv2_get_build."""
    )
    suspend fun createBuild(
        @McpDescription("Git branch to build from (e.g. 'main', 'release/1.0').")
        branch: String,
        @McpDescription("Human-readable name for the build (e.g. 'Release 1.0 candidate').")
        name: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val build = CCv2McpService.getInstance().createBuild(subscriptionId, branch, name)
        return mapper.map(build)
    }

    @McpTool(name = "sap_commerce_ccv2_list_deployments")
    @McpDescription(
        """Lists all CCv2 deployments for a subscription.
        |Returns a JSON object: {"subscription", "total", "items": [{"code", "buildCode", "environmentCode", "status", "updateMode", "strategy", "createdBy", "createdTime", "scheduledTime", "deployedTime", "failedTime", "link"}]}.
        |Common status values: DEPLOYING, DEPLOYED, FAIL, SCHEDULED."""
    )
    suspend fun listDeployments(
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val deployments = CCv2McpService.getInstance().listDeployments(subscriptionId)
        return mapper.map(deployments)
    }

    @McpTool(name = "sap_commerce_ccv2_deploy_build")
    @McpDescription(
        """Deploys a CCv2 build to a target environment.
        |Returns a JSON object: {"subscription", "success", "code", "message"} where 'code' is the deployment code.
        |Track progress with sap_commerce_ccv2_get_deployment_status.
        |WARNING: This initiates a real deployment. Confirm build code, environment, mode, and strategy before calling."""
    )
    suspend fun deployBuild(
        @McpDescription(CCv2McpConstants.Descriptions.BUILD_CODE)
        buildCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.DEPLOYMENT_MODE)
        mode: String = "UPDATE",
        @McpDescription(CCv2McpConstants.Descriptions.DEPLOYMENT_STRATEGY)
        strategy: String = "ROLLING_UPDATE",
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val build = CCv2McpService.getInstance().deployBuild(subscriptionId, buildCode, environmentCode, mode, strategy)
        return mapper.map(build)
    }

    @McpTool(name = "sap_commerce_ccv2_get_deployment_status")
    @McpDescription(
        """Retrieves the current status of a CCv2 deployment by its deployment code.
        |Returns a JSON object: {"code", "buildCode", "environmentCode", "status", "updateMode", "strategy", "createdBy", "createdTime", "scheduledTime", "deployedTime", "failedTime", "link"}.
        |Use sap_commerce_ccv2_list_deployments to discover deployment codes."""
    )
    suspend fun getDeploymentStatus(
        @McpDescription(CCv2McpConstants.Descriptions.DEPLOYMENT_CODE)
        deploymentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val deploymentStatus = CCv2McpService.getInstance().getDeploymentStatus(subscriptionId, deploymentCode)
        return mapper.map(deploymentStatus)
    }

    @McpTool(name = "sap_commerce_ccv2_list_environment_services")
    @McpDescription(
        """Lists services (pods/replicas) running in a CCv2 environment.
        |Returns a JSON object: {"subscription", "environmentCode", "total", "items": [{"code", "name", "desiredReplicas", "availableReplicas", "replicas": [{"name", "status", "ready"}], "link"}]}.
        |Use replica 'name' values from this response when calling sap_commerce_ccv2_restart_service_pod."""
    )
    suspend fun listEnvironmentServices(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val environmentServices = CCv2McpService.getInstance().listEnvironmentServices(subscriptionId, environmentCode)
        return mapper.map(environmentServices)
    }

    @McpTool(name = "sap_commerce_ccv2_restart_service_pod")
    @McpDescription(
        """Restarts a specific replica pod of a service in a CCv2 environment.
        |Returns a JSON object: {"subscription", "success", "message"}.
        |Use sap_commerce_ccv2_list_environment_services to obtain the serviceCode and replicaName.
        |WARNING: Restarting a pod briefly interrupts traffic served by that replica."""
    )
    suspend fun restartServicePod(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SERVICE_CODE)
        serviceCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.REPLICA_NAME)
        replicaName: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val servicePod = CCv2McpService.getInstance().restartServicePod(subscriptionId, environmentCode, serviceCode, replicaName)
        return mapper.map(servicePod)
    }

    @McpTool(name = "sap_commerce_ccv2_list_environment_endpoints")
    @McpDescription(
        """Lists public endpoints (URLs) for a CCv2 environment.
        |Returns a JSON object: {"subscription", "environmentCode", "total", "items": [{"code", "name", "service", "url", "maintenanceMode", "link"}]}.
        |Use endpointCode from this response with sap_commerce_ccv2_toggle_endpoint_maintenance."""
    )
    suspend fun listEnvironmentEndpoints(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val environmentEndpoints = CCv2McpService.getInstance().listEnvironmentEndpoints(subscriptionId, environmentCode)
        return mapper.map(environmentEndpoints)
    }

    @McpTool(name = "sap_commerce_ccv2_toggle_endpoint_maintenance")
    @McpDescription(
        """Toggles the maintenance mode of a CCv2 environment endpoint (on↔off).
        |Returns a JSON object: {"subscription", "success", "message"} describing whether maintenance was activated or deactivated.
        |Use sap_commerce_ccv2_list_environment_endpoints to discover endpoint codes and their current maintenanceMode state.
        |WARNING: Activating maintenance mode makes the endpoint return a maintenance page to end users."""
    )
    suspend fun toggleEndpointMaintenance(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.ENDPOINT_CODE)
        endpointCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val endpointMaintenance = CCv2McpService.getInstance().toggleEndpointMaintenance(subscriptionId, environmentCode, endpointCode)
        return mapper.map(endpointMaintenance)
    }

    @McpTool(name = "sap_commerce_ccv2_list_data_backups")
    @McpDescription(
        """Lists data backups for a CCv2 environment, sorted newest first.
        |Returns a JSON object: {"subscription", "environmentCode", "total", "items": [{"code", "name", "buildCode", "status", "type", "description", "createdBy", "createdTime"}]}."""
    )
    suspend fun listDataBackups(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val dataBackups = CCv2McpService.getInstance().listDataBackups(subscriptionId, environmentCode)
        return mapper.map(dataBackups)
    }

    @McpTool(name = "sap_commerce_ccv2_list_scheduled_activities")
    @McpDescription(
        """Lists scheduled maintenance activities for a CCv2 environment, sorted newest first.
        |Returns a JSON object: {"subscription", "environmentCode", "total", "items": [{"code", "activityType", "activityName", "status", "scheduledTime", "startedTime", "finishedTime", "createdBy", "createdTime"}]}.
        |Common activityType values: BACKUP, RESTORE, PATCH. Common status values: SCHEDULED, RUNNING, FINISHED, FAILED."""
    )
    suspend fun listScheduledActivities(
        @McpDescription(CCv2McpConstants.Descriptions.ENVIRONMENT_CODE)
        environmentCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val scheduledActivities = CCv2McpService.getInstance().listScheduledActivities(subscriptionId, environmentCode)
        return mapper.map(scheduledActivities)
    }

    @McpTool(name = "sap_commerce_ccv2_download_build_logs")
    @McpDescription(
        """Downloads and extracts build log files for a CCv2 build to a local temp directory.
        |Returns a JSON object: {"subscription", "buildCode", "files": [{"name", "path"}]}.
        |Each entry contains the file name and its absolute local path — read the files directly from disk."""
    )
    suspend fun downloadBuildLogs(
        @McpDescription(CCv2McpConstants.Descriptions.BUILD_CODE)
        buildCode: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
    ): String {
        val mapper = resolveMapper(McpConstants.Formats.JSON)
        val buildLogs = CCv2McpService.getInstance().downloadBuildLogs(subscriptionId, buildCode)
        return mapper.map(buildLogs)
    }

    @McpTool(name = "sap_commerce_ccv2_unscramble_log")
    @McpDescription(
        """Unscrambles a CCv2 JSON exception log entry into a readable Java stack trace.
        |Accepts a raw CCv2 log line (or block) that contains a JSON-serialized exception with a 'thrown.extendedStackTrace' field.
        |Returns a JSON object: {"success", "stackTrace", "message"}. When 'success' is false the input did not contain a recognizable CCv2 exception."""
    )
    suspend fun unscrambleLog(
        @McpDescription("Raw CCv2 log text containing a JSON-encoded exception (must include a 'thrown.extendedStackTrace' field).")
        logText: String,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val unscrambleLog = CCv2McpService.getInstance().unscrambleLog(logText)
        return mapper.map(unscrambleLog)
    }

    @McpTool(name = "sap_commerce_ccv2_set_credentials")
    @McpDescription(
        """Saves CCv2 OAuth client credentials (client_id and client_secret) to the IDE's secure credential store.
        |Credentials are used automatically by all other sap_commerce_ccv2_* tools when authenticating against the CCv2 API.
        |When subscriptionId is provided the credentials are saved for that specific subscription only (overrides global credentials for that subscription).
        |When subscriptionId is omitted the credentials are saved globally and used as a fallback for all subscriptions that do not have subscription-specific credentials.
        |Returns a JSON object: {"subscription", "success", "message"}.
        |Call this tool when another CCv2 tool fails with an authentication error."""
    )
    suspend fun setCredentials(
        @McpDescription("OAuth client_id to save.")
        clientId: String,
        @McpDescription("OAuth client_secret to save.")
        clientSecret: String,
        @McpDescription(CCv2McpConstants.Descriptions.SUBSCRIPTION_ID)
        subscriptionId: String? = null,
        @McpDescription(McpConstants.Descriptions.OUTPUT_FORMAT)
        outputFormat: String = McpConstants.Formats.JSON,
    ): String {
        val mapper = resolveMapper(outputFormat)
        val credentials = CCv2McpService.getInstance().setCredentials(subscriptionId, clientId, clientSecret)
        return mapper.map(credentials)
    }
}
