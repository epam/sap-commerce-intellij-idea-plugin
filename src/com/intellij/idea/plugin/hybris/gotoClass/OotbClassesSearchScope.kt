package com.intellij.idea.plugin.hybris.gotoClass

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.common.HybrisUtil
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.search.GlobalSearchScope

class OotbClassesSearchScope(project: Project) : GlobalSearchScope(project) {

    override fun isSearchInModuleContent(module: Module) = false

    override fun isSearchInLibraries() = true

    override fun contains(file: VirtualFile): Boolean {
        var virtualFile = file

        while (isNotClassesOrDirectories(virtualFile)) {
            virtualFile = virtualFile.parent
        }

        if (virtualFile.name == HybrisConstants.CLASSES_DIRECTORY) {
            virtualFile = virtualFile.parent
            return HybrisUtil.isHybrisModuleRoot(virtualFile)
        }

        return JarFileSystem.getInstance().getVirtualFileForJar(file)
            ?.parent
            ?.path
            ?.endsWith("${HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY}/${HybrisConstants.BIN_DIRECTORY}")
            ?: false
    }

    private fun isNotClassesOrDirectories(virtualFile: VirtualFile) =
        !(virtualFile.isDirectory && (isClassesOrModels(virtualFile)))

    private fun isClassesOrModels(virtualFile: VirtualFile) = virtualFile.name == HybrisConstants.CLASSES_DIRECTORY
        || virtualFile.name == HybrisConstants.JAR_MODELS
}
