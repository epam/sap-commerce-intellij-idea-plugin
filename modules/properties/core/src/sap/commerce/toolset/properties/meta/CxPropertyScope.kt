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

package sap.commerce.toolset.properties.meta

import sap.commerce.toolset.project.ProjectConstants

/**
 * Precedence tiers of the SAP Commerce property chain, declared from the lowest to the highest priority.
 *
 * A declaration coming from a tier with a higher [ordinal] wins over a declaration of the very same property coming
 * from a lower tier; within one tier the winner is decided by [CxPropertySource.rank].
 *
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE/b490bb4e85bc42a7aa09d513d0bcb18e/8b8e13c9866910149d40b151a9196543.html?locale=en-US">Configuring the Behavior of SAP Commerce</a>
 * @see <a href="https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/d090fb3dd48a418d967a1dfdca9fcac6.html?locale=en-US">SAP Commerce Cloud Properties</a>
 */
enum class CxPropertyScope(val title: String, val fileName: String? = null) {

    /** `${platformhome}/platformhome.properties` */
    PLATFORM_HOME("Platform Home", ProjectConstants.File.PLATFORM_HOME_PROPERTIES),

    /** `${platformhome}/env.properties` */
    ENV("Environment", ProjectConstants.File.ENV_PROPERTIES),

    /** `${platformhome}/resources/advanced.properties` */
    ADVANCED("Advanced", ProjectConstants.File.ADVANCED_PROPERTIES),

    /** `<extension>/project.properties`, ranked by the extension load order. */
    PROJECT("Project", ProjectConstants.File.PROJECT_PROPERTIES),

    /** `${HYBRIS_CONFIG_DIR}/local.properties` */
    LOCAL("Local", ProjectConstants.File.LOCAL_PROPERTIES),

    /** `${HYBRIS_OPT_CONFIG_DIR}/NN-<name>.properties`, ranked alphabetically. */
    OPTIONAL_CONFIG("Optional Config"),

    /** The file referenced by the `HYBRIS_RUNTIME_PROPERTIES` environment variable. */
    RUNTIME("Runtime"),

    /** Values injected by the running process itself: `platformhome` and the `env.properties.prefix` variables. */
    ENVIRONMENT("System Environment"),
    ;

    companion object {
        fun of(fileName: String) = entries.find { it.fileName == fileName }
    }
}
