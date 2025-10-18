/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.project.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag
import sap.commerce.toolset.project.ExtensionDescriptor

@Tag("HybrisProjectSettings")
data class ProjectSettingsState(
    @JvmField @OptionTag val customDirectory: String? = null,
    @JvmField @OptionTag val hybrisDirectory: String? = null,
    @JvmField @OptionTag val configDirectory: String? = null,
    @JvmField @OptionTag val importedByVersion: String? = null,
    @JvmField @OptionTag val hybrisVersion: String? = null,
    @JvmField @OptionTag val javadocUrl: String? = null,
    @JvmField @OptionTag val sourceCodeFile: String? = null,
    @JvmField @OptionTag val externalExtensionsDirectory: String? = null,
    @JvmField @OptionTag val externalConfigDirectory: String? = null,
    @JvmField @OptionTag val externalDbDriversDirectory: String? = null,
    @JvmField @OptionTag val ideModulesFilesDirectory: String? = null,
    @JvmField @OptionTag val generateCodeOnRebuild: Boolean = true,
    @JvmField @OptionTag val generateCodeOnJUnitRunConfiguration: Boolean = false,
    @JvmField @OptionTag val generateCodeTimeoutSeconds: Int = 60,
    @JvmField @OptionTag val importOotbModulesInReadOnlyMode: Boolean = true,
    @JvmField @OptionTag val followSymlink: Boolean = false,
    @JvmField @OptionTag val scanThroughExternalModule: Boolean = true,
    @JvmField @OptionTag val excludeTestSources: Boolean = false,
    @JvmField @OptionTag val importCustomAntBuildFiles: Boolean = false,
    @JvmField @OptionTag val showFullModuleName: Boolean = false,
    @JvmField @OptionTag val removeExternalModulesOnRefresh: Boolean = false,
    @JvmField val unusedExtensions: Set<String> = emptySet(),
    @JvmField val modulesOnBlackList: Set<String> = emptySet(),

    // by BaseState.property(TreeMap<String, ExtensionDescriptor> { a, b -> a.compareTo(b, true) }) { it.isEmpty() }
    @JvmField val availableExtensions: Map<String, ExtensionDescriptor> = emptyMap(),
    @JvmField val excludedFromScanning: Set<String> = emptySet(),
    @JvmField @OptionTag val useFakeOutputPathForCustomExtensions: Boolean = false,
)