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

import com.intellij.idea.plugin.hybris.common.utils.HybrisIcons;
import com.intellij.idea.plugin.hybris.type.system.model.Items;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Iconable;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.idea.plugin.hybris.type.system.utils.TypeSystemUtils.isTypeSystemXmlFile;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 15/06/2016.
 */
public class TypeSystemDomFileDescription extends DomFileDescription<Items> {

    public TypeSystemDomFileDescription() {
        super(Items.class, "items");
    }

    @Override
    public boolean isMyFile(
        @NotNull final XmlFile file, @Nullable final Module module
    ) {
        final boolean isMyFile = super.isMyFile(file, module);
        final boolean isTypeSystem = isTypeSystemXmlFile(file);
        return isMyFile && isTypeSystem;
    }

    @Override
    @Nullable
    public Icon getFileIcon(@Iconable.IconFlags final int flags) {
        return HybrisIcons.TYPE_SYSTEM;
    }
}
