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

package com.intellij.idea.plugin.hybris.impex.psi;

import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage;
import com.intellij.idea.plugin.hybris.impex.file.ImpexFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ImpexFile extends PsiFileBase {

    public ImpexFile(@NotNull final FileViewProvider viewProvider) {
        super(viewProvider, ImpexLanguage.getInstance());
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return ImpexFileType.getInstance();
    }

    @NotNull
    @Override
    public String toString() {
        return "Impex File";
    }

    @Override
    public Icon getIcon(final int flags) {
        return super.getIcon(flags);
    }

//    @Override
//    public PsiReference findReferenceAt(final int offset) {
//        final PsiElement psiElement = findElementAt(offset);
//        if (isMacroNameDeclaration(psiElement) || isMacroUsage(psiElement)) {
//            return new ImpexMacrosReferenceBase(psiElement);
//        } else {
//            return super.findReferenceAt(offset);
//        }
//    }
}