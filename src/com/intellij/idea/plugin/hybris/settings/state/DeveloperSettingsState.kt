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

import com.intellij.util.xmlb.annotations.OptionTag
import com.intellij.util.xmlb.annotations.Tag

@Tag("HybrisDeveloperSpecificProjectSettings")
data class DeveloperSettingsState(

    @JvmField @OptionTag val activeRemoteConnectionID: String? = null,
    @JvmField @OptionTag val activeSolrConnectionID: String? = null,
    @JvmField @OptionTag val activeCCv2SubscriptionID: String? = null,

    @JvmField val remoteConnectionSettingsList: List<RemoteConnectionSettings> = emptyList(),
    @JvmField @OptionTag val typeSystemDiagramSettings: TypeSystemDiagramSettings = TypeSystemDiagramSettings(),
    @JvmField @OptionTag val beanSystemSettings: BeanSystemSettings = BeanSystemSettings(),
    @JvmField @OptionTag val typeSystemSettings: TypeSystemSettings = TypeSystemSettings(),
    @JvmField @OptionTag val cngSettings: CngSettings = CngSettings(),
    @JvmField @OptionTag val bpSettings: BpSettings = BpSettings(),
    @JvmField @OptionTag val flexibleSearchSettings: FlexibleSearchSettings = FlexibleSearchSettings(),
    @JvmField @OptionTag val aclSettings: AclSettings = AclSettings(),
    @JvmField @OptionTag val polyglotQuerySettings: PolyglotQuerySettings = PolyglotQuerySettings(),
    @JvmField @OptionTag val impexSettings: ImpexSettings = ImpexSettings(),
    @JvmField @OptionTag val groovySettings: GroovySettings = GroovySettings(),
    @JvmField @OptionTag val ccv2Settings: CCv2Settings = CCv2Settings(),
)
