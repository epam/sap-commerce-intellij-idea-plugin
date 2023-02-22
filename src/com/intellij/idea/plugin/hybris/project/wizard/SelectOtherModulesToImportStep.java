/*
 * This file is part of "hybris integration" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
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

package com.intellij.idea.plugin.hybris.project.wizard;

import com.intellij.icons.AllIcons;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.project.descriptors.CCv2HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.EclipseModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.GradleModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.MavenModuleDescriptor;
import com.intellij.openapi.options.ConfigurationException;
import icons.GradleIcons;
import icons.OpenapiIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SelectOtherModulesToImportStep extends AbstractSelectModulesToImportStep {

    public SelectOtherModulesToImportStep(final WizardContext context) {
        super(context);
    }

    @Override
    public void updateStep() {
        super.updateStep();
        for (int index = 0; index < fileChooser.getElementCount(); index++) {
            final var descriptor = fileChooser.getElementAt(index);
            if (descriptor instanceof EclipseModuleDescriptor || descriptor instanceof CCv2HybrisModuleDescriptor) {
                fileChooser.setElementMarked(descriptor, true);
            }
        }
    }

    @Override
    @Nullable
    protected Icon getElementIcon(final HybrisModuleDescriptor module) {
        if (this.isInConflict(module)) {
            return AllIcons.Actions.Cancel;
        }
        if (module instanceof MavenModuleDescriptor) {
            return OpenapiIcons.RepositoryLibraryLogo;
        }
        if (module instanceof EclipseModuleDescriptor) {
            return AllIcons.Providers.Eclipse;
        }
        if (module instanceof GradleModuleDescriptor) {
            return GradleIcons.Gradle;
        }
        if (module instanceof CCv2HybrisModuleDescriptor) {
            return HybrisIcons.MODULE_CCV2;
        }

        return null;
    }

    @Override
    protected void setList(final List<HybrisModuleDescriptor> otherElements) {
        final Stream<HybrisModuleDescriptor> hybrisModuleStream = getContext().getHybrisModulesToImport().stream();
        final List<HybrisModuleDescriptor> allModules =
            Stream.concat(hybrisModuleStream, otherElements.stream()).collect(Collectors.toList());
        try {
            this.getContext().setList(allModules);
        } catch (ConfigurationException e) {
            // no-op already validated
        }
    }

    @Override
    protected List<HybrisModuleDescriptor> getAdditionalFixedElements() {
        return getContext().getHybrisModulesToImport();
    }

    @Override
    public boolean validate() throws ConfigurationException {
        return this.validateCommon();
    }

    @Override
    public boolean isStepVisible() {
        getContext().setExternalStepModuleList();
        return !getContext().getList().isEmpty();
    }

    @Override
    public String getName() {
        return "Other";
    }
}
