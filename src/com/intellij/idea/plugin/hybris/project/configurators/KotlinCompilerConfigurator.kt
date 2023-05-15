package com.intellij.idea.plugin.hybris.project.configurators

import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.openapi.project.Project

interface KotlinCompilerConfigurator {

    fun configure(descriptor: HybrisProjectDescriptor, project: Project, cache: HybrisConfiguratorCache)
}
