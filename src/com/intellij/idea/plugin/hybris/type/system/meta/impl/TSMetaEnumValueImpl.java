package com.intellij.idea.plugin.hybris.type.system.meta.impl;

import com.intellij.idea.plugin.hybris.type.system.meta.TSMetaEnumValue;
import com.intellij.idea.plugin.hybris.type.system.model.EnumValue;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Eugene.Kudelevsky
 */
public class TSMetaEnumValueImpl extends TSMetaEntityImpl<EnumValue> implements TSMetaEnumValue {

    public TSMetaEnumValueImpl(final Project project, final @NotNull TSMetaEnumImpl owner, final @NotNull EnumValue dom) {
        super(project, extractEnumValueName(dom), dom);
    }

    @Nullable
    private static String extractEnumValueName(@NotNull final EnumValue dom) {
        return dom.getCode().getValue();
    }

}
