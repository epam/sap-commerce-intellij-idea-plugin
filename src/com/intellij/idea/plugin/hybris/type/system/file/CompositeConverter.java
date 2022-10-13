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

package com.intellij.idea.plugin.hybris.type.system.file;

import com.intellij.psi.PsiElement;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ResolvingConverter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class CompositeConverter<DOM> extends ResolvingConverter<DOM> {

    private final TypeSystemConverterBase<? extends DOM>[] myDelegates;

    public CompositeConverter(TypeSystemConverterBase<? extends DOM>... converters) {
        myDelegates = converters;
    }

    @NotNull
    @Override
    public Collection<? extends DOM> getVariants(final ConvertContext context) {
        final List<DOM> result = new LinkedList<>();
        for (TypeSystemConverterBase<? extends DOM> next : myDelegates) {
            result.addAll(next.getVariants(context));
        }
        return result;
    }

    @Nullable
    @Override
    public DOM fromString(
        @Nullable @NonNls final String s, final ConvertContext context
    ) {
        for (TypeSystemConverterBase<? extends DOM> next : myDelegates) {
            final DOM nextResult = next.fromString(s, context);
            if (nextResult != null) {
                return nextResult;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public String toString(@Nullable final DOM t, final ConvertContext context) {
        for (TypeSystemConverterBase<? extends DOM> next : myDelegates) {
            final String nextToString = next.tryToString(t, context);
            if (nextToString != null) {
                return nextToString;
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement getPsiElement(@Nullable final DOM resolvedValue) {
        for (TypeSystemConverterBase<? extends DOM> next : myDelegates) {
            if (next.getResolvesToClass().isInstance(resolvedValue)) {
                final PsiElement nextResult = next.tryGetPsiElement(resolvedValue);
                if (nextResult != null) {
                    return nextResult;
                }
            }
        }
        return null;
    }

    public static class TypeOrEnum extends CompositeConverter<DomElement> {

        public TypeOrEnum() {
            super(new EnumTypeConverter(), new ItemTypeConverter());
        }
    }

    public static class AnyClassifier extends CompositeConverter<DomElement> {

        public AnyClassifier() {
            super(
                new EnumTypeConverter(),
                new ItemTypeConverter(),
                new CollectionTypeConverter(),
                new AtomicTypeConverter(),
                new MapTypeConverter()
            );
        }

    }
}
