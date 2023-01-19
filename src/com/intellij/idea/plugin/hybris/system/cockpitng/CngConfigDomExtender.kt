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
package com.intellij.idea.plugin.hybris.system.cockpitng

import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Actions
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Base
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.AdvancedSearch
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.EditorArea
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.simpleSearch.SimpleSearch
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.ExplorerTree
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.NotificationArea
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.Flow
import com.intellij.util.xml.reflect.DomExtender
import com.intellij.util.xml.reflect.DomExtensionsRegistrar

class CngConfigDomExtender : DomExtender<Context>() {

    override fun registerExtensions(t: Context, registrar: DomExtensionsRegistrar) {
        registrar.registerCustomChildrenExtension(Actions::class.java)
        registrar.registerCustomChildrenExtension(EditorArea::class.java)
        registrar.registerCustomChildrenExtension(Flow::class.java)
        registrar.registerCustomChildrenExtension(ListView::class.java)
        registrar.registerCustomChildrenExtension(ExplorerTree::class.java)
        registrar.registerCustomChildrenExtension(AdvancedSearch::class.java)
        registrar.registerCustomChildrenExtension(SimpleSearch::class.java)
        registrar.registerCustomChildrenExtension(Base::class.java)
        registrar.registerCustomChildrenExtension(NotificationArea::class.java)
    }
}