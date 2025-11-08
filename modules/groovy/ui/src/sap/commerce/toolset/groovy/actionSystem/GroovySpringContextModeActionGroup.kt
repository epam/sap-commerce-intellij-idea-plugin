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

package sap.commerce.toolset.groovy.actionSystem

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.ui.GotItTooltip
import com.intellij.ui.scale.JBUIScale
import sap.commerce.toolset.GotItTooltips
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.groovy.GroovyConstants
import sap.commerce.toolset.i18n
import sap.commerce.toolset.settings.DeveloperSettings
import sap.commerce.toolset.triggerAction
import sap.commerce.toolset.ui.ActionButtonWithTextAndDescriptionComponentProvider

class GroovySpringContextModeActionGroup : DefaultActionGroup() {

    private val componentProvider = ActionButtonWithTextAndDescriptionComponentProvider(
        actionGroup = this,
        gotItTooltipProvider = { component ->
            val before = """<pre class="code">
                import de.hybris.platform.core.Registry
                import de.hybris.platform.product.ProductService

                def productService = Registry.applicationContext
                    .getBean('productService', ProductService.class)

                productService.getProduct('test')</pre>
            """.trimIndent()
            val after = "<pre class=\"code\">productService.getProduct('test')</pre>"

            GotItTooltip(
                id = GotItTooltips.SPRING_CONTEXT_MODE,
                textSupplier = {
                    """
                    You can enable Spring Context within your groovy scripts by switching to ${icon(HybrisIcons.Spring.LOCAL)} local resolution mode.
                    <br>Resolution of the Spring Context is a heavy operation, that's why it is ${icon(HybrisIcons.Spring.DISABLED)} disabled by default for every new Editor, but it can be changed via ${
                        link("Groovy settings") {
                            DataManager.getInstance().getDataContext(component).getData(CommonDataKeys.PROJECT)
                                ?.triggerAction("hybris.groovy.openSettings")
                        }
                    }.
                    <br><br>Before:
                    <br>${before}

                    <br>After:
                    <br>${after}
                """.trimIndent()
                },
                parentDisposable = null
            )
                .withMaxWidth(JBUIScale.scale(420))
                .withHeader("Welcome Spring Context in Groovy!")
        }
    )

    init {
        templatePresentation.putClientProperty(ActionUtil.SHOW_TEXT_IN_TOOLBAR, true)
        templatePresentation.putClientProperty(ActionUtil.COMPONENT_PROVIDER, componentProvider)
    }

    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        val vf = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return
        val project = e.project ?: return
        val currentMode = vf.getUserData(GroovyConstants.KEY_SPRING_CONTEXT_MODE)
            ?: DeveloperSettings.getInstance(project).groovySettings.springContextMode

        e.presentation.icon = currentMode.icon
        e.presentation.text = i18n("hybris.groovy.actions.springContext.mode", currentMode.presentationText)
        e.presentation.description = "Spring context resolution mode"
    }

}