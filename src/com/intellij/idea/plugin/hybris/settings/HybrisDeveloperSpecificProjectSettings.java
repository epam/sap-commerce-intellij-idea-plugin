package com.intellij.idea.plugin.hybris.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HybrisDeveloperSpecificProjectSettings {

    protected String activeRemoteConnectionID;
    protected String activeSolrConnectionID;
    protected List<HybrisRemoteConnectionSettings> remoteConnectionSettingsList = new ArrayList<>();

    public List<HybrisRemoteConnectionSettings> getRemoteConnectionSettingsList() {
        return remoteConnectionSettingsList;
    }

    public void setRemoteConnectionSettingsList(final List<HybrisRemoteConnectionSettings> remoteConnectionSettingsList) {
        this.remoteConnectionSettingsList = remoteConnectionSettingsList;
    }

    public String getActiveRemoteConnectionID() {
        return activeRemoteConnectionID;
    }

    public void setActiveRemoteConnectionID(final String activeRemoteConnectionID) {
        this.activeRemoteConnectionID = activeRemoteConnectionID;
    }

    public String getActiveSolrConnectionID() {
        return activeSolrConnectionID;
    }

    public void setActiveSolrConnectionID(final String activeSolrConnectionID) {
        this.activeSolrConnectionID = activeSolrConnectionID;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final HybrisDeveloperSpecificProjectSettings that = (HybrisDeveloperSpecificProjectSettings) obj;
        return getActiveRemoteConnectionID().equals(that.getActiveRemoteConnectionID()) &&
               getActiveSolrConnectionID().equals(that.getActiveSolrConnectionID()) &&
               Objects.equals(getRemoteConnectionSettingsList(), that.getRemoteConnectionSettingsList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            getActiveRemoteConnectionID(),
            getRemoteConnectionSettingsList(),
            getActiveSolrConnectionID()
        );
    }
}
