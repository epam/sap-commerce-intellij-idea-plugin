package com.intellij.idea.plugin.hybris.gotoClass

import com.intellij.ide.util.gotoByName.DefaultClassNavigationContributor
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.ArrayUtil
import com.intellij.util.Processors
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.IdFilter

class CustomGotoClassContributor : GotoClassContributor {

    private val defaultClassNavigationContributor = DefaultClassNavigationContributor()

    override fun getQualifiedName(item: NavigationItem) = null

    override fun getQualifiedNameSeparator() = null

    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String> {
        if (shouldNotBeProcessed(includeNonProjectItems, project)) return emptyArray()

        val result = ArrayList<String>()
        val scope = OotbClassesSearchScope(project)
        val processor = Processors.cancelableCollectProcessor(result)
        val projectIdFilter = IdFilter.getProjectIdFilter(project, true)
        val shortNamesCache = PsiShortNamesCache.getInstance(project)

        shortNamesCache.processAllClassNames(processor, scope, projectIdFilter)

        return result.toTypedArray()
    }

    override fun getItemsByName(
        name: String,
        pattern: String,
        project: Project,
        includeNonProjectItems: Boolean
    ): Array<NavigationItem> {
        if (shouldNotBeProcessed(includeNonProjectItems, project)) return NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY

        val result = ArrayList<NavigationItem>()
        val scope = OotbClassesSearchScope(project)
        val processor = Processors.cancelableCollectProcessor(result)
        val symbolParameters = FindSymbolParameters(pattern, pattern, scope)

        defaultClassNavigationContributor.processElementsWithName(name, processor, symbolParameters)

        return if (result.isEmpty()) NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY
        else result.toTypedArray()
    }

    private fun shouldNotBeProcessed(includeNonProjectItems: Boolean, project: Project) = includeNonProjectItems
        || !HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()
}