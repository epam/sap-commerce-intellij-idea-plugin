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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive;
import com.intellij.util.xml.DomElement;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TSMetaCache {

    private final Map<MetaType, CaseInsensitiveMap<String, TSMetaClassifier<? extends DomElement>>> metaCache = new ConcurrentHashMap<>();
    private final CaseInsensitive.NoCaseMultiMap<TSMetaReference.ReferenceEnd> myReferencesBySourceTypeName = new CaseInsensitive.NoCaseMultiMap<>();

    @SuppressWarnings("unchecked")
    public <T> CaseInsensitiveMap<String, T> getMetaType(final MetaType metaType) {
        return (CaseInsensitiveMap<String, T>) metaCache.computeIfAbsent(metaType, mt -> new CaseInsensitiveMap<>());
    }

    public CaseInsensitive.NoCaseMultiMap<TSMetaReference.ReferenceEnd> getReferencesBySourceTypeName() {
        return myReferencesBySourceTypeName;
    }
}
