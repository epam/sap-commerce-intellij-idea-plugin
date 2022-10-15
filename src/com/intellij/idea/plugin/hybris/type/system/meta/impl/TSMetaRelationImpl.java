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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClassifier;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelAccess;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaRelation;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.xml.DomAnchor;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class TSMetaRelationImpl extends TSMetaEntityImpl<Relation> implements TSMetaRelation {

    private final ReferenceEnd mySourceEnd;
    private final ReferenceEnd myTargetEnd;
    private final String myTypeCode;

    @SuppressWarnings("ThisEscapedInObjectConstruction")
    public TSMetaRelationImpl(
        final Project project,
        final String name,
        final String typeCode,
        final @NotNull Relation dom
    ) {
        super(project, name, dom);
        myTypeCode = typeCode;
        mySourceEnd = new ReferenceEndImpl(project, this, dom.getSourceElement());
        myTargetEnd = new ReferenceEndImpl(project, this, dom.getTargetElement());
    }

    @Override
    public String getTypeCode() {
        return myTypeCode;
    }

    @NotNull
    @Override
    public ReferenceEnd getSource() {
        return mySourceEnd;
    }

    @NotNull
    @Override
    public ReferenceEnd getTarget() {
        return myTargetEnd;
    }

    private static class ReferenceEndImpl implements ReferenceEnd {

        private final Project myProject;
        private final DomAnchor<RelationElement> myDomAnchor;
        private final TSMetaRelation myOwner;
        private final String myTypeName;
        private final String myRole;
        private final boolean myNavigatable;

        public ReferenceEndImpl(
            final Project project,
            final @NotNull TSMetaRelation owner,
            final @NotNull RelationElement dom
        ) {
            myProject = project;
            myOwner = owner;
            myDomAnchor = DomService.getInstance().createAnchor(dom);
            myTypeName = StringUtil.notNullize(dom.getType().getStringValue());
            myRole = StringUtil.notNullize(dom.getQualifier().getStringValue());
            myNavigatable = Optional.ofNullable(dom.getNavigable().getValue()).orElse(true);
        }

        @NotNull
        @Override
        public String getTypeName() {
            return myTypeName;
        }

        @Nullable
        @Override
        public TSMetaClassifier<?> resolveType() {
            return TSMetaModelAccess.Companion.getInstance(myProject).findMetaItemByName(getTypeName());
        }

        @Nullable
        @Override
        public RelationElement retrieveDom() {
            return myDomAnchor.retrieveDomElement();
        }

        @NotNull
        @Override
        public String getRole() {
            return myRole;
        }

        @Override
        public boolean isNavigable() {
            return myNavigatable;
        }

        @NotNull
        @Override
        public TSMetaRelation getOwningReference() {
            return myOwner;
        }

    }
}
