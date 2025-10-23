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
import sap.commerce.toolset.project.DefaultHybrisProjectImportBuilder;
import sap.commerce.toolset.project.descriptor.ModuleDescriptor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static sap.commerce.toolset.HybrisI18NBundleUtils.message;
import static sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus.MANDATORY;
import static sap.commerce.toolset.project.descriptor.ModuleDescriptorImportStatus.UNUSED;

public abstract class AbstractSelectModulesToImportStep extends SelectImportedProjectsStep<ModuleDescriptor> {

    final static int COLUMN_WIDTH = 300;

    public AbstractSelectModulesToImportStep(final WizardContext context) {
        super(context);
        init();
    }

    protected void init() {
    }

    @Override
    public DefaultHybrisProjectImportBuilder getContext() {
        return (DefaultHybrisProjectImportBuilder) this.getBuilder();
    }

    protected boolean isInConflict(@NotNull final ModuleDescriptor item) {
        return (this.fileChooser.getMarkedElements().contains(item) || getAdditionalFixedElements().contains(item))
               && this.calculateSelectedModuleDuplicates().contains(item);
    }

    protected List<ModuleDescriptor> getAdditionalFixedElements() {
        return Collections.emptyList();
    }

    @NotNull
    protected Set<ModuleDescriptor> calculateSelectedModuleDuplicates() {
        final Set<ModuleDescriptor> duplicateModules = new HashSet<>();
        final Map<String, ModuleDescriptor> uniqueModules = new HashMap<>();

        getAdditionalFixedElements().forEach(e -> uniqueModules.put(e.getName(), e));

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
        return getModuleNameAndPath(item);
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
        final List<ModuleDescriptor> markedElements = new ArrayList<>(this.fileChooser.getMarkedElements());
        final List<ModuleDescriptor> allElements = new ArrayList<>(markedElements);

        for (int index = 0; index < this.fileChooser.getElementCount(); index++) {
            final ModuleDescriptor element = fileChooser.getElementAt(index);
            if (markedElements.contains(element)) {
                if (element.getImportStatus() != MANDATORY) {
                    element.setImportStatus(UNUSED);
                }
            }
        }

        setList(allElements);

    }

    protected abstract void setList(final List<ModuleDescriptor> allElements);

    protected boolean validateCommon() throws ConfigurationException {
        final Set<ModuleDescriptor> moduleDuplicates = this.calculateSelectedModuleDuplicates();
        final Collection<String> moduleDuplicateNames = new HashSet<>(moduleDuplicates.size());

        for (ModuleDescriptor moduleDuplicate : moduleDuplicates) {
            moduleDuplicateNames.add(this.getModuleNameAndPath(moduleDuplicate));
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
    protected String getModuleNameAndPath(@NotNull final ModuleDescriptor moduleDescriptor) {
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
            .append(moduleDescriptor.getRelativePath())
            .append(')')
            .toString();
    }

}
