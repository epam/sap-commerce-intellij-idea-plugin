package com.intellij.idea.plugin.hybris.gotoClass;

import com.intellij.ide.util.gotoByName.DefaultClassNavigationContributor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent;
import com.intellij.navigation.GotoClassContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.Processors;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CustomGotoClassContributor implements GotoClassContributor {

    private final DefaultClassNavigationContributor inner = new DefaultClassNavigationContributor();

    @Nullable
    @Override
    public String getQualifiedName(final NavigationItem item) {
        return null;
    }

    @Nullable
    @Override
    public String getQualifiedNameSeparator() {
        return null;
    }

    @NotNull
    @Override
    public String[] getNames(final Project project, final boolean includeNonProjectItems) {
        if (includeNonProjectItems || !HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) {
            return ArrayUtil.EMPTY_STRING_ARRAY;
        }
        final GlobalSearchScope scope = new OotbClassesSearchScope(project);
        final List<String> result = new ArrayList<>();
        final Processor<String> processor = Processors.cancelableCollectProcessor(result);

        PsiShortNamesCache.getInstance(project).processAllClassNames(
            processor,
            scope,
            IdFilter.getProjectIdFilter(project, true)
        );
        return ArrayUtil.toStringArray(result);
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(
        final String name,
        final String pattern,
        final Project project,
        final boolean includeNonProjectItems
    ) {
        if (includeNonProjectItems || !HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()) {
            return NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY;
        }
        final List<NavigationItem> result = new ArrayList<>();
        final Processor<NavigationItem> processor = Processors.cancelableCollectProcessor(result);
        final GlobalSearchScope scope = new OotbClassesSearchScope(project);
        inner.processElementsWithName(name, processor, new FindSymbolParameters(pattern, pattern, scope));

        return result.isEmpty() ? NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY :
            result.toArray(NavigationItem.EMPTY_NAVIGATION_ITEM_ARRAY);
    }

}
