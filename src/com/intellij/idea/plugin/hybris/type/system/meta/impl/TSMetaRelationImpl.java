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

package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaDeployment;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModifiers;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.model.Cardinality;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TSMetaRelationImpl extends TSMetaEntityImpl<Relation> implements TSMetaRelation {

    private final TSMetaRelationElement mySourceEnd;
    private final TSMetaRelationElement myTargetEnd;
    private final TSMetaDeployment<TSMetaRelation> myDeployment;
    private final boolean myLocalized;
    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final String myDescription;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public TSMetaRelationImpl(
        final Project project,
        final String name,
        final @NotNull Relation dom
    ) {
        super(project, name, dom);
        myLocalized = Boolean.TRUE.equals(dom.getLocalized().getValue());
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myDescription = dom.getDescription().getStringValue();
        mySourceEnd = new TSMetaRelationElementImpl(project, this, dom.getSourceElement());
        myTargetEnd = new TSMetaRelationElementImpl(project, this, dom.getTargetElement());
        myDeployment = new TSMetaDeploymentImpl<>(project, this, dom.getDeployment());
    }

    @Override
    public TSMetaDeployment<TSMetaRelation> getDeployment() {
        return myDeployment;
    }

    @NotNull
    @Override
    public TSMetaRelation.TSMetaRelationElement getSource() {
        return mySourceEnd;
    }

    @NotNull
    @Override
    public TSMetaRelation.TSMetaRelationElement getTarget() {
        return myTargetEnd;
    }

    @Override
    public boolean isLocalized() {
        return myLocalized;
    }

    @Override
    public boolean isAutoCreate() {
        return myAutoCreate;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }

    @Override
    public String getDescription() {
        return myDescription;
    }

    private static class TSMetaRelationElementImpl extends TSMetaEntityImpl<RelationElement> implements TSMetaRelationElement {

        private final DomAnchor<RelationElement> myDomAnchor;
        private final TSMetaModifiers<TSMetaRelationElement> myModifiers;
        private final TSMetaRelation myOwner;
        private final String myType;
        private final String myQualifier;
        private final String myDescription;
        private final String myMetaType;
        private final boolean myNavigable;
        private final boolean myOrdered;
        private final Cardinality myCardinality;
        private final Type myCollectionType;

        public TSMetaRelationElementImpl(final Project project, final @NotNull TSMetaRelation owner, final @NotNull RelationElement dom) {
            super(project, dom);
            myOwner = owner;
            myDomAnchor = DomService.getInstance().createAnchor(dom);
            myType = StringUtil.notNullize(dom.getType().getStringValue());
            myQualifier = StringUtil.notNullize(dom.getQualifier().getStringValue());
            myNavigable = Optional.ofNullable(dom.getNavigable().getValue()).orElse(true);
            myOrdered = Boolean.TRUE.equals(dom.getOrdered().getValue());
            myDescription = dom.getDescription().getStringValue();
            myCardinality = dom.getCardinality().getValue();
            myMetaType = dom.getMetaType().getStringValue();
            myCollectionType = Optional.ofNullable(dom.getCollectionType().getValue()).orElse(Type.COLLECTION);
            myModifiers = new TSMetaModifiersImpl<>(project, dom.getModifiers());
        }

        @NotNull
        @Override
        public String getType() {
            return myType;
        }

        @Nullable
        @Override
        public RelationElement retrieveDom() {
            return myDomAnchor.retrieveDomElement();
        }

        @NotNull
        @Override
        public String getQualifier() {
            return myQualifier;
        }

        @Override
        public boolean isNavigable() {
            return myNavigable;
        }

        @NotNull
        @Override
        public TSMetaRelation getOwningRelation() {
            return myOwner;
        }

        @Override
        public TSMetaModifiers<TSMetaRelationElement> getModifiers() {
            return myModifiers;
        }

        @Override
        public Cardinality getCardinality() {
            return myCardinality;
        }

        @Override
        public String getDescription() {
            return myDescription;
        }

        @Override
        public Type getCollectionType() {
            return myCollectionType;
        }

        @Override
        public String getMetaType() {
            return myMetaType;
        }

        @Override
        public boolean isOrdered() {
            return myOrdered;
        }
    }
}
