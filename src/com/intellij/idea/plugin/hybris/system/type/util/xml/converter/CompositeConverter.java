/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.system.type.util.xml.converter;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiDocCommentOwner;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public abstract class CompositeConverter<DOM extends DomElement> extends ResolvingConverter<DOM> {

    private final AbstractTSConverterBase<? extends DOM>[] myDelegates;
    private final Map<Class<? extends DOM>, AbstractTSConverterBase<? extends DOM>> converters;

    public CompositeConverter(final AbstractTSConverterBase<? extends DOM>... converters) {
        myDelegates = converters;
        this.converters = Arrays.stream(converters)
            .collect(Collectors.toMap(AbstractTSConverterBase::getResolvesToClass, it -> it));
    }

    @NotNull
    @Override
    public Collection<? extends DOM> getVariants(@NotNull final ConvertContext context) {
        final List<DOM> result = new LinkedList<>();
        for (AbstractTSConverterBase<? extends DOM> next : myDelegates) {
            result.addAll(next.getVariants(context));
        }
        return result;
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(@Nullable final DOM dom) {
        final AbstractTSConverterBase<DOM> converter = getConverter(dom);
        if (converter == null) return null;
        return converter.tryGetPsiElement(dom);
    }

    @Override
    public boolean canResolveTo(final Class<? extends PsiElement> elementClass) {
        return !PsiDocCommentOwner.class.isAssignableFrom(elementClass);
    }

    @Override
    public @Nullable LookupElement createLookupElement(final DOM dom) {
        final AbstractTSConverterBase<DOM> converter = getConverter(dom);
        if (converter == null) return null;
        return converter.createLookupElement(dom);
    }

    @Nullable
    @Override
    public DOM fromString(@Nullable @NonNls final String s, @NotNull final ConvertContext context) {
        for (AbstractTSConverterBase<? extends DOM> next : myDelegates) {
            final DOM nextResult = next.fromString(s, context);
            if (nextResult != null) {
                return nextResult;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String toString(@Nullable final DOM dom, @NotNull final ConvertContext context) {
        final AbstractTSConverterBase<DOM> converter = getConverter(dom);
        if (converter == null) return null;
        return converter.tryToString(dom, context);
    }

    @Nullable
    @SuppressWarnings({"unchecked", "SuspiciousMethodCalls"})
    private AbstractTSConverterBase<DOM> getConverter(@Nullable final DOM dom) {
        if (dom == null) return null;

        return (AbstractTSConverterBase<DOM>) converters.get(dom.getDomElementType());
    }

    public static class TypeOrEnumOrAtomic extends CompositeConverter<DomElement> {

        public TypeOrEnumOrAtomic() {
            super(
                new EnumTypeConverter(),
                new ItemTypeConverter(),
                new AtomicTypeConverter()
            );
        }
    }

    public static class AnyClassifier extends CompositeConverter<DomElement> {

        public AnyClassifier() {
            super(
                new AdvancedTypeMappingConverter(),
                new EnumTypeConverter(),
                new ItemTypeConverter(),
                new CollectionTypeConverter(),
                new AtomicTypeConverter(),
                new MapTypeConverter()
            );
        }

    }
}
