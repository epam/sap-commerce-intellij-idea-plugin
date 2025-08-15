/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.configurators.impl;

import com.intellij.find.FindSettings;
import com.intellij.ide.projectView.impl.ModuleGroup;
import com.intellij.idea.plugin.hybris.impex.ImpExConstants;
import com.intellij.idea.plugin.hybris.project.configurators.SearchScopeConfigurator;
import com.intellij.idea.plugin.hybris.settings.ApplicationSettings;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.ModifiableModuleModel;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.scope.packageSet.*;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.HybrisI18NBundleUtils;
import sap.commerce.toolset.HybrisIcons;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static sap.commerce.toolset.HybrisConstants.*;
import static sap.commerce.toolset.HybrisI18NBundleUtils.message;

public class DefaultSearchScopeConfigurator implements SearchScopeConfigurator {

    @Override
    public void configure(
        final @NotNull ProgressIndicator indicator,
        final @NotNull Project project,
        final @NotNull ApplicationSettings applicationSettings,
        final @NotNull ModifiableModuleModel model
    ) {
        indicator.setText(message("hybris.project.import.search.scope"));
        final String customGroupName = applicationSettings.getGroupCustom();
        final String commerceGroupName = applicationSettings.getGroupHybris();
        final String nonHybrisGroupName = applicationSettings.getGroupNonHybris();
        final String platformGroupName = applicationSettings.getGroupPlatform();
        final List<NamedScope> newScopes = new ArrayList<>();
        NamedScope customScope = null;
        NamedScope platformScope = null;
        NamedScope commerceScope = null;
        NamedScope hybrisScope = null;

        if (groupExists(model, customGroupName)) {
            customScope = createScope(HybrisIcons.Extension.INSTANCE.getCUSTOM(), customGroupName);
            newScopes.add(customScope);

            newScopes.add(new NamedScope(
                HybrisI18NBundleUtils.message("hybris.scope.editable.custom.ts.files"),
                new FilePatternPackageSet(
                    customGroupName + '*',
                    "*//*" + HYBRIS_ITEMS_XML_FILE_ENDING
                )
            ));

            newScopes.add(new NamedScope(
                SEARCH_SCOPE_Y_PREFIX + ' ' + HybrisI18NBundleUtils.message("hybris.scope.editable.custom.ts.beans.impex.files"),
                createCustomTsImpexBeansFilesPattern(applicationSettings)
            ));
        }
        if (groupExists(model, platformGroupName)) {
            platformScope = createScope(HybrisIcons.Scope.INSTANCE.getPLATFORM_GROUP(), platformGroupName);
            newScopes.add(platformScope);
        }
        if (groupExists(model, commerceGroupName)) {
            commerceScope = createScope(HybrisIcons.Scope.INSTANCE.getCOMMERCE_GROUP(), commerceGroupName);
            newScopes.add(commerceScope);
        }
        if (platformScope != null && commerceScope != null) {
            hybrisScope = createScopeFor2Groups(HybrisIcons.Scope.INSTANCE.getPLATFORM(), platformGroupName, commerceGroupName);
            newScopes.add(hybrisScope);
        }
        if (groupExists(model, nonHybrisGroupName)) {
            newScopes.add(createScope(HybrisIcons.Scope.INSTANCE.getLOCAL(), nonHybrisGroupName));
        }
        newScopes.add(new NamedScope(
            HybrisI18NBundleUtils.message("hybris.scope.editable.all.ts.files"),
            new FilePatternPackageSet(null, "*//*" + HYBRIS_ITEMS_XML_FILE_ENDING)
        ));
        newScopes.add(new NamedScope(
            HybrisI18NBundleUtils.message("hybris.scope.editable.all.beans.files"),
            new FilePatternPackageSet(null, "*//*" + HYBRIS_BEANS_XML_FILE_ENDING)
        ));
        ApplicationManager.getApplication().invokeLater(() -> addOrReplaceScopes(project, newScopes));

        final NamedScope defaultScope = customScope != null ? customScope : hybrisScope != null ? hybrisScope : platformScope;
        if (defaultScope != null) {
            FindSettings.getInstance().setCustomScope(defaultScope.getPresentableName());
            FindSettings.getInstance().setDefaultScopeName(defaultScope.getPresentableName());
        }
    }

    @NotNull
    public static PackageSet createCustomTsImpexBeansFilesPattern(final @NotNull ApplicationSettings appSettings) {
        final String customGroupName = appSettings.getGroupCustom();
        final FilePatternPackageSet tsFilePatternPackageSet = new FilePatternPackageSet(
            customGroupName + '*',
            "*//*" + HYBRIS_ITEMS_XML_FILE_ENDING
        );
        final FilePatternPackageSet beansFilePatternPackageSet = new FilePatternPackageSet(
            customGroupName + '*',
            "*//*" + HYBRIS_BEANS_XML_FILE_ENDING
        );
        final FilePatternPackageSet impexFilePatternPackageSet = new FilePatternPackageSet(
            customGroupName + '*',
            "*//*" + ImpExConstants.HYBRIS_IMPEX_XML_FILE_ENDING
        );
        return UnionPackageSet.create(
            UnionPackageSet.create(tsFilePatternPackageSet, beansFilePatternPackageSet),
            impexFilePatternPackageSet);
    }

    private static void addOrReplaceScopes(@NotNull final Project project, @NotNull final List<NamedScope> newScopes) {
        final Set<String> newScopeNames = newScopes
            .stream()
            .map(NamedScope::getPresentableName)
            .collect(Collectors.toSet());

        final NamedScopeManager namedScopeManager = NamedScopeManager.getInstance(project);
        final NamedScope[] existingScopes = namedScopeManager.getEditableScopes();

        final NamedScope[] filteredScopes = Arrays
            .stream(existingScopes)
            .filter(it -> !newScopeNames.contains(it.getPresentableName()))
            .toArray(NamedScope[]::new);

        namedScopeManager.setScopes(ArrayUtil.mergeArrays(
            filteredScopes,
            newScopes.toArray(new NamedScope[0])
        ));
    }

    private static boolean groupExists(@NotNull final ModifiableModuleModel model, final String groupName) {
        return !new ModuleGroup(List.of(groupName))
            .modulesInGroup(model.getProject(), true)
            .isEmpty();
    }

    @NotNull
    private static NamedScope createScope(final Icon icon, @NotNull final String groupName) {
        final FilePatternPackageSet filePatternPackageSet = new FilePatternPackageSet(
            groupName + '*',
            "*//*"
        );
        return new NamedScope(
            SEARCH_SCOPE_Y_PREFIX + ' ' + groupName,
            icon,
            filePatternPackageSet
        );
    }

    @NotNull
    private static NamedScope createScopeFor2Groups(final Icon icon, @NotNull final String firstGroupName, @NotNull final String secondGroupName) {
        final FilePatternPackageSet firstFilePatternPackageSet = new FilePatternPackageSet(
            firstGroupName + '*',
            "*//*"
        );
        final FilePatternPackageSet secondFilePatternPackageSet = new FilePatternPackageSet(
            secondGroupName + '*',
            "*//*"
        );
        final PackageSet unionPackageSet = UnionPackageSet.create(
            firstFilePatternPackageSet,
            secondFilePatternPackageSet
        );
        return new NamedScope(
            SEARCH_SCOPE_Y_PREFIX + ' ' + firstGroupName + " & " + secondGroupName,
            icon,
            unionPackageSet
        );
    }
}
