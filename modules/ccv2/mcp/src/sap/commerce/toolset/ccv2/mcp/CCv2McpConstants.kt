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

object CCv2McpConstants {

    object Descriptions {
        const val SUBSCRIPTION_ID =
            """Optional subscription identifier — the subscription 'id' (e.g. 'd1234567') or 'name' as configured in CCv2 settings.
            |If omitted, the first configured subscription is used.
            |Use sap_commerce_ccv2_list_subscriptions to discover available subscriptions."""

        const val ENVIRONMENT_CODE =
            "CCv2 environment code (e.g. 'd1', 's1', 'p1'). Use sap_commerce_ccv2_list_environments to discover available codes."

        const val BUILD_CODE =
            "CCv2 build code (e.g. '20240101.1'). Use sap_commerce_ccv2_list_builds to discover available build codes."

        const val DEPLOYMENT_CODE =
            "CCv2 deployment code. Use sap_commerce_ccv2_list_deployments to discover available deployment codes."

        const val SERVICE_CODE =
            "CCv2 service code (e.g. 'accstorefront', 'backoffice'). Use sap_commerce_ccv2_list_environment_services to list services."

        const val REPLICA_NAME =
            "Replica pod name. Use sap_commerce_ccv2_list_environment_services to list replicas per service."

        const val ENDPOINT_CODE =
            "CCv2 endpoint code. Use sap_commerce_ccv2_list_environment_endpoints to discover available endpoint codes."

        const val DEPLOYMENT_MODE =
            "Database update mode for deployment. One of: NONE, UPDATE, INITIALIZE. Default: UPDATE."

        const val DEPLOYMENT_STRATEGY =
            "Deployment strategy. One of: ROLLING_UPDATE, RECREATE, GREEN. Default: ROLLING_UPDATE."

        const val BUILD_TOP =
            "Maximum number of builds to return. Default is 20."
    }
}
