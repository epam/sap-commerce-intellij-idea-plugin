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

package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.lowerCase;

public class FlexibleSearchTokenType extends IElementType {

    private static final Pattern PATTERN = Pattern.compile("[_]");

    public FlexibleSearchTokenType(@NotNull @NonNls final String debugName) {
        super(debugName, FlexibleSearchLanguage.getInstance());
    }

    @Override
    public String toString() {
        final String name = super.toString();

        if (isBlank(name)) {
            return name;
        }

        final String fixedName = PATTERN.matcher(lowerCase(name)).replaceAll(" ");

        return new StringBuilder("<").append(fixedName).append('>').toString();
    }
}