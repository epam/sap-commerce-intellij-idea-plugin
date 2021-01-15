package com.intellij.idea.plugin.hybris.settings;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.services.CommonIdeaService;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_COMPONENT_NAME;
import static com.intellij.idea.plugin.hybris.common.HybrisConstants.HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_FILE_NAME;

@State(name = HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_COMPONENT_NAME, storages = {@Storage(HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_FILE_NAME)})
public class HybrisDeveloperSpecificProjectSettingsComponent
    implements PersistentStateComponent<HybrisDeveloperSpecificProjectSettings> {

    private HybrisDeveloperSpecificProjectSettings state = new HybrisDeveloperSpecificProjectSettings();

    public static HybrisDeveloperSpecificProjectSettingsComponent getInstance(@NotNull final Project project) {
        return ServiceManager.getService(project, HybrisDeveloperSpecificProjectSettingsComponent.class);
    }

    @Nullable
    @Override
    public HybrisDeveloperSpecificProjectSettings getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull final HybrisDeveloperSpecificProjectSettings state) {
        XmlSerializerUtil.copyBean(state, this.state);
    }

    public HybrisRemoteConnectionSettings getActiveHybrisRemoteConnectionSettings(final Project project) {
        final HybrisDeveloperSpecificProjectSettings state = getState();
        if (state == null) {
            return getDefaultHybrisRemoteConnectionSettings(project);
        }
        final List<HybrisRemoteConnectionSettings> connectionList = state.getRemoteConnectionSettingsList();
        final List<HybrisRemoteConnectionSettings> remoteList = connectionList
            .stream().filter(it -> it.getType() == HybrisRemoteConnectionSettings.Type.Hybris).collect(Collectors.toList());
        if (remoteList.isEmpty()) {
            return getDefaultHybrisRemoteConnectionSettings(project);
        }
        final String id = state.getActiveRemoteConnectionID();

        return remoteList.stream().filter(e -> Objects.equals(id, e.getUuid())).findFirst().orElse(remoteList.get(0));
    }

    public HybrisRemoteConnectionSettings getActiveSolrConnectionSettings(final Project project) {
        final HybrisDeveloperSpecificProjectSettings state = getState();
        if (state == null) {
            return getDefaultSolrRemoteConnectionSettings(project);
        }
        final List<HybrisRemoteConnectionSettings> connectionList = state.getRemoteConnectionSettingsList();
        final List<HybrisRemoteConnectionSettings> solrList = connectionList
            .stream().filter(it -> it.getType() == HybrisRemoteConnectionSettings.Type.SOLR).collect(Collectors.toList());
        if (solrList.isEmpty()) {
            return getDefaultSolrRemoteConnectionSettings(project);
        }
        final String id = state.getActiveSolrConnectionID();

        return solrList.stream().filter(e -> Objects.equals(id, e.getUuid())).findFirst().orElse(solrList.get(0));
    }

    @NotNull
    public HybrisRemoteConnectionSettings getDefaultHybrisRemoteConnectionSettings(final Project project) {
        final HybrisRemoteConnectionSettings item = new HybrisRemoteConnectionSettings();
        item.setType(HybrisRemoteConnectionSettings.Type.Hybris);
        item.setUuid(UUID.randomUUID().toString());
        item.setHostIP("localhost");
        item.setPort(HybrisConstants.DEFAULT_TOMCAT_SSL_PORT);
        item.setHacLogin("admin");
        item.setHacPassword("nimda");
        item.setHacSsl(true);
        item.setGeneratedURL(CommonIdeaService.getInstance().getHostHacUrl(project, item));
        return item;
    }

    @NotNull
    public HybrisRemoteConnectionSettings getDefaultSolrRemoteConnectionSettings(final Project project) {
        final HybrisRemoteConnectionSettings item = new HybrisRemoteConnectionSettings();
        item.setType(HybrisRemoteConnectionSettings.Type.SOLR);
        item.setUuid(UUID.randomUUID().toString());
        item.setHostIP("localhost");
        item.setPort(HybrisConstants.DEFAULT_SOLR_TOMCAT_SSL_PORT);
        item.setSolrWebroot("solr");
        item.setAdminLogin("solradmin");
        item.setAdminPassword("admin123");
        item.setSolrSsl(true);
        item.setGeneratedURL(CommonIdeaService.getInstance().getSolrUrl(project, item));
        return item;
    }
}
