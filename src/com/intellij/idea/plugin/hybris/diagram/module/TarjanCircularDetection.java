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

package com.intellij.idea.plugin.hybris.diagram.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

public class TarjanCircularDetection {

    private int index;
    private Collection<ModuleDepDiagramEdge> myEdges;
    private Collection<ModuleDepDiagramItem> myNodes;
    private Stack<ModuleDepDiagramItem> stack;
    private Map<ModuleDepDiagramItem, Integer> indexMap;
    private Map<ModuleDepDiagramItem, Integer> lowLinkMap;

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
        final List<List<ModuleDepDiagramItem>> nodeSegments = computeTarjan();
        int segmentId = 0;
        for (List<ModuleDepDiagramItem> segment : nodeSegments) {
            final int finalSegmentId = segmentId;
            myEdges.stream()
                   .filter(edge -> segment.contains(edge.getSource().getIdentifyingElement()))
                   .filter(edge -> segment.contains(edge.getTarget().getIdentifyingElement()))
                   .forEach(edge -> {
                       edge.setCircleNumber(finalSegmentId);
                       edge.setNumberOfCircles(nodeSegments.size());
                   });
            segmentId++;
        }
    }

    public List<List<ModuleDepDiagramItem>> computeTarjan() {
        this.index = 0;
        stack = new Stack<>();
        List<List<ModuleDepDiagramItem>> result = new ArrayList<>();
        for (ModuleDepDiagramItem v : this.myNodes) {
            if (indexMap.get(v) == null) {
                result.addAll(this.strongConnect(v));
            }
        }
        return result;
    }


    public List<List<ModuleDepDiagramItem>> strongConnect(ModuleDepDiagramItem v) {
        indexMap.put(v, index);
        lowLinkMap.put(v, index);
        index++;
        stack.push(v);
        List<List<ModuleDepDiagramItem>> result = new ArrayList<>();
        for (ModuleDepDiagramItem w : getSuccessors(v)) {
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
            List<ModuleDepDiagramItem> sccList = new ArrayList<>();
            while (true) {
                ModuleDepDiagramItem w = stack.pop();
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

    private List<ModuleDepDiagramItem> getSuccessors(final ModuleDepDiagramItem v) {
        return myEdges.stream()
                      .filter(edge -> edge.getSource().getIdentifyingElement().equals(v))
                      .map(edge -> edge.getTarget().getIdentifyingElement()).collect(Collectors.toList());
    }
}
