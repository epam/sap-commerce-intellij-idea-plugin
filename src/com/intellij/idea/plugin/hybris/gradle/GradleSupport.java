package com.intellij.idea.plugin.hybris.gradle;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.gradle.settings.GradleSettings;

import java.util.Collections;

/**
 * @author Eugene.Kudelevsky
 */
public class GradleSupport {

    @Nullable
    public static GradleSupport getInstance() {
        return ApplicationManager.getApplication().getService(GradleSupport.class);
    }

    public void clearLinkedProjectSettings(@NotNull Project project) {
        GradleSettings.getInstance(project).setLinkedProjectsSettings(Collections.emptyList());
    }
}
