package com.intellij.idea.plugin.hybris.settings;

import com.intellij.util.messages.Topic;

public interface HybrisDeveloperSpecificProjectSettingsListener {
    Topic<HybrisDeveloperSpecificProjectSettingsListener> TOPIC = Topic.create("HybrisDeveloperSpecificProjectSettingsListener", HybrisDeveloperSpecificProjectSettingsListener.class);

    default void hacConnectionSettingsChanged() {};
    default void hacActiveConnectionSettingsChanged() {};

    default void solrConnectionSettingsChanged() {};
    default void solrActiveConnectionSettingsChanged() {};

}
