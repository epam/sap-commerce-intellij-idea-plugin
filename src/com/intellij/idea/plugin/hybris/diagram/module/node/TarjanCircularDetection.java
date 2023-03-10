/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.diagram.module.node;

import com.intellij.idea.plugin.hybris.diagram.module.node.graph.ModuleDepGraphNode;

import java.util.*;
import java.util.stream.Collectors;

public class TarjanCircularDetection {

    private int index;
    private Collection<ModuleDepDiagramEdge> myEdges;
    private Collection<ModuleDepGraphNode> myNodes;
    private Stack<ModuleDepGraphNode> stack;
    private Map<ModuleDepGraphNode, Integer> indexMap;
    private Map<ModuleDepGraphNode, Integer> lowLinkMap;

    public TarjanCircularDetection(
        final Collection<ModuleDepDiagramNode> diagramNodes,
        final Collection<ModuleDepDiagramEdge> myEdges
    ) {
        this.index = 0;
        this.myEdges = myEdges;
        this.myNodes = diagramNodes.stream().map(node -> node.getIdentifyingElement()).collect(Collectors.toList());
        this.indexMap = new HashMap<>();
        this.lowLinkMap = new HashMap<>();
    }

    public void detectAndMarkCircles() {
        final List<List<ModuleDepGraphNode>> nodeSegments = computeTarjan();
        int segmentId = 0;
        for (List<ModuleDepGraphNode> segment : nodeSegments) {
            final int finalSegmentId = segmentId;
            myEdges.stream()
                .filter(edge -> segment.contains(edge.getSource().getIdentifyingElement()))
                .filter(edge -> segment.contains(edge.getTarget().getIdentifyingElement()))
                .forEach(edge -> {
                    edge.circleNumber = finalSegmentId;
                    edge.numberOfCircles = nodeSegments.size();
                });
            segmentId++;
        }
    }

    public List<List<ModuleDepGraphNode>> computeTarjan() {
        this.index = 0;
        stack = new Stack<>();
        List<List<ModuleDepGraphNode>> result = new ArrayList<>();
        for (ModuleDepGraphNode v : this.myNodes) {
            if (indexMap.get(v) == null) {
                result.addAll(this.strongConnect(v));
            }
        }
        return result;
    }


    public List<List<ModuleDepGraphNode>> strongConnect(ModuleDepGraphNode v) {
        indexMap.put(v, index);
        lowLinkMap.put(v, index);
        index++;
        stack.push(v);
        List<List<ModuleDepGraphNode>> result = new ArrayList<>();
        for (ModuleDepGraphNode w : getSuccessors(v)) {
            if (indexMap.get(w) == null) {
                result.addAll(strongConnect(w));
                lowLinkMap.put(v, Math.min(lowLinkMap.get(v), lowLinkMap.get(w)));
            } else {
                if (stack.contains(w)) {
                    lowLinkMap.put(v, Math.min(lowLinkMap.get(v), indexMap.get(w)));
                }
            }
        }

        if (lowLinkMap.get(v).equals(indexMap.get(v))) {
            List<ModuleDepGraphNode> sccList = new ArrayList<>();
            while (true) {
                ModuleDepGraphNode w = stack.pop();
                sccList.add(w);
                if (w.equals(v)) {
                    break;
                }
            }
            if (sccList.size() > 1) {
                result.add(sccList);
            } // don't return trivial sccs in the form of single nodes.
        }
        return result;
    }

    private List<ModuleDepGraphNode> getSuccessors(final ModuleDepGraphNode v) {
        return myEdges.stream()
            .filter(edge -> edge.getSource().getIdentifyingElement().equals(v))
            .map(edge -> edge.getTarget().getIdentifyingElement()).collect(Collectors.toList());
    }
}
