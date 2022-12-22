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

package com.intellij.idea.plugin.hybris.impex.file;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ImpexFileType extends LanguageFileType {

    private static final ImpexFileType INSTANCE = new ImpexFileType();

    public static ImpexFileType getInstance() {
        return INSTANCE;
    }

    protected ImpexFileType() {
        super(ImpexLanguage.getInstance());
    }

    public ImpexFileType(@NotNull Language language) {
        super(language);
    }

    public ImpexFileType(@NotNull Language language, boolean secondary) {
        super(language, secondary);
    }

    @NotNull
    @Override
    public String getName() {
        return HybrisConstants.IMPEX;
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Impex language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return HybrisConstants.IMPEX_FILE_EXTENSION;
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return HybrisIcons.IMPEX_FILE;
    }
}
