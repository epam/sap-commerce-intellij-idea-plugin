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

package com.intellij.idea.plugin.hybris.type.system.meta;

import com.intellij.idea.plugin.hybris.type.system.model.Cardinality;
import com.intellij.idea.plugin.hybris.type.system.model.Relation;
import com.intellij.idea.plugin.hybris.type.system.model.RelationElement;
import com.intellij.idea.plugin.hybris.type.system.model.Type;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TSMetaRelation extends TSMetaClassifier<Relation> {

    TSMetaDeployment<TSMetaRelation> getDeployment();

    @NotNull
    TSMetaRelationElement getSource();

    @NotNull
    TSMetaRelationElement getTarget();

    boolean isLocalized();

    boolean isAutocreate();

    boolean isGenerate();

    String getDescription();

    interface TSMetaRelationElement extends TSMetaClassifier<RelationElement>{

        @NotNull
        String getQualifier();

        @NotNull
        String getType();

        boolean isNavigable();

        @NotNull
        TSMetaRelation getOwningRelation();

        TSMetaModifiers<TSMetaRelationElement> getModifiers();

        @Nullable
        RelationElement retrieveDom();

        Cardinality getCardinality();

        @Nullable
        String getDescription();

        Type getCollectionType();

        @Nullable
        String getMetaType();

        boolean isOrdered();
    }

}
