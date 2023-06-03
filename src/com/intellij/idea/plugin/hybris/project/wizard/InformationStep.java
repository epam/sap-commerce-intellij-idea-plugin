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
import com.intellij.projectImport.ProjectImportWizardStep;
import com.intellij.ui.BrowserHyperlinkListener;

import javax.swing.*;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class InformationStep extends ProjectImportWizardStep {

    private JPanel rootPanel;
    private JLabel importLabel;
    private JLabel cvsLabel;
    private JLabel jrebelLabel;
    private JLabel jiraLabel;
    private JEditorPane jiraEditorPane;
    private JEditorPane informationEditorPane;
    private JEditorPane cvsEditorPane;
    private JEditorPane jrebelEditorPane;

    public InformationStep(final WizardContext wizardContext) {
        super(wizardContext);
    }

    @Override
    public JComponent getComponent() {
        return rootPanel;
    }

    @Override
    public void updateDataModel() {
    }

    @Override
    public boolean isStepVisible() {
        return true;
    }

    private void createUIComponents() {
        final Font font = UIManager.getFont("Label.font");
        final String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
        informationEditorPane = new JEditorPane();
        informationEditorPane.setEditorKit(new HTMLEditorKit());
        ((HTMLDocument) informationEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        cvsEditorPane = new JEditorPane();
        cvsEditorPane.setEditorKit(new HTMLEditorKit());
        ((HTMLDocument) cvsEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        jrebelEditorPane = new JEditorPane();
        jrebelEditorPane.setEditorKit(new HTMLEditorKit());
        ((HTMLDocument) jrebelEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        jiraEditorPane = new JEditorPane();
        jiraEditorPane.setEditorKit(new HTMLEditorKit());
        ((HTMLDocument) jiraEditorPane.getDocument()).getStyleSheet().addRule(bodyRule);
        jiraEditorPane.addHyperlinkListener(BrowserHyperlinkListener.INSTANCE);
    }
}
