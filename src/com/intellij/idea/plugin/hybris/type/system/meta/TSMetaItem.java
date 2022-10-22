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

import com.intellij.idea.plugin.hybris.type.system.meta.impl.CaseInsensitive;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.idea.plugin.hybris.type.system.model.CreationMode;
import com.intellij.idea.plugin.hybris.type.system.model.Index;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.openapi.module.Module;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public interface TSMetaItem extends TSMetaClassifier<ItemType>, TSMetaSelfMerge<TSMetaItem>  {

    String IMPLICIT_SUPER_CLASS_NAME = "GenericItem";

    @Nullable
    String getExtendedMetaItemName();

    void addAttribute(String key, TSMetaItemAttribute attribute);

    void addCustomProperty(String key, TSMetaCustomProperty customProperty);

    void addIndex(String key, TSMetaItemIndex index);

    CaseInsensitive.NoCaseMultiMap<TSMetaItemAttribute> getAttributes();

    CaseInsensitive.NoCaseMultiMap<TSMetaCustomProperty> getCustomAttributes();

    CaseInsensitive.NoCaseMultiMap<TSMetaItemIndex> getIndexes();

    @NotNull
    Stream<? extends ItemType> retrieveAllDomsStream();

    TSMetaDeployment<TSMetaItem> getDeployment();

    boolean isAbstract();

    boolean isAutoCreate();

    boolean isGenerate();

    boolean isSingleton();

    boolean isJaloOnly();

    String getJaloClass();

    String getDescription();

    interface TSMetaItemIndex {

        Module getModule();

        @Nullable
        String getName();

        @Nullable
        Index retrieveDom();

        Set<String> getKeys();

        boolean isCustom();

        boolean isRemove();

        boolean isReplace();

        boolean isUnique();

        CreationMode getCreationMode();

        TSMetaItem getOwner();
    }

    interface TSMetaItemAttribute {

        Module getModule();

        @Nullable
        String getName();

        @Nullable
        String getDescription();

        @Nullable
        String getDefaultValue();

        @Nullable
        Attribute retrieveDom();

        boolean isCustom();

        boolean isDeprecated();

        boolean isAutoCreate();

        boolean isRedeclare();

        boolean isGenerate();

        void addCustomProperty(String key, TSMetaCustomProperty customProperty);

        @NotNull
        List<? extends TSMetaCustomProperty> getCustomProperties(boolean includeInherited);

        @Nullable
        String getType();

        @NotNull
        TSMetaItem getOwner();
    }
}
