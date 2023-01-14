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
public enum AttributeModifier implements ImpexModifier {
    UNIQUE("unique", BOOLEAN),
    ALLOW_NULL("allownull", BOOLEAN),
    FORCE_WRITE("forceWrite", BOOLEAN),
    IGNORE_KEY_CASE("ignoreKeyCase", BOOLEAN),
    IGNORE_NULL("ignorenull", BOOLEAN),
    VIRTUAL("virtual", BOOLEAN),
    MODE("mode", ModeModifierValue.values()),
    ALIAS("alias"),
    COLLECTION_DELIMITER("collection-delimiter"),
    DATE_FORMAT("dateformat"),
    DEFAULT("default"),
    KEY_2_VALUE_DELIMITER("key2value-delimiter"),
    LANG("lang"),
    MAP_DELIMITER("map-delimiter"),
    NUMBER_FORMAT("numberformat"),
    PATH_DELIMITER("path-delimiter"),
    POS("pos"),
    CELL_DECORATOR("cellDecorator"),
    TRANSLATOR("translator") {
        @Override
        public @NotNull Set<LookupElement> getLookupElements(final Project project) {
            return ImpexImplementationClassCompletionContributor.Companion.getInstance(project)
                                                                          .getImplementationsForClass(HybrisConstants.IMPEX_CLASS_TRANSLATOR);
        }
    };

    private static final Map<String, ImpexModifier> ELEMENTS_MAP = new HashMap<>(
        AttributeModifier.values().length
    );

    static {
        for (ImpexModifier impexModifier : AttributeModifier.values()) {
            ELEMENTS_MAP.put(impexModifier.getModifierName(), impexModifier);
        }
    }

    private final String modifierName;
    private final Set<LookupElement> lookupElements;

    AttributeModifier(
        @NotNull final String modifierName
    ) {
        this(modifierName, NONE);
    }

    AttributeModifier(
        @NotNull final String modifierName,
        @NotNull final ImpexModifierValue[] modifierValues
    ) {
        Validate.notEmpty(modifierName);
        Validate.notNull(modifierValues);

        this.modifierName = modifierName;

        if (ArrayUtils.isEmpty(modifierValues)) {
            this.lookupElements = Collections.emptySet();
        } else {
            this.lookupElements = Arrays.stream(modifierValues)
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
    public @NotNull Set<LookupElement> getLookupElements(final Project project) {
        return Collections.unmodifiableSet(lookupElements);
    }

}
