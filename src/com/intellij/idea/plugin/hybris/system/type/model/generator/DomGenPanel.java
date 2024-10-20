/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2014-2016 Alexander Bartash <AlexanderBartash@gmail.com>
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ArrayUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class DomGenPanel {

    private JPanel mainPanel;
    private JTextField myNamespace;
    private JTextField mySuperClass;
    private TextFieldWithBrowseButton mySchemaLocation;
    private JTextField myPackage;
    private TextFieldWithBrowseButton myOutputDir;
    private JTextArea mySkipSchemas;
    private JTextField myAuthor;
    private JBCheckBox myUseQualifiedClassNames;
    private final Project myProject;

    public DomGenPanel(final Project project) {
        myProject = project;
    }

    private void createUIComponents() {
        mySchemaLocation = new TextFieldWithBrowseButton();
        mySchemaLocation.addBrowseFolderListener(
            myProject,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
                .withExtensionFilter("Files (.xsd, .dtd)", "xsd", "dtd")
                .withTitle("Choose XSD or DTD Schema")
                .withDescription("Make sure there are only necessary schemes in directory where your XSD or DTD schema is located")
        );
        mySchemaLocation.getTextField().setEditable(false);
        mySchemaLocation.addActionListener(e -> {
            final File file = new File(mySchemaLocation.getText());
            if (file.exists() && file.getName().toLowerCase().endsWith(".xsd")) {
                final VirtualFile vf = LocalFileSystem.getInstance().findFileByIoFile(file);
                if (vf != null) {
                    final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(vf);
                    if (psiFile instanceof XmlFile) {
                        final XmlDocument xml = ((XmlFile) psiFile).getDocument();
                        if (xml != null) {
                            final XmlTag rootTag = xml.getRootTag();
                            if (rootTag != null) {
                                String target = null;
                                final ArrayList<String> ns = new ArrayList<>();
                                for (XmlAttribute attr : rootTag.getAttributes()) {
                                    if ("targetNamespace".equals(attr.getName())) {
                                        target = attr.getValue();
                                    } else if (attr.getName().startsWith("xmlns")) {
                                        ns.add(attr.getValue());
                                    }
                                }

                                ns.remove(target);
                                if (target != null) {
                                    myNamespace.setText(target);
                                }
                                mySkipSchemas.setText(StringUtil.join(ArrayUtil.toStringArray(ns), "\n"));
                            }
                        }
                    }
                }
            }
        });
        myOutputDir = new TextFieldWithBrowseButton();
        final FileChooserDescriptor descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
            .withTitle("Select Output Directory For Generated Files");
        myOutputDir.addBrowseFolderListener(myProject, descriptor);
    }

    public JComponent getComponent() {
        return mainPanel;
    }

    public NamespaceDesc getNamespaceDescriptor() {
        return new NamespaceDesc(
            myNamespace.getText().trim(),
            myPackage.getText().trim(),
            mySuperClass.getText().trim(),
            "",
            null,
            null,
            null,
            null
        );
    }

    public String getLocation() {
        return mySchemaLocation.getText();
    }

    public String getOutputDir() {
        return myOutputDir.getText();
    }

    private static final String PREFIX = "DomGenPanel.";

    public void restore() {
        myNamespace.setText(getValue("namespace", ""));
        myPackage.setText(getValue("package", "com.intellij.myframework.model"));
        mySchemaLocation.setText(getValue("schemaLocation", ""));
        mySuperClass.setText(getValue("superClass", "com.intellij.util.xml.DomElement"));
        myOutputDir.setText(getValue("output", ""));
        mySkipSchemas.setText(getValue(
            "skipSchemas",
            "http://www.w3.org/2001/XMLSchema\nhttp://www.w3.org/2001/XMLSchema-instance"
        ));
        myAuthor.setText(getValue("author", ""));
        myUseQualifiedClassNames.setSelected(getValue("useFQNs", "false").equals("true"));
    }

    private static String getValue(final String name, final String defaultValue) {
        return PropertiesComponent.getInstance().getValue(PREFIX + name, defaultValue);
    }

    private static void setValue(final String name, final String value) {
        PropertiesComponent.getInstance().setValue(PREFIX + name, value);
    }

    public void saveAll() {
        setValue("namespace", myNamespace.getText());
        setValue("package", myPackage.getText());
        setValue("schemaLocation", mySchemaLocation.getText());
        setValue("superClass", mySuperClass.getText());
        setValue("output", myOutputDir.getText());
        setValue("skipSchemas", mySkipSchemas.getText());
        setValue("author", myAuthor.getText());
        setValue("useFQNs", myUseQualifiedClassNames.isSelected() ? "true" : "false");
    }

    public boolean validate() {
        if (!new File(mySchemaLocation.getText()).exists()) {
            Messages.showErrorDialog(myProject, "Schema location doesn't exist", "Error");
            IdeFocusManager.getInstance(myProject).requestFocus(mySchemaLocation, true);
            return false;
        }

        if (!new File(myOutputDir.getText()).exists()) {
            Messages.showErrorDialog(myProject, "Output dir doesn't exist", "Error");
            IdeFocusManager.getInstance(myProject).requestFocus(myOutputDir, true);
            return false;
        }

        return true;
    }

    public String[] getSkippedSchemas() {
        final String schemes = mySkipSchemas.getText().replaceAll("\r\n", "\n").trim();
        if (schemes.length() > 0) {
            return schemes.split("\n");
        }
        return new String[0];
    }

    public String getAuthor() {
        return myAuthor.getText();
    }

    public boolean isUseQualifiedClassNames() {
        return myUseQualifiedClassNames.isSelected();
    }
}
