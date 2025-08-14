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

package com.intellij.idea.plugin.hybris.settings.state

import com.intellij.idea.plugin.hybris.acl.settings.state.AclSettingsState
import com.intellij.idea.plugin.hybris.flexibleSearch.settings.state.FlexibleSearchSettingsState
import com.intellij.idea.plugin.hybris.groovy.settings.state.GroovySettingsState
import com.intellij.idea.plugin.hybris.impex.settings.state.ImpExSettingsState
import com.intellij.idea.plugin.hybris.jsp.settings.state.JspSettingsState
import com.intellij.idea.plugin.hybris.polyglotQuery.settings.state.PolyglotQuerySettingsState
import com.intellij.idea.plugin.hybris.system.bean.settings.state.BeanSystemSettingsState
import com.intellij.idea.plugin.hybris.system.businessProcess.settings.state.BpSettingsState
import com.intellij.idea.plugin.hybris.system.cockpitng.settings.state.CngSettingsState
import com.intellij.idea.plugin.hybris.system.type.settings.state.TypeSystemDiagramSettingsState
import com.intellij.idea.plugin.hybris.system.type.settings.state.TypeSystemSettingsState
import com.intellij.idea.plugin.hybris.tools.ccv2.settings.state.CCv2SettingsState
import com.intellij.idea.plugin.hybris.tools.remote.settings.state.RemoteConnectionSettingsState
import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("HybrisDeveloperSpecificProjectSettings")
data class DeveloperSettingsState(

    @JvmField @OptionTag val activeRemoteConnectionID: String? = null,
    @JvmField @OptionTag val activeSolrConnectionID: String? = null,
    @JvmField @OptionTag val activeCCv2SubscriptionID: String? = null,

    @JvmField val remoteConnectionSettingsList: List<RemoteConnectionSettingsState> = emptyList(),
    @JvmField @OptionTag val typeSystemDiagramSettings: TypeSystemDiagramSettingsState = TypeSystemDiagramSettingsState(),
    @JvmField @OptionTag val beanSystemSettings: BeanSystemSettingsState = BeanSystemSettingsState(),
    @JvmField @OptionTag val typeSystemSettings: TypeSystemSettingsState = TypeSystemSettingsState(),
    @JvmField @OptionTag val cngSettings: CngSettingsState = CngSettingsState(),
    @JvmField @OptionTag val bpSettings: BpSettingsState = BpSettingsState(),
    @JvmField @OptionTag val flexibleSearchSettings: FlexibleSearchSettingsState = FlexibleSearchSettingsState(),
    @JvmField @OptionTag val aclSettings: AclSettingsState = AclSettingsState(),
    @JvmField @OptionTag val polyglotQuerySettings: PolyglotQuerySettingsState = PolyglotQuerySettingsState(),
    @JvmField @OptionTag val impexSettings: ImpExSettingsState = ImpExSettingsState(),
    @JvmField @OptionTag val groovySettings: GroovySettingsState = GroovySettingsState(),
    @JvmField @OptionTag val jspSettings: JspSettingsState = JspSettingsState(),
    @JvmField @OptionTag val ccv2Settings: CCv2SettingsState = CCv2SettingsState(),
)
