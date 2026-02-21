/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package sap.commerce.toolset.ccv2.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.*
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.util.text.StringUtil
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.util.asSafely
import com.intellij.util.ui.JBUI
import sap.commerce.toolset.HybrisIcons
import sap.commerce.toolset.ccv2.CCv2Service
import sap.commerce.toolset.ccv2.dto.CCv2BuildDto
import sap.commerce.toolset.ccv2.dto.CCv2BuildStatus
import sap.commerce.toolset.ccv2.settings.state.CCv2Subscription
import sap.commerce.toolset.ccv2.ui.components.CCv2SubscriptionsComboBoxModelFactory
import sap.commerce.toolset.ui.banner
import sap.commerce.toolset.ui.scrollPanel
import java.awt.Dimension
import java.awt.event.ActionEvent
import java.io.Serial
import java.util.*
import javax.swing.Icon
import javax.swing.JLabel

class CCv2CleanupBuildsDialog(
    private val project: Project,
    private val subscription: CCv2Subscription?,
) : DialogWrapper(project) {

    private lateinit var subscriptionComboBox: ComboBox<CCv2Subscription>
    private lateinit var topField: JBTextField
    private lateinit var totalLabel: JLabel
    private lateinit var placeholder: Placeholder
    private var loadDisposable = Disposer.newDisposable(disposable)
    private val resultsHeaderToggle = AtomicBooleanProperty(false)
    private val cleanableBuilds = mutableMapOf<CCv2BuildDto, AtomicBooleanProperty>()

    private val fetchBuildsButton = object : DialogWrapperAction("Fetch Builds") {
        @Serial
        private val serialVersionUID: Long = -1963011685030505631L

        override fun doAction(e: ActionEvent) {
            val subscription = subscriptionComboBox.selectedItem.asSafely<CCv2Subscription>()
            refreshAutoDeploymentPanel(subscription)
        }
    }

    init {
        title = "Cleanup CCv2 Builds"
        super.init()

        isResizable = false
        setOKButtonText("Delete Builds")

        refreshAutoDeploymentPanel(subscription)
    }

    override fun createNorthPanel() = banner(
        text = """
            Selected builds will be deleted permanently in 14 day(s).<br>
            During this period you can request a restore via ticket to your system administrator.
        """.trimIndent(),
        status = EditorNotificationPanel.Status.Warning
    )

    override fun createCenterPanel() = panel {
        row {
            subscriptionComboBox = comboBox(
                CCv2SubscriptionsComboBoxModelFactory.create(project, subscription),
                renderer = SimpleListCellRenderer.create { label, value, _ ->
                    if (value != null) {
                        label.icon = HybrisIcons.Module.CCV2
                        label.text = value.presentableName
                    }
                }
            )
                .label("Subscription:")
                .align(AlignX.FILL)
                .gap(RightGap.SMALL)
                .addValidationRule("Please select a subscription to fetch cleanable builds.") { it.selectedItem == null }
                .onChanged { refreshAutoDeploymentPanel(it.selectedItem as CCv2Subscription) }
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            topField = intTextField(1..100)
                .label("Get last:")
                .commentRight("Builds")
                .align(AlignX.FILL)
                .text("20")
                .component
        }.layout(RowLayout.PARENT_GRID)

        separator()

        row {
            totalLabel = label("").component
            button(IdeBundle.message("command.select.all")) { cleanableBuilds.values.forEach { it.set(true) } }
            button(IdeBundle.message("command.unselect.all")) { cleanableBuilds.values.forEach { it.set(false) } }
        }.visibleIf(resultsHeaderToggle)

        row {
            placeholder = placeholder().align(AlignX.FILL)
        }.layout(RowLayout.PARENT_GRID)

    }.also {
        it.border = JBUI.Borders.empty(16)
    }

    override fun doOKAction() {
        if (!isOKActionEnabled) return

        val subscription = subscriptionComboBox.selectedItem as CCv2Subscription
        val builds = cleanableBuilds.entries
            .filter { it.value.get() }
            .map { it.key }

        val ask = MessageDialogBuilder.yesNo(
            title = "Confirm Cleanup CCv2 Builds",
            message = "Please, confirm deletion of the ${builds.size} builds: ${builds.joinToString(", ") { it.code }}",
            icon = HybrisIcons.CCv2.Build.Actions.CLEANUP,
        ).ask(project)

        if (ask) {
            CCv2Service.getInstance(project).deleteBuilds(project, subscription, builds)
            close(OK_EXIT_CODE, ExitActionType.OK)
        }
    }

    override fun getStyle() = DialogStyle.COMPACT
    override fun getInitialSize() = JBUI.DialogSizes.large()
    override fun createLeftSideActions() = arrayOf(fetchBuildsButton)
    override fun getPreferredFocusedComponent() = subscriptionComboBox

    private fun refreshAutoDeploymentPanel(subscription: CCv2Subscription?) {
        isOKActionEnabled = false
        loadDisposable.dispose()
        cleanableBuilds.clear()
        resultsHeaderToggle.set(false)

        if (subscription == null) {
            placeholder.component = infoPanel(
                "Please, select a Subscription first...",
                HybrisIcons.Module.CCV2
            )

            return
        }

        placeholder.component = infoPanel(
            "Re-fetching builds for the subscription...",
            AnimatedIcon.Default.INSTANCE
        )

        val top = topField.text.toIntOrNull() ?: 20

        CCv2Service.getInstance(project).fetchBuilds(
            subscriptions = listOf(subscription),
            top = top,
            withoutStatuses = CCv2BuildStatus.entries
                .filterNot { it == CCv2BuildStatus.FAIL || it == CCv2BuildStatus.SUCCESS },
            onCompleteCallback = { builds ->
                val environmentsPanel = getEnvironmentsPanel(builds, subscription)

                resultsHeaderToggle.set(cleanableBuilds.isNotEmpty())
                totalLabel.text = "Found ${cleanableBuilds.size} clearable builds"
                placeholder.component = environmentsPanel
            },
            sendEvents = false
        )
    }

    private fun getEnvironmentsPanel(
        buildsPerSubscription: SortedMap<CCv2Subscription, Collection<CCv2BuildDto>>,
        subscription: CCv2Subscription
    ): DialogPanel = buildsPerSubscription[subscription]
        ?.filter { it.canDelete() }
        ?.takeIf { it.isNotEmpty() }
        ?.let { builds ->
            panel {
                builds.forEach { build ->
                    val flag = AtomicBooleanProperty(false).apply {
                        afterChange(loadDisposable) {
                            isOKActionEnabled = cleanableBuilds.values.any { it.get() }
                        }
                    }
                    cleanableBuilds[build] = flag

                    row {
                        checkBox(StringUtil.first(build.name, 20, true))
                            .comment(build.code)
                            .gap(RightGap.COLUMNS)
                            .bindSelected(flag)

                        status(build).gap(RightGap.COLUMNS)
                        sUser(project, build.createdBy, HybrisIcons.CCv2.Build.CREATED_BY).gap(RightGap.COLUMNS)
                        date("Start time", build.startTime)
                    }.layout(RowLayout.PARENT_GRID)
                }
            }
                .apply { border = JBUI.Borders.emptyRight(16) }
                .let { scrollPanel(it) }
                .apply { preferredSize = Dimension(preferredSize.width, 230) }
        }
        ?: infoPanel(
            "No builds eligible for cleanup...",
            AllIcons.General.Warning,
        )

    private fun infoPanel(message: String, icon: Icon): DialogPanel = panel {
        row {
            label(message)
                .component
                .also { it.icon = icon }
        }
    }
}