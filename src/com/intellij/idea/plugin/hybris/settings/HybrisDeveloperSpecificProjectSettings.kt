package com.intellij.idea.plugin.hybris.settings

data class HybrisDeveloperSpecificProjectSettings(
    var activeRemoteConnectionID: String? = null,
    var activeSolrConnectionID: String? = null,
    var remoteConnectionSettingsList: MutableList<HybrisRemoteConnectionSettings> = mutableListOf(),
    var typeSystemDiagramSettings: TSDiagramSettings = TSDiagramSettings(),
)