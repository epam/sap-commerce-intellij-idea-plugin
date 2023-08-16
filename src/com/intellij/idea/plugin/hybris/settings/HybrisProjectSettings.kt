/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
import com.intellij.idea.plugin.hybris.groovy.settings.GroovySettings
import com.intellij.idea.plugin.hybris.impex.settings.ImpexSettings
import com.intellij.idea.plugin.hybris.polyglotQuery.settings.PolyglotQuerySettings
import com.intellij.openapi.components.BaseState
import java.util.*

class HybrisProjectSettings : BaseState() {
    var flexibleSearchSettings: FlexibleSearchSettings = FlexibleSearchSettings()
    var polyglotQuerySettings: PolyglotQuerySettings = PolyglotQuerySettings()
    var impexSettings: ImpexSettings = ImpexSettings()
    var groovySettings: GroovySettings = GroovySettings()

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
    var importOotbModulesInReadOnlyMode by property(false)
    var followSymlink by property(false)
    var scanThroughExternalModule by property(true)
    var excludeTestSources by property(false)
    var importCustomAntBuildFiles by property(false)
    var showFullModuleName by property(false)
    var completeSetOfAvailableExtensionsInHybris: Set<String> = mutableSetOf()
    var unusedExtensions: Set<String> = mutableSetOf()
    var modulesOnBlackList: Set<String> = mutableSetOf()
    var availableExtensions: MutableMap<String, ExtensionDescriptor> = TreeMap<String, ExtensionDescriptor> { a, b ->
        a.compareTo(b, true)
    }
}