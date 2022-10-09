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

package com.intellij.idea.plugin.hybris.business.process.common.impl;

import com.intellij.idea.plugin.hybris.business.process.common.BpGraphNode;
import com.intellij.idea.plugin.hybris.business.process.common.BpGraphService;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Action;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Choice;
import com.intellij.idea.plugin.hybris.business.process.jaxb.End;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Join;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Notify;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Process;
import com.intellij.idea.plugin.hybris.business.process.jaxb.ScriptAction;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Split;
import com.intellij.idea.plugin.hybris.business.process.jaxb.TargetNode;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Transition;
import com.intellij.idea.plugin.hybris.business.process.jaxb.Wait;
import com.intellij.idea.plugin.hybris.business.process.jaxb.model.BpGenericAction;
import com.intellij.idea.plugin.hybris.business.process.jaxb.services.BpJaxbService;
import com.intellij.idea.plugin.hybris.common.utils.HybrisI18NBundleUtils;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.UnmarshalException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created 9:35 PM 02 February 2016.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class DefaultBpGraphService implements BpGraphService {

    private static final Logger LOG = Logger.getInstance(DefaultBpGraphService.class);

    @Nullable
    @Override
    public BpGraphNode buildGraphFromXmlFile(@NotNull final VirtualFile virtualFile) throws UnmarshalException {
        Validate.notNull(virtualFile);

        final BpJaxbService bpJaxbService = ApplicationManager.getApplication().getService(BpJaxbService.class);

        final Process process = bpJaxbService.unmarshallBusinessProcessXml(VfsUtil.virtualToIoFile(virtualFile));

        if (CollectionUtils.isEmpty(process.getNodes())) {
            return null;
        }

        final Map<String, BpGraphNode> nodesMap = this.buildNodesMap(virtualFile, process);

        this.populateNodesTransitions(virtualFile, process, nodesMap);

        return nodesMap.get(process.getStart());
    }

    protected void populateNodesTransitions(
        @NotNull final VirtualFile virtualFile,
        @NotNull final Process process,
        @NotNull final Map<String, BpGraphNode> nodesMap
    ) {
        Validate.notNull(virtualFile);
        Validate.notNull(process);
        Validate.notNull(nodesMap);

        for (BpGenericAction genericAction : process.getNodes()) {
            final BpGraphNode actionGraphNode = nodesMap.get(genericAction.getId());

            for (Map.Entry<String, String> transition : this.getTransitionIdsForAction(genericAction).entrySet()) {
                actionGraphNode.getTransitions().put(transition.getKey(), nodesMap.get(transition.getValue()));
            }
        }
    }

    @NotNull
    protected Map<String, String> getTransitionIdsForAction(final BpGenericAction genericAction) {
        Validate.notNull(genericAction);

        final Map<String, String> transitionsIds = new HashMap<String, String>();

        if (genericAction instanceof Action) {
            final Action action = (Action) genericAction;

            for (Transition transition : action.getTransition()) {
                transitionsIds.put(transition.getName(), transition.getTo());
            }

        } else if (genericAction instanceof Split) {
            final Split split = (Split) genericAction;

            for (TargetNode targetNode : split.getTargetNode()) {
                transitionsIds.put("", targetNode.getName());
            }

        } else if (genericAction instanceof Wait) {
            final Wait wait = (Wait) genericAction;

            transitionsIds.put("", wait.getThen());

            if (null != wait.getCase() && CollectionUtils.isNotEmpty(wait.getCase().getChoice())) {

                for (Choice choice : wait.getCase().getChoice()) {
                    transitionsIds.put(choice.getId(), choice.getThen());
                }
            }

            if (null != wait.getTimeout()) {

                transitionsIds.put(
                    HybrisI18NBundleUtils.message("hybris.business.process.timeout") + '\n' + wait.getTimeout()
                                                                                                  .getDelay(),
                    wait.getTimeout().getThen()
                );
            }

        } else if (genericAction instanceof Join) {
            final Join join = (Join) genericAction;

            transitionsIds.put("", join.getThen());

        } else if (genericAction instanceof End) {
            final End end = (End) genericAction;

        } else if (genericAction instanceof ScriptAction) {
            final ScriptAction scriptAction = (ScriptAction) genericAction;

            for (Transition transition : scriptAction.getTransition()) {
                transitionsIds.put("", transition.getTo());
            }

        } else if (genericAction instanceof Notify) {
            final Notify notify = (Notify) genericAction;

            transitionsIds.put("", notify.getThen());
        }

        return transitionsIds;
    }

    @NotNull
    protected Map<String, BpGraphNode> buildNodesMap(
        @NotNull final VirtualFile virtualFile,
        @NotNull final Process process
    ) {
        Validate.notNull(virtualFile);
        Validate.notNull(process);

        final Map<String, BpGraphNode> nodesMap = new HashMap<String, BpGraphNode>();

        for (BpGenericAction action : process.getNodes()) {
            nodesMap.put(action.getId(), new DefaultBpGraphNode(action, nodesMap, virtualFile, process));
        }

        return nodesMap;
    }
}
