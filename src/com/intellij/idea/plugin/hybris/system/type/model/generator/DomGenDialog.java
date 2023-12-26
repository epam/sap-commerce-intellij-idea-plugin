/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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
package com.intellij.idea.plugin.hybris.system.type.model.generator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;

import javax.swing.*;
import java.io.File;

public class DomGenDialog extends DialogWrapper {

    final DomGenPanel panel;
    final JComponent comp;

    protected DomGenDialog(final Project project) {
        super(project);
        panel = new DomGenPanel(project);
        comp = panel.getComponent();
        panel.restore();
        setTitle("Generate DOM Model From XSD or DTD");
        init();
        getOKAction().putValue(Action.NAME, "Generate");
    }

    @Override
    protected JComponent createCenterPanel() {
        return comp;
    }

    @Override
    protected void doOKAction() {
        if (!panel.validate()) {
            return;
        }
        final String location = panel.getLocation();
        final ModelLoader loader = location.toLowerCase().endsWith(".xsd") ? new XSDModelLoader() : new DTDModelLoader();
        final JetBrainsEmitter emitter = new JetBrainsEmitter();
        final MergingFileManager fileManager = new MergingFileManager();
        if (panel.getAuthor().trim().length() > 0) {
            emitter.setAuthor(panel.getAuthor());
        }
        if (panel.isUseQualifiedClassNames()) {
            emitter.enableQualifiedClassNames();
        }
        final ModelGen modelGen = new ModelGen(loader, emitter, fileManager);
        final NamespaceDesc desc = panel.getNamespaceDescriptor();
        modelGen.setConfig(desc.name, location, desc, panel.getSkippedSchemas());
        try {
            final File output = new File(panel.getOutputDir());
            modelGen.perform(output, new File(location).getParentFile());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        panel.saveAll();
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        panel.saveAll();
        super.doCancelAction();
    }

    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }
}
