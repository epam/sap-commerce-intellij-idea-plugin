/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

package com.intellij.idea.plugin.hybris.structureView;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.idea.plugin.hybris.system.type.model.Attributes;
import com.intellij.idea.plugin.hybris.system.type.utils.TSUtils;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Martin Zdarsky-Jones (martin.zdarsky@hybris.com) on 20/2/17.
 */
public class TSXmlStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {

    @Nullable
    @Override
    public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile xmlFile) {
        if (!TSUtils.isTypeSystemXmlFile(xmlFile)) {
            return null;
        }
        return new TSStructureViewBuilder(xmlFile, (dom) -> {
            if (dom instanceof Attributes) {
                return DomService.StructureViewMode.SHOW_CHILDREN;
            }
            return DomService.StructureViewMode.SHOW;
        });
    }
}
