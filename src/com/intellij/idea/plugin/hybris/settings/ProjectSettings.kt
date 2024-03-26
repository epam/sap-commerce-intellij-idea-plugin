/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.openapi.components.BaseState
import com.intellij.util.xmlb.annotations.Tag
import java.util.*

@Tag("HybrisProjectSettings")
class ProjectSettings : BaseState() {
    var customDirectory by string(null)
    var hybrisDirectory by string(null)
    var configDirectory by string(null)
    var importedByVersion by string(null)
    var hybrisVersion by string(null)
    var javadocUrl by string(null)
    var sourceCodeFile by string(null)
    var externalExtensionsDirectory by string(null)
    var externalConfigDirectory by string(null)
    var externalDbDriversDirectory by string(null)
    var ideModulesFilesDirectory by string(null)
    var hybrisProject by property(false)
    var generateCodeOnRebuild by property(true)
    var generateCodeTimeoutSeconds by property(60)
    var importOotbModulesInReadOnlyMode by property(false)
    var followSymlink by property(false)
    var scanThroughExternalModule by property(true)
    var excludeTestSources by property(false)
    var importCustomAntBuildFiles by property(false)
    var showFullModuleName by property(false)
    var removeExternalModulesOnRefresh by property(false)
    var completeSetOfAvailableExtensionsInHybris by stringSet()
    var unusedExtensions by stringSet()
    var modulesOnBlackList by stringSet()
    var availableExtensions by property(TreeMap<String, ExtensionDescriptor> { a, b -> a.compareTo(b, true) }) { it.isEmpty() }
    var excludedFromScanning by stringSet()
    var remoteConnectionSettingsList by list<RemoteConnectionSettings>()
    var useFakeOutputPathForCustomExtensions by property(false)
    var activeCCv2Subscription by string(null)
}