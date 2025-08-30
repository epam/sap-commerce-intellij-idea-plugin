/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.solr.exec.settings.event

import com.intellij.util.messages.Topic
import sap.commerce.toolset.exec.settings.event.ExecConnectionListener
import sap.commerce.toolset.solr.exec.settings.state.SolrConnectionSettingsState

interface SolrConnectionSettingsListener : ExecConnectionListener<SolrConnectionSettingsState> {

    override fun onActive(connection: SolrConnectionSettingsState) = Unit
    override fun onCreate(connection: SolrConnectionSettingsState) = Unit
    override fun onUpdate(settings: Collection<SolrConnectionSettingsState>) = Unit
    override fun onDelete(connection: SolrConnectionSettingsState) = Unit

    companion object {
        val TOPIC = Topic(SolrConnectionSettingsListener::class.java)
    }
}