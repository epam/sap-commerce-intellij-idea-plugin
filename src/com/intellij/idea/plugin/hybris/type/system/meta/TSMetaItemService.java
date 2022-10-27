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

import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.meta.model.TSMetaRelation;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface TSMetaItemService {
    
    static TSMetaItemService getInstance(final Project project) {
        return project.getService(TSMetaItemService.class);
    }

    List<? extends TSMetaItem.TSMetaItemAttribute> getAttributes(TSMetaItem meta, boolean includeInherited);

    List<? extends TSMetaItem.TSMetaItemIndex> getIndexes(TSMetaItem meta, boolean includeInherited);

    List<? extends TSMetaCustomProperty> getCustomProperties(TSMetaItem meta, boolean includeInherited);

    Collection<? extends TSMetaItem.TSMetaItemAttribute> findAttributesByName(TSMetaItem meta, String name, boolean includeInherited);

    Set<TSMetaItem> getExtends(TSMetaItem meta);

    Stream<? extends TSMetaRelation.TSMetaRelationElement> getReferenceEndsStream(TSMetaItem meta, boolean includeInherited);

    Collection<? extends TSMetaRelation.TSMetaRelationElement> findReferenceEndsByRole(TSMetaItem meta, @NotNull String role, boolean includeInherited);

}
