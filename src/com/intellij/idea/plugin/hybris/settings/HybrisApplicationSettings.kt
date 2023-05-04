/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.idea.plugin.hybris.common.HybrisConstants

data class HybrisApplicationSettings(

    var groupModules: Boolean = true,
    var hideEmptyMiddleFolders: Boolean = true,
    var defaultPlatformInReadOnly: Boolean = true,
    var followSymlink: Boolean = true,
    var sourceZipUsed: Boolean = true,
    var warnIfGeneratedItemsAreOutOfDate: Boolean = true,
    var withMavenSources: Boolean = false,
    var withMavenJavadocs: Boolean = false,
    var withStandardProvidedSources: Boolean = false,
    var allowedSendingPlainStatistics: Boolean = false,
    var scanThroughExternalModule: Boolean = false,
    var excludeTestSources: Boolean = false,
    var groupHybris: String = "Hybris",
    var groupOtherHybris: String = "Hybris/Unused",
    var groupCustom: String = "Custom",
    var groupNonHybris: String = "Others",
    var groupOtherCustom: String = "Custom/Unused",
    var groupPlatform: String = "Platform",
    var groupCCv2: String = "CCv2",
    var externalDbDriversDirectory: String = "",
    var sourceCodeDirectory: String = "",
    var junkDirectoryList: List<String> = HybrisConstants.DEFAULT_JUNK_FILE_NAMES,
    var extensionsResourcesToExclude: List<String> = HybrisConstants.DEFAULT_EXTENSIONS_RESOURCES_TO_EXCLUDE,
    var excludedFromIndexList: List<String> = HybrisConstants.DEFAULT_EXCLUDED_FROM_INDEX,

)
