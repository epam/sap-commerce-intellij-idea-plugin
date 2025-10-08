/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2025 EPAM Systems <hybrisideaplugin@epam.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package sap.commerce.toolset.debugger.ui.tree

import com.intellij.debugger.engine.DebuggerUtils
import com.intellij.debugger.ui.impl.watch.UserExpressionDescriptorImpl
import com.intellij.debugger.ui.impl.watch.ValueDescriptorImpl
import com.intellij.debugger.ui.tree.render.OnDemandRenderer
import com.intellij.openapi.project.Project
import sap.commerce.toolset.HybrisIcons

internal class LocalizedValueDescriptor(
    parentObject: ValueDescriptorImpl,
    presentationName: String,
    project: Project,
    methodName: String,
) : UserExpressionDescriptorImpl(
    project, parentObject, "java.util.HashMap", presentationName,
    DebuggerUtils.getInstance().createExpressionWithImports(methodName.toExpression),
    0
) {

    init {
        putUserData(OnDemandRenderer.ON_DEMAND_CALCULATED, false)
    }

    override fun getValueIcon() = HybrisIcons.TypeSystem.LOCALIZED
}

private val String.toExpression
    get() = """
            de.hybris.platform.servicelayer.i18n.CommonI18NService commonI18NService = de.hybris.platform.core.Registry.getApplicationContext().getBean(de.hybris.platform.servicelayer.i18n.CommonI18NService.class);
        
            final HashMap<Locale, Object> values = new HashMap<>();
        
            for (final de.hybris.platform.core.model.c2l.LanguageModel languageModel : commonI18NService.getAllLanguages()) {
                Locale locale = commonI18NService.getLocaleForLanguage(languageModel);
                values.put(locale, $this(locale));
            }
        
            values
        """.trimIndent()