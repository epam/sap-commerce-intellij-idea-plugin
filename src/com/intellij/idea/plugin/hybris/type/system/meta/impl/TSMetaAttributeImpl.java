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

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaAttribute;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaCustomProperty;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaItem;
import com.intellij.idea.plugin.hybris.type.system.model.Attribute;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TSMetaAttributeImpl extends TSMetaEntityImpl<Attribute> implements TSMetaAttribute {

    private final TSMetaItem myMetaItem;
    private final CaseInsensitive.NoCaseMultiMap<TSMetaCustomProperty> myCustomProperties = new CaseInsensitive.NoCaseMultiMap<>();
    private final boolean myDeprecated;
    private final boolean myAutoCreate;
    private final boolean myGenerate;
    private final boolean myRedeclare;
    private final String myDescription;
    private final String myDefaultValue;
    @Nullable private final String myType;

    public TSMetaAttributeImpl(final Module module, final Project project, final @NotNull TSMetaItem owner, final @NotNull Attribute dom, final boolean custom) {
        super(module, project, extractName(dom), dom, custom);
        myMetaItem = owner;
        myDeprecated = extractDeprecated(dom);
        myRedeclare = Boolean.TRUE.equals(dom.getRedeclare().getValue());
        myAutoCreate = Boolean.TRUE.equals(dom.getAutoCreate().getValue());
        myGenerate = Boolean.TRUE.equals(dom.getGenerate().getValue());
        myType = dom.getType().getStringValue();
        myDescription = Optional.ofNullable(dom.getDescription().getXmlTag())
            .map(xmlTag -> xmlTag.getValue().getText())
            .orElse(null);
        myDefaultValue = dom.getDefaultValue().getStringValue();
        dom.getCustomProperties().getProperties().stream()
                .map(domAttribute -> new TSMetaCustomPropertyImpl(module, project, domAttribute, custom))
                .filter(attribute -> StringUtils.isNotBlank(attribute.getName()))
                .forEach(attribute -> addCustomProperty(attribute.getName().trim(), attribute));
    }

    @Override
    @Nullable
    public String getType() {
        return myType;
    }

    @Override
    public boolean isDeprecated() {
        return myDeprecated;
    }

    @Override
    public boolean isAutoCreate() {
        return myAutoCreate;
    }

    @Override
    public boolean isRedeclare() {
        return myRedeclare;
    }

    @Override
    public boolean isGenerate() {
        return myGenerate;
    }

    @Override
    public void addCustomProperty(final String key, final TSMetaCustomProperty customProperty) {
        myCustomProperties.putValue(key, customProperty);
    }

    @Override
    public @NotNull List<? extends TSMetaCustomProperty> getCustomProperties(final boolean includeInherited) {
        return new LinkedList<>(myCustomProperties.values());
    }

    @Nullable
    @Override
    public String getName() {
        return super.getName();
    }

    @Nullable
    @Override
    public String getDescription() {
        return myDescription;
    }

    @Nullable
    @Override
    public String getDefaultValue() {
        return myDefaultValue;
    }

    @NotNull
    @Override
    public TSMetaItem getMetaItem() {
        return myMetaItem;
    }

    @Nullable
    private static String extractName(final Attribute dom) {
        return dom.getQualifier().getStringValue();
    }

    private boolean extractDeprecated(@NotNull final Attribute dom) {
        final String name = getName();
        return name != null && dom.getModel().getSetters().stream().anyMatch(
            setter -> name.equals(setter.getName().getStringValue()) &&
                      Boolean.TRUE.equals(setter.getDeprecated().getValue()));
    }

}
