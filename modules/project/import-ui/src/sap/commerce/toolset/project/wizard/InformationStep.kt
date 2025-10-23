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

package sap.commerce.toolset.project.wizard

import com.intellij.icons.AllIcons
import com.intellij.ide.util.projectWizard.WizardContext
import com.intellij.projectImport.ProjectImportWizardStep
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import sap.commerce.toolset.HybrisIcons

class InformationStep(wizardContext: WizardContext) : ProjectImportWizardStep(wizardContext) {

    override fun updateDataModel() = Unit

    override fun getComponent() = panel {
        row {
            label("Project")
                .bold()
                .align(AlignX.CENTER)
        }
        row {
            icon(HybrisIcons.Y.LOGO_GREEN)
            text(
                """
                    Imported project modules can be regrouped via <strong>hybris4intellij.properties</strong>, which will be auto-created on project import.
                    <br>
                """.trimIndent()
            )
        }

        separator()

        row {
            label("Compilation")
                .bold()
                .align(AlignX.CENTER)
        }
        row {
            icon(AllIcons.Actions.Compile)
            text(
                """
                    Make sure the project is compiled by <b>ant clean all</b> prior to import.
                    <br><br>
                    IDE incremental compilation is fully supported. This will save time as only changed classes will be compiled.
                    <br>
                    To allow this feature you need to follow those rules:
                    <ul>
                        <li>When you add a new module, you need to execute <code>ant clean all</code>. You then need to trigger <code>Build -> Rebuild</code> project.</li>
                        <li>Do not mix ant and IDE compilation. It can lead to "unknown" state. Whenever you execute <code>ant all</code> and would like to use IDE compilation then you need to trigger <code>Build -> Rebuild</code> project.</li>
                        <li>Once compiled by IDE you don't need to compile by <code>ant</code> in order to run SAP Commerce.</li>
                    </ul>
                """.trimIndent()
            )
        }

        separator()

        row {
            label("VCS")
                .bold()
                .align(AlignX.CENTER)
        }
        row {
            icon(AllIcons.General.Vcs)
            text(
                """
                    <a href="https://intellij-support.jetbrains.com/hc/en-us/articles/206544839-How-to-manage-projects-under-Version-Control-Systems">How to manage projects under Version Control Systems</a>
                    <br>
                    Make sure you commit project specific files within <i>.idea</i>. Especially <i>.idea/hybrisProjectSettings.xml</i> and <i>hybris4intellij.properties</i> must be in VCS.
                    <br>
                """.trimIndent()
            )
        }

        separator()

        row {
            label("JRebel & HotSwap")
                .bold()
                .align(AlignX.CENTER)
        }
        row {
            icon(AllIcons.Actions.SwapPanels)
            text(
                """
                    IDEA needs to build its own compile tree to enable hotSwap.
                    <br>
                    Once the project is imported and indexed you need to trigger <code>Build -> Rebuild</code> project once only.
                    <br>
                """.trimIndent()
            )
        }

        separator()

        row {
            label("Feedback")
                .bold()
                .align(AlignX.CENTER)
        }
        row {
            icon(AllIcons.General.User)
            text(
                """
                    We'd love to hear from you! you can report any suggestion, feature request or a bug to us <a href="https://github.com/epam/sap-commerce-intellij-idea-plugin">here</a>.
                """.trimIndent()
            )
        }
    }
}