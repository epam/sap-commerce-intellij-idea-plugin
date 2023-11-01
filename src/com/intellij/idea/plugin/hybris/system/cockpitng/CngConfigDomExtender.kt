/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

import com.intellij.idea.plugin.hybris.common.HybrisConstants.COCKPIT_NG_NAMESPACE_KEY
import com.intellij.idea.plugin.hybris.system.cockpitng.model.advancedSearch.AdvancedSearch
import com.intellij.idea.plugin.hybris.system.cockpitng.model.collectionBrowser.CollectionBrowser
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.Context
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Actions
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Base
import com.intellij.idea.plugin.hybris.system.cockpitng.model.config.hybris.Editors
import com.intellij.idea.plugin.hybris.system.cockpitng.model.framework.CockpitLocales
import com.intellij.idea.plugin.hybris.system.cockpitng.model.framework.Dashboard
import com.intellij.idea.plugin.hybris.system.cockpitng.model.gridView.GridView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.itemEditor.EditorArea
import com.intellij.idea.plugin.hybris.system.cockpitng.model.listView.ListView
import com.intellij.idea.plugin.hybris.system.cockpitng.model.simpleSearch.SimpleSearch
import com.intellij.idea.plugin.hybris.system.cockpitng.model.widgets.*
import com.intellij.idea.plugin.hybris.system.cockpitng.model.wizardConfig.Flow
import com.intellij.util.xml.XmlName
import com.intellij.util.xml.reflect.DomExtender
import com.intellij.util.xml.reflect.DomExtensionsRegistrar

class CngConfigDomExtender : DomExtender<Context>() {

    override fun registerExtensions(t: Context, registrar: DomExtensionsRegistrar) {
        registrar.registerCollectionChildrenExtension(XmlName("advanced-search", COCKPIT_NG_NAMESPACE_KEY), AdvancedSearch::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("cockpit-locales", COCKPIT_NG_NAMESPACE_KEY), CockpitLocales::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("collection-browser", COCKPIT_NG_NAMESPACE_KEY), CollectionBrowser::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("dashboard", COCKPIT_NG_NAMESPACE_KEY), Dashboard::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("explorer-tree", COCKPIT_NG_NAMESPACE_KEY), ExplorerTree::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("extended-split-layout", COCKPIT_NG_NAMESPACE_KEY), ExtendedSplitLayout::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("fulltext-search", COCKPIT_NG_NAMESPACE_KEY), FulltextSearch::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("links", COCKPIT_NG_NAMESPACE_KEY), Links::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("notification-area", COCKPIT_NG_NAMESPACE_KEY), NotificationArea::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("perspective-chooser", COCKPIT_NG_NAMESPACE_KEY), PerspectiveChooser::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("facet-config", COCKPIT_NG_NAMESPACE_KEY), FacetConfig::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("simple-list", COCKPIT_NG_NAMESPACE_KEY), SimpleList::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("simple-search", COCKPIT_NG_NAMESPACE_KEY), SimpleSearch::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("view-switcher", COCKPIT_NG_NAMESPACE_KEY), ViewSwitcher::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("flow", COCKPIT_NG_NAMESPACE_KEY), Flow::class.java)

        registrar.registerCollectionChildrenExtension(XmlName("actions", COCKPIT_NG_NAMESPACE_KEY), Actions::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("editors", COCKPIT_NG_NAMESPACE_KEY), Editors::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("base", COCKPIT_NG_NAMESPACE_KEY), Base::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("compare-view", COCKPIT_NG_NAMESPACE_KEY), CompareView::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("editorArea", COCKPIT_NG_NAMESPACE_KEY), EditorArea::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("list-view", COCKPIT_NG_NAMESPACE_KEY), ListView::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("grid-view", COCKPIT_NG_NAMESPACE_KEY), GridView::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("summary-view", COCKPIT_NG_NAMESPACE_KEY), SummaryView::class.java)
        registrar.registerCollectionChildrenExtension(XmlName("quick-list", COCKPIT_NG_NAMESPACE_KEY), QuickList::class.java)
    }
}