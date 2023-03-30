package com.intellij.idea.plugin.hybris.gotoClass;

import com.intellij.idea.plugin.hybris.common.HybrisConstants;
import com.intellij.idea.plugin.hybris.common.HybrisUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class OotbClassesSearchScope extends GlobalSearchScope {

    public OotbClassesSearchScope(@NotNull final Project project) {
        super(project);
    }

    @Override
    public boolean isSearchInModuleContent(@NotNull final Module module) {
        return false;
    }

    @Override
    public boolean isSearchInLibraries() {
        return true;
    }

    @Override
    public boolean contains(@NotNull final VirtualFile file) {
        VirtualFile f = file;

        while (
            f != null &&
            !(f.isDirectory() &&
              (f.getName().equals(HybrisConstants.CLASSES_DIRECTORY) || f.getName().equals(HybrisConstants.JAR_MODELS))
            )
        ) {
            f = f.getParent();
        }
        if (f == null) {
            return false;
        }
        if (f.getName().equals(HybrisConstants.CLASSES_DIRECTORY)) {
            f = f.getParent();
            return f != null && HybrisUtil.isHybrisModuleRoot(f);
        }
        f = JarFileSystem.getInstance().getVirtualFileForJar(file);

        if (f != null) {
            f = f.getParent();
            return f.getPath().endsWith(
                HybrisConstants.PLATFORM_BOOTSTRAP_DIRECTORY + '/' + HybrisConstants.BIN_DIRECTORY);
        }
        return false;
    }
}
