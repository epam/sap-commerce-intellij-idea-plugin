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

package sap.commerce.toolset.settings.state

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag
import sap.commerce.toolset.HybrisConstants

@Tag("HybrisApplicationSettings")
data class ApplicationSettingsState(

    @JvmField @OptionTag val groupModules: Boolean = true,
    @JvmField @OptionTag val groupExternalModules: Boolean = false,
    @JvmField @OptionTag val hideEmptyMiddleFolders: Boolean = true,
    @JvmField @OptionTag val defaultPlatformInReadOnly: Boolean = true,
    @JvmField @OptionTag val followSymlink: Boolean = true,
    @JvmField @OptionTag val sourceZipUsed: Boolean = true,
    @JvmField @OptionTag val warnIfGeneratedItemsAreOutOfDate: Boolean = true,
    @JvmField @OptionTag val ignoreNonExistingSourceDirectories: Boolean = false,
    @JvmField @OptionTag val withStandardProvidedSources: Boolean = true,
    @JvmField @OptionTag val scanThroughExternalModule: Boolean = true,
    @JvmField @OptionTag val excludeTestSources: Boolean = false,
    @JvmField @OptionTag val importCustomAntBuildFiles: Boolean = false,
    @JvmField @OptionTag val groupHybris: String = "Hybris",
    @JvmField @OptionTag val groupOtherHybris: String = "Hybris/Unused",
    @JvmField @OptionTag val groupCustom: String = "Custom",
    @JvmField @OptionTag val groupNonHybris: String = "Others",
    @JvmField @OptionTag val groupOtherCustom: String = "Custom/Unused",
    @JvmField @OptionTag val groupPlatform: String = "Platform",
    @JvmField @OptionTag val groupCCv2: String = "CCv2",
    @JvmField @OptionTag val groupNameExternalModules: String = "External Modules",
    @JvmField @OptionTag val externalDbDriversDirectory: String? = null,
    @JvmField @OptionTag val sourceCodeDirectory: String? = null,
    @JvmField val junkDirectoryList: List<String> = HybrisConstants.DEFAULT_JUNK_FILE_NAMES,
    @JvmField val extensionsResourcesToExclude: List<String> = HybrisConstants.DEFAULT_EXTENSIONS_RESOURCES_TO_EXCLUDE,
    @JvmField val excludedFromIndexList: List<String> = HybrisConstants.DEFAULT_EXCLUDED_FROM_INDEX,
)