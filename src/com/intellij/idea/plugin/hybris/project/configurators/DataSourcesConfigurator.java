package com.intellij.idea.plugin.hybris.project.configurators;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface DataSourcesConfigurator {

    void configure(@NotNull Project project);
}
