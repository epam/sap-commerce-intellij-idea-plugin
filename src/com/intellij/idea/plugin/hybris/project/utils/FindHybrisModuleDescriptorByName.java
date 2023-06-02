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

package com.intellij.idea.plugin.hybris.project.utils;

import com.google.common.base.Predicate;
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created 8:31 PM 20 June 2015.
 *
 * @author Alexander Bartash <AlexanderBartash@gmail.com>
 */
public class FindHybrisModuleDescriptorByName implements Predicate<ModuleDescriptor> {

    private final String name;

    public FindHybrisModuleDescriptorByName(@NotNull final String name) {
        Validate.notEmpty(name);

        this.name = name;
    }

    @Override
    public boolean apply(@Nullable final ModuleDescriptor t) {
        return null != t && name.equalsIgnoreCase(t.getName());
    }
}
