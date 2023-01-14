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

import com.intellij.diagram.DiagramDataModel;
import com.intellij.diagram.DiagramNode;
import com.intellij.diagram.DiagramRelationshipInfoAdapter;
import com.intellij.diagram.presentation.DiagramLineType;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.diagram.businessProcess.BpDiagramProvider;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 11:11 PM 31 January 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public final class BpDiagramDataModel extends DiagramDataModel<BpGraphNode> {

    private final Collection<BpDiagramFileEdge> edges = new ArrayList<>();
    private final Map<String, BpDiagramFileNode> nodesMap = new HashMap<>();

    public BpDiagramDataModel(final Project project, final BpGraphNode rootBpGraphNode) {
        super(project, ApplicationManager.getApplication().getService(BpDiagramProvider.class));

        for (BpGraphNode bpGraphNode : rootBpGraphNode.getNodesMap().values()) {
            final BpDiagramFileNode bpDiagramFileNode = new BpDiagramFileNode(bpGraphNode);

            this.nodesMap.put(bpGraphNode.getGenericAction().getId(), bpDiagramFileNode);
        }
    }

    @NotNull
    @Override
    public Collection<BpDiagramFileNode> getNodes() {
        return this.nodesMap.values();
    }

    @NotNull
    @Override
    public Collection<BpDiagramFileEdge> getEdges() {
        return this.edges;
    }

    @NotNull
    @Override
    public String getNodeName(final DiagramNode<BpGraphNode> diagramNode) {
        return diagramNode.getIdentifyingElement().getGenericAction().getId();
    }

    @Contract(value = "_ -> null", pure = true)
    @Nullable
    @Override
    public BpDiagramFileNode addElement(final BpGraphNode t) {
        return null;
    }

    @Override
    public void refreshDataModel() {
        this.edges.clear();

        for (BpDiagramFileNode sourceBpDiagramFileNode : this.nodesMap.values()) {
            final BpGraphNode sourceBpGraphNode = sourceBpDiagramFileNode.getIdentifyingElement();

            for (Map.Entry<String, BpGraphNode> transition : sourceBpGraphNode.getTransitions().entrySet()) {
                final String transitionName = transition.getKey();

                final BpGraphNode targetBpGraphNode = transition.getValue();
                if (null != targetBpGraphNode) {
                    final BpDiagramFileNode targetBpDiagramFileNode = this.nodesMap.get(targetBpGraphNode.getGenericAction()
                                                                                                         .getId());

                    final BpDiagramFileEdge edge = new BpDiagramFileEdge(
                        sourceBpDiagramFileNode, targetBpDiagramFileNode, new BpDiagramRelationship(transitionName)
                    );

                    this.edges.add(edge);
                }
            }
        }
    }

    @Contract(pure = true)
    @NotNull
    @Override
    public ModificationTracker getModificationTracker() {
        return this;
    }

    @Override
    public void dispose() {

    }

    protected static class BpDiagramRelationship extends DiagramRelationshipInfoAdapter {

        public BpDiagramRelationship(final String label) {
            super(label, DiagramLineType.SOLID, label, "", "", 1);
        }

        @Override
        public Shape getStartArrow() {
            return STANDARD;
        }
    }
}
