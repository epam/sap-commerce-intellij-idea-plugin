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

// Generated on Sun Jun 05 01:21:13 EEST 2016
// DTD/Schema  :    null

package com.intellij.idea.plugin.hybris.system.type.model;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import org.jetbrains.annotations.NotNull;

/**
 * null:relationsType interface.
 * <pre>
 * <h3>Type null:relationsType documentation</h3>
 * Defines a list of relation types.
 * </pre>
 */
public interface Relations extends DomElement {

    String RELATION = "relation";

    /**
     * Returns the list of relation children.
     * <pre>
     * <h3>Element null:relation documentation</h3>
     * A RelationType defines a n-m or 1-n relation between types.
     * </pre>
     *
     * @return the list of relation children.
     */
    @NotNull
    @SubTagList(RELATION)
    java.util.List<Relation> getRelations();

    /**
     * Adds new child to the list of relation children.
     *
     * @return created child
     */
    @SubTagList("relation")
    Relation addRelation();


}
