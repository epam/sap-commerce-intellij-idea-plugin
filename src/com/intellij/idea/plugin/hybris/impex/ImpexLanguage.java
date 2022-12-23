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

package com.intellij.idea.plugin.hybris.impex;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.lang.Language;

import java.io.Serial;

public class ImpexLanguage extends Language {

    @Serial
    private static final long serialVersionUID = -6312784074276675105L;
    private static final Language INSTANCE = new ImpexLanguage();

    public static Language getInstance() {
        return INSTANCE;
    }

    protected ImpexLanguage() {
        super(HybrisConstants.IMPEX);
    }
}
