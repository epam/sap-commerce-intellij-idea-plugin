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

package sap.commerce.toolset.project.configurator

import com.intellij.find.FindSettings
import com.intellij.ide.projectView.impl.ModuleGroup
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.externalSystem.service.project.IdeModifiableModelsProvider
import com.intellij.openapi.project.Project
import com.intellij.psi.search.scope.packageSet.FilePatternPackageSet
import com.intellij.psi.search.scope.packageSet.NamedScope
import com.intellij.psi.search.scope.packageSet.NamedScopeManager
import com.intellij.psi.search.scope.packageSet.UnionPackageSet
import com.intellij.util.ArrayUtil
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.i18n
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import sap.commerce.toolset.settings.ApplicationSettings
import javax.swing.Icon

class SearchScopeConfigurator : ProjectImportConfigurator {

    override val name: String
        get() = "Search Scope"

    override fun configure(
        hybrisProjectDescriptor: HybrisProjectDescriptor,
        modifiableModelsProvider: IdeModifiableModelsProvider
    ) {
        val project = hybrisProjectDescriptor.project ?: return
        val applicationSettings = ApplicationSettings.getInstance()
        val customGroupName = applicationSettings.groupCustom
        val commerceGroupName = applicationSettings.groupHybris
        val nonHybrisGroupName = applicationSettings.groupNonHybris
        val platformGroupName = applicationSettings.groupPlatform
        val newScopes = mutableListOf<NamedScope>()
        var customScope: NamedScope? = null
        var platformScope: NamedScope? = null
        var commerceScope: NamedScope? = null
        var hybrisScope: NamedScope? = null

        if (groupExists(customGroupName, project)) {
            customScope = createScope(HybrisIcons.Extension.CUSTOM, customGroupName)
            newScopes.add(customScope)

            newScopes += NamedScope(
                i18n("hybris.scope.editable.custom.ts.files"),
                FilePatternPackageSet("$customGroupName*", "*//*${HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING}")
            )

            newScopes += NamedScope(
                "${HybrisConstants.SEARCH_SCOPE_Y_PREFIX} ${i18n("hybris.scope.editable.custom.ts.beans.impex.files")}",
                createCustomTsImpexBeansFilesPattern(applicationSettings)
            )
        }

        if (groupExists(platformGroupName, project)) {
            platformScope = createScope(HybrisIcons.Scope.PLATFORM_GROUP, platformGroupName)
            newScopes.add(platformScope)
        }

        if (groupExists(commerceGroupName, project)) {
            commerceScope = createScope(HybrisIcons.Scope.COMMERCE_GROUP, commerceGroupName)
            newScopes.add(commerceScope)
        }

        if (platformScope != null && commerceScope != null) {
            hybrisScope = createScopeFor2Groups(
                HybrisIcons.Scope.PLATFORM,
                platformGroupName,
                commerceGroupName
            )
            newScopes.add(hybrisScope)
        }

        if (groupExists(nonHybrisGroupName, project)) {
            newScopes.add(createScope(HybrisIcons.Scope.LOCAL, nonHybrisGroupName))
        }

        newScopes += NamedScope(
            i18n("hybris.scope.editable.all.ts.files"),
            FilePatternPackageSet(null, "*//*${HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING}")
        )

        newScopes += NamedScope(
            i18n("hybris.scope.editable.all.beans.files"),
            FilePatternPackageSet(null, "*//*${HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING}")
        )

        ApplicationManager.getApplication().invokeLater {
            addOrReplaceScopes(project, newScopes)
        }

        val defaultScope = customScope ?: hybrisScope ?: platformScope
        defaultScope?.let {
            FindSettings.getInstance().customScope = it.presentableName
            FindSettings.getInstance().defaultScopeName = it.presentableName
        }
    }

    private fun createCustomTsImpexBeansFilesPattern(appSettings: ApplicationSettings) = appSettings.groupCustom.let { customGroupName ->
        UnionPackageSet.create(
            UnionPackageSet.create(
                FilePatternPackageSet("$customGroupName*", "*//*${HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING}"),
                FilePatternPackageSet("$customGroupName*", "*//*${HybrisConstants.HYBRIS_BEANS_XML_FILE_ENDING}")
            ),
            FilePatternPackageSet("$customGroupName*", "*//*${HybrisConstants.HYBRIS_IMPEX_XML_FILE_ENDING}")
        )
    }

    private fun addOrReplaceScopes(project: Project, newScopes: List<NamedScope>) {
        val newNames = newScopes.map { it.presentableName }.toSet()

        with(NamedScopeManager.getInstance(project)) {
            val filtered = editableScopes.filter { it.presentableName !in newNames }
            scopes = ArrayUtil.mergeArrays(filtered.toTypedArray(), newScopes.toTypedArray())
        }
    }

    private fun groupExists(groupName: String, project: Project) = ModuleGroup(listOf(groupName))
        .modulesInGroup(project, true)
        .isNotEmpty()

    private fun createScope(icon: Icon, groupName: String) = NamedScope(
        "${HybrisConstants.SEARCH_SCOPE_Y_PREFIX} $groupName",
        icon,
        FilePatternPackageSet(
            "$groupName*",
            "*//*"
        )
    )

    private fun createScopeFor2Groups(icon: Icon, first: String, second: String) = NamedScope(
        "${HybrisConstants.SEARCH_SCOPE_Y_PREFIX} $first & $second",
        icon,
        UnionPackageSet.create(
            FilePatternPackageSet("$first*", "*//*"),
            FilePatternPackageSet("$second*", "*//*")
        )
    )
}