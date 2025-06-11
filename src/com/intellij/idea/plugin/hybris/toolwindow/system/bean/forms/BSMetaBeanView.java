/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
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

package com.intellij.idea.plugin.hybris.toolwindow.system.bean.forms;

import com.intellij.idea.plugin.hybris.system.bean.meta.model.*;
import com.intellij.idea.plugin.hybris.system.bean.psi.BSPsiHelper;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaAnnotationsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaHintsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaImportsTable;
import com.intellij.idea.plugin.hybris.toolwindow.system.bean.components.BSMetaPropertiesTable;
import com.intellij.idea.plugin.hybris.toolwindow.ui.AbstractTable;
import com.intellij.openapi.project.Project;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPanel;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;

/**
 * UI view class representing a metadata editor for a global bean in SAP Commerce.
 *
 * This panel displays and allows editing of bean metadata, including properties,
 * hints, imports, annotations, and various flags.
 */
public class BSMetaBeanView {

    private final Project myProject;
    private BSGlobalMetaBean myMeta;
    private JPanel myContentPane;
    private JBTextField myDescription;
    private JBTextField myClass;
    private JBCheckBox myDeprecated;
    private JBTextField myTemplate;
    private JPanel myPropertiesPane;
    private JBPanel myDetailsPane;
    private JPanel myFlagsPane;
    private JBTextField myDeprecatedSince;
    private JBCheckBox mySuperEquals;
    private JPanel myHintsPane;
    private JBTextField myExtends;
    private JPanel myImportsPane;
    private JPanel myAnnotationsPane;
    private JBCheckBox myAbstract;
    private AbstractTable<BSGlobalMetaBean, BSMetaProperty> myProperties;
    private AbstractTable<BSGlobalMetaBean, BSMetaHint> myHints;
    private AbstractTable<BSGlobalMetaBean, BSMetaImport> myImports;
    private AbstractTable<BSGlobalMetaBean, BSMetaAnnotations> myAnnotations;

    /**
     * Constructs a new instance of the metadata bean view.
     *
     * @param project the current IntelliJ IDEA project context
     */
    public BSMetaBeanView(final Project project) {
        this.myProject = project;
    }

    /**
     * Initializes or updates UI components with the given metadata bean.
     * Skips re-initialization if the same object is provided.
     *
     * @param myMeta the metadata bean to load into the form
     */
    private void initData(final BSGlobalMetaBean myMeta) {
        if (Objects.equals(this.myMeta, myMeta)) {
            // same object, no need in re-init
            return;
        }
        this.myMeta = myMeta;

        myClass.setText(myMeta.getFullName());
        myExtends.setText(myMeta.getFullExtends());
        myTemplate.setText(myMeta.getTemplate());
        myDescription.setText(myMeta.getDescription());
        myDeprecatedSince.setText(myMeta.getDeprecatedSince());
        myDeprecated.setSelected(myMeta.isDeprecated());
        myAbstract.setSelected(myMeta.isAbstract());
        mySuperEquals.setSelected(myMeta.isSuperEquals());

        myProperties.updateModel(myMeta);
        myHints.updateModel(myMeta);
        myImports.updateModel(myMeta);
        myAnnotations.updateModel(myMeta);
    }

    /**
     * Gets the content panel and initializes it with the given metadata bean.
     *
     * @param meta the metadata bean to display
     * @return the initialized content panel
     */
    public JPanel getContent(final BSGlobalMetaBean meta) {
        initData(meta);

        return myContentPane;
    }

    /**
     * Gets the content panel initialized with a specific metadata bean and selects a property.
     *
     * @param meta the metadata bean to display
     * @param metaValue the property to select in the table
     * @return the initialized content panel
     */
    public JPanel getContent(final BSGlobalMetaBean meta, final BSMetaProperty metaValue) {
        initData(meta);

        myProperties.select(metaValue);

        return myContentPane;
    }

    /**
     * Creates and configures custom Swing components for displaying metadata details,
     * such as tables for properties, hints, imports, and annotations.
     * Also sets up popup handlers and component borders.
     */
    private void createUIComponents() {
        myProperties = BSMetaPropertiesTable.getInstance(myProject);
        myHints = BSMetaHintsTable.getInstance(myProject);
        myImports = BSMetaImportsTable.getInstance(myProject);
        myAnnotations = BSMetaAnnotationsTable.getInstance(myProject);

        myPropertiesPane = ToolbarDecorator.createDecorator(myProperties)
            .setRemoveAction(anActionButton -> Optional.ofNullable(myProperties.getCurrentItem())
                .ifPresent(it -> BSPsiHelper.INSTANCE.delete(myProject, myMeta, it)))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myProperties.getCurrentItem())
                .map(BSMetaClassifier::isCustom)
                .orElse(false))
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myHintsPane = ToolbarDecorator.createDecorator(myHints)
            .setRemoveAction(anActionButton -> Optional.ofNullable(myHints.getCurrentItem())
                .ifPresent(it -> BSPsiHelper.INSTANCE.delete(myProject, myMeta, it)))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myHints.getCurrentItem())
                .map(BSMetaClassifier::isCustom)
                .orElse(false))
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myImportsPane = ToolbarDecorator.createDecorator(myImports)
            .setRemoveAction(anActionButton -> Optional.ofNullable(myImports.getCurrentItem())
                .ifPresent(it -> BSPsiHelper.INSTANCE.delete(myProject, myMeta, it)))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myImports.getCurrentItem())
                .map(BSMetaClassifier::isCustom)
                .orElse(false))
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myAnnotationsPane = ToolbarDecorator.createDecorator(myAnnotations)
            .setRemoveAction(anActionButton -> Optional.ofNullable(myAnnotations.getCurrentItem())
                .ifPresent(it -> BSPsiHelper.INSTANCE.delete(myProject, myMeta, it)))
            .setRemoveActionUpdater(e -> Optional.ofNullable(myAnnotations.getCurrentItem())
                .map(BSMetaClassifier::isCustom)
                .orElse(false))
            .disableUpDownActions()
            .setPanelBorder(JBUI.Borders.empty())
            .createPanel();
        myDetailsPane = new JBPanel();
        myFlagsPane = new JBPanel();

        myDetailsPane.setBorder(IdeBorderFactory.createTitledBorder("Details"));
        myFlagsPane.setBorder(IdeBorderFactory.createTitledBorder("Flags"));
        myPropertiesPane.setBorder(IdeBorderFactory.createTitledBorder("Properties"));
        myHintsPane.setBorder(IdeBorderFactory.createTitledBorder("Hints"));
        myImportsPane.setBorder(IdeBorderFactory.createTitledBorder("Imports"));
        myAnnotationsPane.setBorder(IdeBorderFactory.createTitledBorder("Annotations"));

        PopupHandler.installPopupMenu(myProperties, "BSView.ToolWindow.TablePopup", "BSView.ToolWindow.TablePopup");
        PopupHandler.installPopupMenu(myHints, "BSView.ToolWindow.TablePopup", "BSView.ToolWindow.TablePopup");
        PopupHandler.installPopupMenu(myImports, "BSView.ToolWindow.TablePopup", "BSView.ToolWindow.TablePopup");
        PopupHandler.installPopupMenu(myAnnotations, "BSView.ToolWindow.TablePopup", "BSView.ToolWindow.TablePopup");
    }
}
