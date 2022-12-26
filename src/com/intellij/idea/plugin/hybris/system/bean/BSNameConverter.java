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

package com.intellij.idea.plugin.hybris.system.bean;

import com.intellij.idea.plugin.hybris.system.bean.model.Property;
import com.intellij.psi.PsiField;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class BSNameConverter extends ResolvingConverter<PsiField> {

    @Nullable
    @Override
    public PsiField fromString(@Nullable final String text, final ConvertContext context) {
        DomElement host = context.getInvocationElement();
        if (host instanceof GenericAttributeValue<?>) {
            DomElement domProperty = host.getParent();
            return domProperty instanceof Property ? BSUtils.resolveToPsiField((Property) domProperty, text) : null;
        }
        return null;
    }

    @NotNull
    @Override
    public Collection<? extends PsiField> getVariants(final ConvertContext context) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String toString(@Nullable final PsiField psiField, final ConvertContext context) {
        return psiField == null ? "" : psiField.getName();
    }
}
