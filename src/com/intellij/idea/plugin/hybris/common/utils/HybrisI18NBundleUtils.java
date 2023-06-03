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

package com.intellij.idea.plugin.hybris.common.utils;

import com.intellij.AbstractBundle;
import com.intellij.BundleBase;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

public final class HybrisI18NBundleUtils extends AbstractBundle {

    public static final String PATH_TO_BUNDLE = "i18n.HybrisBundle";

    private static final HybrisI18NBundleUtils BUNDLE = new HybrisI18NBundleUtils();

    private HybrisI18NBundleUtils() {
        super(PATH_TO_BUNDLE);
    }

    @NotNull
    public static String message(
        @NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) final String key,
        @NotNull final Object... params
    ) {
        if (StringUtils.isBlank(key)) {
            return "";
        }

        final String message = BUNDLE.getMessage(key, params);

        return StringUtils.isBlank(message) ? key : message;
    }

    @Nullable
    public static String messageFallback(
        @NotNull @PropertyKey(resourceBundle = PATH_TO_BUNDLE) final String key,
        @NotNull final String fallback,
        @NotNull final Object... params
    ) {
        return BundleBase.messageOrDefault(BUNDLE.getResourceBundle(), key, fallback, params);
    }
}
