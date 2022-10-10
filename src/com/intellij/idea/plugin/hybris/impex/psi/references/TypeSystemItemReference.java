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

package com.intellij.idea.plugin.hybris.impex.psi.references;

import com.intellij.idea.plugin.hybris.impex.psi.ImpexHeaderTypeName;
import com.intellij.idea.plugin.hybris.impex.psi.references.result.EnumResolveResult;
import com.intellij.idea.plugin.hybris.psi.references.TypeSystemReferenceBase;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaClass;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnum;
import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaModelService;
import com.intellij.idea.plugin.hybris.type.system.model.EnumType;
import com.intellij.idea.plugin.hybris.type.system.model.ItemType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
class TypeSystemItemReference extends TypeSystemReferenceBase<ImpexHeaderTypeName> {

    public TypeSystemItemReference(@NotNull final ImpexHeaderTypeName owner) {
        super(owner);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(final boolean incompleteCode) {
        final TSMetaModelService meta = getMetaService();
        final String lookingForName = getElement().getText();
        final Optional<TSMetaClass> metaClass = searchInMetaClasses(meta, lookingForName);
        if (metaClass.isPresent()) {
            return metaClass
                .map(TSMetaClass::retrieveAllDomsStream)
                .orElse(Stream.empty())
                .map(ItemTypeResolveResult::new)
                .toArray(ResolveResult[]::new);
        } else {
            final TSMetaEnum metaEnum = meta.findMetaEnumByName(lookingForName);
            if (metaEnum != null) {
                final EnumType enumType = metaEnum.retrieveDom();
                final EnumResolveResult resolveResult = new EnumResolveResult(enumType);
                return new ResolveResult[]{resolveResult};
            }
        }
        return ResolveResult.EMPTY_ARRAY;
    }

    private Optional<TSMetaClass> searchInMetaClasses(final TSMetaModelService meta, final String lookingForName) {
        return Optional.ofNullable(meta.findMetaClassByName(lookingForName));
    }

    private static class ItemTypeResolveResult implements TypeSystemResolveResult {

        private final ItemType myDomItemType;

        public ItemTypeResolveResult(@NotNull final ItemType domItemType) {
            myDomItemType = domItemType;
        }

        @Nullable
        @Override
        public PsiElement getElement() {
            final GenericAttributeValue<String> codeAttr = myDomItemType.getCode();
            return codeAttr == null ? null : codeAttr.getXmlAttributeValue();
        }

        @NotNull
        @Override
        public DomElement getSemanticDomElement() {
            return myDomItemType;
        }

        @Override
        public boolean isValidResult() {
            return getElement() != null;
        }
    }

}
