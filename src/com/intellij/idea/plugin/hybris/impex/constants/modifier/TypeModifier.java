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

package com.intellij.idea.plugin.hybris.impex.constants.modifier;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.impex.completion.ImpexImplementationClassCompletionContributor;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.impex.constants.ImpexConstants.ModifierCommonValues.BOOLEAN;
import static com.intellij.idea.plugin.hybris.impex.constants.ImpexConstants.ModifierCommonValues.NONE;

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 */
public enum TypeModifier implements ImpexModifier {

    BATCH_MODE("batchmode", BOOLEAN),
    CACHE_UNIQUE("cacheUnique", BOOLEAN),
    IMPEX_LEGACY_MODE("impex.legacy.mode", BOOLEAN),
    PROCESSOR("processor") {
        @Override
        public @NotNull Set<LookupElement> getLookupElements(final Project project) {
            return ImpexImplementationClassCompletionContributor.Companion.getInstance(project)
                                                                          .getImplementationsForClass(HybrisConstants.IMPEX_CLASS_PROCESSOR);
        }
    };

    private static final Map<String, ImpexModifier> ELEMENTS_MAP = new HashMap<>(
        TypeModifier.values().length
    );

    static {
        for (ImpexModifier impexModifier : TypeModifier.values()) {
            ELEMENTS_MAP.put(impexModifier.getModifierName(), impexModifier);
        }
    }

    private final String modifierName;
    private final Set<LookupElement> lookupElements;

    TypeModifier(
        @NotNull final String modifierName
    ) {
        this(modifierName, NONE);
    }

    TypeModifier(
        @NotNull final String modifierName,
        @NotNull final ImpexModifierValue[] lookupElements
    ) {
        Validate.notEmpty(modifierName);
        Validate.notNull(lookupElements);

        this.modifierName = modifierName;

        if (ArrayUtils.isEmpty(lookupElements)) {
            this.lookupElements = Collections.emptySet();
        } else {
            this.lookupElements = Arrays.stream(lookupElements)
                                        .map(ImpexModifierValue::getModifierValue)
                                        .map(LookupElementBuilder::create)
                                        .collect(Collectors.toSet());
        }
    }

    @Nullable
    public static ImpexModifier getByModifierName(@NotNull final String modifierName) {
        Validate.notEmpty(modifierName);

        return ELEMENTS_MAP.get(modifierName);
    }

    @NotNull
    @Override
    public String getModifierName() {
        return this.modifierName;
    }

    @Override
    @NotNull
    public Set<LookupElement> getLookupElements(final Project project) {
        return Collections.unmodifiableSet(lookupElements);
    }


}
