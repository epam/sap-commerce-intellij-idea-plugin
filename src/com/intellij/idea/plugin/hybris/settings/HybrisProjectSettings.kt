/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2023 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.settings

import com.intellij.idea.plugin.hybris.facet.ExtensionDescriptor
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.FlexibleSearchSettings
import com.intellij.idea.plugin.hybris.impex.settings.ImpexSettings
import com.intellij.idea.plugin.hybris.polyglotQuery.settings.PolyglotQuerySettings
import java.util.*

data class HybrisProjectSettings(
    var flexibleSearchSettings: FlexibleSearchSettings = FlexibleSearchSettings(),
    var polyglotQuerySettings: PolyglotQuerySettings = PolyglotQuerySettings(),
    var impexSettings: ImpexSettings = ImpexSettings(),

    var customDirectory: String? = null,
    var hybrisDirectory: String? = null,
    var configDirectory: String? = null,
    var importedByVersion: String? = null,
    var hybrisVersion: String? = null,
    var javadocUrl: String? = null,
    var sourceCodeFile: String? = null,
    var externalExtensionsDirectory: String? = null,
    var externalConfigDirectory: String? = null,
    var externalDbDriversDirectory: String? = null,
    var ideModulesFilesDirectory: String? = null,
    var hybrisProject: Boolean = false,
    var importOotbModulesInReadOnlyMode: Boolean = false,
    var followSymlink: Boolean = false,
    var scanThroughExternalModule: Boolean = true,
    var excludeTestSources: Boolean = false,
    var importCustomAntBuildFiles: Boolean = false,
    var completeSetOfAvailableExtensionsInHybris: Set<String> = mutableSetOf(),
    var unusedExtensions: Set<String> = mutableSetOf(),
    var modulesOnBlackList: Set<String> = mutableSetOf(),
    var availableExtensions: MutableMap<String, ExtensionDescriptor> = TreeMap<String, ExtensionDescriptor> { a, b ->
        a.compareTo(b, true)
    },
)