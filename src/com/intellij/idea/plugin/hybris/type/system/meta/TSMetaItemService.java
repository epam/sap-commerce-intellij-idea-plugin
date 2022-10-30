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

package com.intellij.idea.plugin.hybris.type.system.meta;

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSGlobalMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface TSMetaItemService {
    
    static TSMetaItemService getInstance(final Project project) {
        return project.getService(TSMetaItemService.class);
    }

    Collection<? extends TSGlobalMetaItem.TSGlobalMetaItemAttribute> getAttributes(TSGlobalMetaItem meta, boolean includeInherited);

    Collection<? extends TSMetaItem.TSMetaItemIndex> getIndexes(TSGlobalMetaItem meta, boolean includeInherited);

    Collection<? extends TSMetaCustomProperty> getCustomProperties(TSGlobalMetaItem meta, boolean includeInherited);

    List<? extends TSGlobalMetaItem.TSGlobalMetaItemAttribute> findAttributesByName(TSGlobalMetaItem meta, String name, boolean includeInherited);

    Set<TSGlobalMetaItem> getExtends(TSGlobalMetaItem meta);

    List<? extends TSMetaRelation.TSMetaRelationElement> getRelationEnds(TSGlobalMetaItem meta, boolean includeInherited);

    List<? extends TSMetaRelation.TSMetaRelationElement> findReferenceEndsByQualifier(TSGlobalMetaItem meta, @NotNull String qualifier, boolean includeInherited);

}
