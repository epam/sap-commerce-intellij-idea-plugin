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

import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.idea.plugin.hybris.project.AbstractHybrisProjectImportBuilder;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.YModuleDescriptorUtil;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.projectImport.SelectImportedProjectsStep;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ui.ImageUtil;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;

import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.IMPORT_STATUS.MANDATORY;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.IMPORT_STATUS.UNUSED;

public abstract class AbstractSelectModulesToImportStep extends SelectImportedProjectsStep<HybrisModuleDescriptor> {

    final static int COLUMN_WIDTH = 300;

    public AbstractSelectModulesToImportStep(final WizardContext context) {
        super(context);
        init();
    }

    protected void init() {
    }

    @Override
    public AbstractHybrisProjectImportBuilder getContext() {
        return (AbstractHybrisProjectImportBuilder) this.getBuilder();
    }

    protected boolean isInConflict(@NotNull final HybrisModuleDescriptor item) {
        Validate.notNull(item);

        return (this.fileChooser.getMarkedElements().contains(item) || getAdditionalFixedElements().contains(item))
               && this.calculateSelectedModuleDuplicates().contains(item);
    }

    protected List<HybrisModuleDescriptor> getAdditionalFixedElements() {
        return Collections.emptyList();
    }

    @NotNull
    protected Set<HybrisModuleDescriptor> calculateSelectedModuleDuplicates() {
        final Set<HybrisModuleDescriptor> duplicateModules = new HashSet<>();
        final Map<String, HybrisModuleDescriptor> uniqueModules = new HashMap<>();

        getAdditionalFixedElements().forEach(e -> uniqueModules.put(e.getName(), e));

        for (HybrisModuleDescriptor moduleDescriptor : this.fileChooser.getMarkedElements()) {

            final HybrisModuleDescriptor alreadySelected = uniqueModules.get(moduleDescriptor.getName());

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
    protected String getElementText(final HybrisModuleDescriptor item) {
        return getModuleNameAndPath(item);
    }

    @Override
    public boolean validate() throws ConfigurationException {
        validateCommon();

        if (this.fileChooser.getMarkedElements().isEmpty()) {
            throw new ConfigurationException(
                HybrisI18NBundleUtils.message("hybris.project.import.error.nothing.found.to.import"),
                HybrisI18NBundleUtils.message("hybris.project.import.error.unable.to.proceed")
            );
        }

        return true;
    }

    @Override
    public void onStepLeaving() {
        super.onStepLeaving();
        final List<HybrisModuleDescriptor> markedElements = new ArrayList<>(this.fileChooser.getMarkedElements());
        final List<HybrisModuleDescriptor> allElements = new ArrayList<>(markedElements);

        for (int index = 0; index < this.fileChooser.getElementCount(); index++) {
            final HybrisModuleDescriptor element = fileChooser.getElementAt(index);
            if (markedElements.contains(element)) {
                if (element.getImportStatus() != MANDATORY) {
                    element.setImportStatus(UNUSED);
                }
            }
        }

        setList(allElements);

    }

    protected abstract void setList(final List<HybrisModuleDescriptor> allElements);

    protected boolean validateCommon() throws ConfigurationException {
        final Set<HybrisModuleDescriptor> moduleDuplicates = this.calculateSelectedModuleDuplicates();
        final Collection<String> moduleDuplicateNames = new HashSet<>(moduleDuplicates.size());

        for (HybrisModuleDescriptor moduleDuplicate : moduleDuplicates) {
            moduleDuplicateNames.add(this.getModuleNameAndPath(moduleDuplicate));
        }

        if (!moduleDuplicates.isEmpty()) {
            throw new ConfigurationException(
                HybrisI18NBundleUtils.message(
                    "hybris.project.import.duplicate.projects.found",
                    StringUtil.join(ArrayUtil.toStringArray(moduleDuplicateNames), "\n")
                ),
                HybrisI18NBundleUtils.message("hybris.project.error")
            );
        }

        return true;
    }

    /*
     * Aligned text to COLUMN_WIDTH. It is not precise by space pixel width (4pixels)
     */
    @NotNull
    protected String getModuleNameAndPath(@NotNull final HybrisModuleDescriptor moduleDescriptor) {
        Validate.notNull(moduleDescriptor);

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
            .append(YModuleDescriptorUtil.INSTANCE.getRelativePath(moduleDescriptor))
            .append(')')
            .toString();
    }

}
