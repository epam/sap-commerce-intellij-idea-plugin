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

import com.intellij.icons.AllIcons;
import com.intellij.ide.highlighter.DomSupportEnabled;
import com.intellij.ide.highlighter.XmlLikeFileType;
import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.lang.xml.XMLLanguage;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.xml.XmlBundle;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class HybrisItemsXmlFileType extends XmlLikeFileType implements DomSupportEnabled {

    public static final FileType INSTANCE = new HybrisItemsXmlFileType();

    private HybrisItemsXmlFileType() {
        super(XMLLanguage.INSTANCE);
    }

    @Override
    @NotNull
    public String getName() {
        return "XML";
    }

    @Override
    @NotNull
    public String getDescription() {
        return XmlBundle.message("title.xml");
    }

    @Override
    @NotNull
    public String getDefaultExtension() {
        return HybrisConstants.HYBRIS_ITEMS_XML_FILE_ENDING;
    }

    @Override
    public Icon getIcon() {
        return AllIcons.FileTypes.Xml;
    }
}
