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

package com.intellij.idea.plugin.hybris.diagram.businessProcess.impl;

import com.intellij.diagram.DiagramNodeBase;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramProvider;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Action;
import com.intellij.idea.plugin.hybris.business.process.jaxb.End;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Join;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Notify;
import com.intellij.idea.plugin.hybris.business.process.jaxb.ScriptAction;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Split;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Wait;
import com.intellij.idea.plugin.hybris.business.process.jaxb.model.BpGenericAction;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;


/**
 * Created 11:34 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class BpDiagramFileNode extends DiagramNodeBase<BpGraphNode> {

    private final BpGraphNode diagramNode;

    public BpDiagramFileNode(final BpGraphNode file) {
        super(ApplicationManager.getApplication().getService(BpDiagramProvider.class));
        this.diagramNode = file;
    }

    @NotNull
    @Override
    public String getTooltip() {
        return this.getIdentifyingElement().getGenericAction().getId();
    }

    @Override
    public Icon getIcon() {
        final BpGenericAction genericAction = this.diagramNode.getGenericAction();
        if (genericAction instanceof Action) {
            return HybrisIcons.ACTION;

        } else if (genericAction instanceof Split) {
            return HybrisIcons.SPLIT;

        } else if (genericAction instanceof Wait) {
            return HybrisIcons.WAIT;

        } else if (genericAction instanceof Join) {
            return HybrisIcons.JOIN;

        } else if (genericAction instanceof End) {
            return HybrisIcons.END;

        } else if (genericAction instanceof ScriptAction) {
            return HybrisIcons.SCRIPT;

        } else if (genericAction instanceof Notify) {
            return HybrisIcons.NOTIFY;
        }

        return null;
    }

    @NotNull
    @Override
    public BpGraphNode getIdentifyingElement() {
        return this.diagramNode;
    }
}
