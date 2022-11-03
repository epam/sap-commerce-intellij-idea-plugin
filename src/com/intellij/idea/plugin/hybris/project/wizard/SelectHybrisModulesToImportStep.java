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
import com.intellij.ide.util.ElementsChooser;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.project.descriptors.ConfigHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.CustomHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.ExtHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.project.descriptors.PlatformHybrisModuleDescriptor;
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettings;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.ui.table.JBTable;
import org.apache.commons.lang3.BooleanUtils;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.IMPORT_STATUS.MANDATORY;
import static com.intellij.idea.plugin.hybris.project.descriptors.HybrisModuleDescriptor.IMPORT_STATUS.UNUSED;

public class SelectHybrisModulesToImportStep extends AbstractSelectModulesToImportStep implements NonGuiSupport {

    private HybrisModuleDescriptor.IMPORT_STATUS selectionMode = MANDATORY;

    public SelectHybrisModulesToImportStep(final WizardContext context) {
        super(context);
    }

    @Override
    protected void init() {
        this.fileChooser.addElementsMarkListener((ElementsChooser.ElementsMarkListener<HybrisModuleDescriptor>) (element, isMarked) -> {
            if (isMarked) {
                for (HybrisModuleDescriptor moduleDescriptor : element.getDependenciesPlainList()) {
                    if (BooleanUtils.isNotFalse(fileChooser.getElementMarkStates().get(moduleDescriptor))) {
                        continue;
                    }

                    fileChooser.setElementMarked(moduleDescriptor, true);
                    if (selectionMode == MANDATORY) {
                        moduleDescriptor.setImportStatus(MANDATORY);
                    }
                }
            }
            fileChooser.repaint();
        });
    }

    @Override
    public void updateStep() {
        getContext().setCoreStepModuleList();
        super.updateStep();
        selectionMode = MANDATORY;
        for (int index = 0; index < fileChooser.getElementCount(); index++) {
            final HybrisModuleDescriptor hybrisModuleDescriptor = fileChooser.getElementAt(index);
            if (hybrisModuleDescriptor.isPreselected()) {
                fileChooser.setElementMarked(hybrisModuleDescriptor, true);
                hybrisModuleDescriptor.setImportStatus(MANDATORY);
            }
        }
        selectionMode = UNUSED;
        Set<String> duplicateModules = new HashSet<>();
        Set<String> uniqueModules = new HashSet<>();
        getContext().getList().forEach(e->{
            if (uniqueModules.contains(e.getName())) {
                duplicateModules.add(e.getName());
            } else {
                uniqueModules.add(e.getName());
            }
        });
        fileChooser.sort((o1,o2)->{
            boolean o1dup = duplicateModules.contains(o1.getName());
            boolean o2dup = duplicateModules.contains(o2.getName());
            if (o1dup ^ o2dup) {
                return o1dup ? -1 : 1;
            }

            boolean o1custom = o1 instanceof CustomHybrisModuleDescriptor || o1 instanceof ConfigHybrisModuleDescriptor;
            boolean o2custom = o2 instanceof CustomHybrisModuleDescriptor || o2 instanceof ConfigHybrisModuleDescriptor;
            if (o1custom ^ o2custom) {
                return o1custom ? -1 : 1;
            }

            boolean o1selected = o1.getImportStatus() == MANDATORY || o1.isPreselected();
            boolean o2selected = o2.getImportStatus() == MANDATORY || o2.isPreselected();
            if (o1selected ^ o2selected) {
                return o1selected ? -1 : 1;
            }

            return o1.compareTo(o2);
        });
        //scroll to top
        JComponent component = fileChooser.getComponent();
        if (component instanceof JBTable) {
            JBTable table = (JBTable)component;
            table.changeSelection(0, 0, false, false);
        }
    }

    protected void setList(final List<HybrisModuleDescriptor> hybrisModules) {
        getContext().setHybrisModulesToImport(hybrisModules);
    }

    @Override
    public void nonGuiModeImport(final HybrisProjectSettings settings) throws ConfigurationException {
        try {
            final var filteredModuleToImport = getContext().getBestMatchingExtensionsToImport(settings);
            this.getContext().setList(filteredModuleToImport);
        } catch (ConfigurationException e) {
            // no-op already validated
        }
    }

    @Override
    protected boolean isElementEnabled(HybrisModuleDescriptor hybrisModuleDescriptor) {
        if (hybrisModuleDescriptor instanceof ConfigHybrisModuleDescriptor) {
            if (hybrisModuleDescriptor.isPreselected()) {
                return false;
            }
        }
        if (hybrisModuleDescriptor instanceof PlatformHybrisModuleDescriptor) {
            return false;
        }
        if (hybrisModuleDescriptor instanceof ExtHybrisModuleDescriptor) {
            return false;
        }

        return super.isElementEnabled(hybrisModuleDescriptor);
    }

    @Override
    @Nullable
    protected Icon getElementIcon(final HybrisModuleDescriptor module) {
        if (this.isInConflict(module)) {
            return AllIcons.Actions.Cancel;
        }
        if (module instanceof CustomHybrisModuleDescriptor) {
            return AllIcons.Nodes.JavaModule;
        }
        if (module instanceof ConfigHybrisModuleDescriptor) {
            return AllIcons.Nodes.Module;
        }

        return HybrisIcons.HYBRIS_ICON;
    }
}
