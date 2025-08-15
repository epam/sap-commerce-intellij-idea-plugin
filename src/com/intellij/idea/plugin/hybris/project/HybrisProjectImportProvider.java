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

package com.intellij.idea.plugin.hybris.project;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.ProjectWizardStepFactory;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.project.wizard.*;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.projectImport.ProjectImportBuilder;
import com.intellij.projectImport.ProjectImportProvider;
import com.intellij.projectImport.ProjectOpenProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sap.commerce.toolset.HybrisI18NBundleUtils;

public class HybrisProjectImportProvider extends ProjectImportProvider {

    @Override
    protected boolean canImportFromFile(final VirtualFile file) {
        return doGetProjectOpenProcessor().canOpenProject(file);
    }

    @Override
    public ModuleWizardStep[] createSteps(final WizardContext context) {
        final ProjectWizardStepFactory stepFactory = ProjectWizardStepFactory.getInstance();

        return new ModuleWizardStep[]{
            new CheckRequiredPluginsWizardStep(context),
            new InformationStep(context),
            new ProjectImportWizardRootStep(context),
            new SelectHybrisModulesToImportStep(context),
            new SelectOtherModulesToImportStep(context),
            stepFactory.createProjectJdkStep(context)
        };
    }

    private HybrisProjectOpenProcessor doGetProjectOpenProcessor() {
        return ProjectOpenProcessor.EXTENSION_POINT_NAME.findExtensionOrFail(HybrisProjectOpenProcessor.class);
    }

    @NotNull
    @Override
    protected DefaultHybrisProjectImportBuilder doGetBuilder() {
        return ProjectImportBuilder.EXTENSIONS_POINT_NAME.findExtensionOrFail(DefaultHybrisProjectImportBuilder.class);
    }

    @Nullable
    @Override
    public String getFileSample() {
        return HybrisI18NBundleUtils.message("hybris.project.import.dialog.message");
    }
}
