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

package com.intellij.idea.plugin.hybris.project.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptorImportStatus;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.CCv2ModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.EclipseModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.GradleModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.impl.MavenModuleDescriptor;
import com.intellij.openapi.options.ConfigurationException;
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
            if (descriptor instanceof EclipseModuleDescriptor || descriptor instanceof CCv2ModuleDescriptor) {
                fileChooser.setElementMarked(descriptor, true);
            }
            if (descriptor instanceof CCv2ModuleDescriptor && descriptor.isPreselected()) {
                descriptor.setImportStatus(ModuleDescriptorImportStatus.MANDATORY);
            }
        }
    }

    @Override
    @Nullable
    protected Icon getElementIcon(final ModuleDescriptor module) {
        if (this.isInConflict(module)) {
            return HybrisIcons.MODULE_CONFLICT;
        }
        if (module instanceof MavenModuleDescriptor) {
            return HybrisIcons.MODULE_MAVEN;
        }
        if (module instanceof EclipseModuleDescriptor) {
            return HybrisIcons.MODULE_ECLIPSE;
        }
        if (module instanceof GradleModuleDescriptor) {
            return HybrisIcons.MODULE_GRADLE;
        }
        if (module instanceof CCv2ModuleDescriptor) {
            return HybrisIcons.MODULE_CCV2;
        }

        return null;
    }

    @Override
    protected void setList(final List<ModuleDescriptor> otherElements) {
        final List<ModuleDescriptor> allModules = Stream.concat(
                getContext().getHybrisModulesToImport().stream(),
                otherElements.stream()
            )
            .collect(Collectors.toList());
        try {
            this.getContext().setList(allModules);
        } catch (ConfigurationException e) {
            // no-op already validated
        }
    }

    @Override
    protected List<ModuleDescriptor> getAdditionalFixedElements() {
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
