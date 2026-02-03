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

package sap.commerce.toolset.project.wizard;

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.projectImport.SelectImportedProjectsStep;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ImageUtil;
import org.jetbrains.annotations.NotNull;
import sap.commerce.toolset.project.HybrisProjectImportBuilder;
import sap.commerce.toolset.project.context.ModuleGroup;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.*;

import static sap.commerce.toolset.HybrisI18nBundle.message;

// TODO: -> kotlin
// TODO: use own implementation, do not use "select all" and "unselect all"
public abstract class AbstractSelectModulesStep extends SelectImportedProjectsStep<ModuleDescriptor> {

    final static int COLUMN_WIDTH = 300;
    protected final ModuleGroup moduleGroup;

    public AbstractSelectModulesStep(final WizardContext context, final ModuleGroup moduleGroup) {
        super(context);
        this.moduleGroup = moduleGroup;
    }

    @Override
    public HybrisProjectImportBuilder getContext() {
        return (HybrisProjectImportBuilder) this.getBuilder();
    }

    protected boolean isInConflict(@NotNull final ModuleDescriptor item) {
        return this.fileChooser.getMarkedElements().contains(item) && this.calculateSelectedModuleDuplicates().contains(item);
    }

    @NotNull
    protected Set<ModuleDescriptor> calculateSelectedModuleDuplicates() {
        final Set<ModuleDescriptor> duplicateModules = new HashSet<>();
        final Map<String, ModuleDescriptor> uniqueModules = new HashMap<>();

        for (ModuleDescriptor moduleDescriptor : this.fileChooser.getMarkedElements()) {

            final ModuleDescriptor alreadySelected = uniqueModules.get(moduleDescriptor.getName());

            if (null == alreadySelected) {
                uniqueModules.put(moduleDescriptor.getName(), moduleDescriptor);
            } else {
                duplicateModules.add(alreadySelected);
                duplicateModules.add(moduleDescriptor);
            }
        }

        return duplicateModules;
    }

    @Override
    protected String getElementText(final ModuleDescriptor item) {
        final var importContext = getContext().getContext();
        if (importContext == null) return item.getName();
        return getModuleNameAndPath(importContext.getRootDirectory(), item);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        validateCommon();

        if (this.fileChooser.getMarkedElements().isEmpty()) {
            throw new ConfigurationException(
                message("hybris.project.import.error.nothing.found.to.import"),
                message("hybris.project.import.error.unable.to.proceed")
            );
        }

        return true;
    }

    @Override
    public void onStepLeaving() {
        super.onStepLeaving();
        final var markedElements = new ArrayList<>(fileChooser.getMarkedElements());
        final var importContext = getContext().getContext();

        if (importContext != null) {
            importContext.chooseModuleDescriptors(moduleGroup, markedElements);
        }
    }

    protected boolean validateCommon() throws ConfigurationException {
        final var importContext = getContext().getContext();
        if (importContext == null) return false;
        final var rootDirectory = importContext.getRootDirectory();

        final Set<ModuleDescriptor> moduleDuplicates = this.calculateSelectedModuleDuplicates();
        final Collection<String> moduleDuplicateNames = new HashSet<>(moduleDuplicates.size());

        for (ModuleDescriptor moduleDuplicate : moduleDuplicates) {
            moduleDuplicateNames.add(this.getModuleNameAndPath(rootDirectory, moduleDuplicate));
        }

        if (!moduleDuplicates.isEmpty()) {
            throw new ConfigurationException(
                message(
                    "hybris.project.import.duplicate.projects.found",
                    StringUtil.join(ArrayUtil.toStringArray(moduleDuplicateNames), "\n")
                ),
                message("hybris.project.error")
            );
        }

        return true;
    }

    /*
     * Aligned text to COLUMN_WIDTH. It is not precise by space pixel width (4pixels)
     */
    @NotNull
    protected String getModuleNameAndPath(final @NotNull Path rootDirectory, @NotNull final ModuleDescriptor moduleDescriptor) {
        final StringBuilder builder = new StringBuilder();
        builder.append(moduleDescriptor.getName());

        final Font font = getComponent().getFont();
        final BufferedImage img = ImageUtil.createImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        final FontMetrics fm = img.getGraphics().getFontMetrics(font);

        final int currentWidth = fm.stringWidth(builder.toString());
        final int spaceWidth = fm.charWidth(' ');
        final int spaceCount = (COLUMN_WIDTH - currentWidth) / spaceWidth;

        return builder
            .append(" ".repeat(Math.max(0, spaceCount)))
            .append(" (")
            .append(moduleDescriptor.getRelativePath(rootDirectory))
            .append(')')
            .toString();
    }

}
