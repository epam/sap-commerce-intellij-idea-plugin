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

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * https://help.sap.com/docs/SAP_COMMERCE/d0224eca81e249cb821f2cdf45a82ace/1c8f5bebdc6e434782ff0cfdb0ca1847.html?locale=en-US
 */
public enum ModeModifierValue implements ImpexModifierValue {

    APPEND("append"),
    REMOVE("remove");

    private final String modifierValue;

    private static final Map<String, ModeModifierValue> ELEMENTS_MAP = new HashMap<String, ModeModifierValue>(
        ModeModifierValue.values().length
    );

    static {
        for (ModeModifierValue impexModifierValue : ModeModifierValue.values()) {
            ELEMENTS_MAP.put(impexModifierValue.getModifierValue(), impexModifierValue);
        }
    }

    @Nullable
    public static ModeModifierValue getByModifierValue(@NotNull final String modifierValue) {
        Validate.notEmpty(modifierValue);

        return ELEMENTS_MAP.get(modifierValue);
    }

    ModeModifierValue(@NotNull final String modifierValue) {
        Validate.notEmpty(modifierValue);

        this.modifierValue = modifierValue;
    }

    @NotNull
    @Override
    public String getModifierValue() {
        return modifierValue;
    }
}
