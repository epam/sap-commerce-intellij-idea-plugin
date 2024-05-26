/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2024 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.ui

import com.intellij.idea.plugin.hybris.settings.CCv2SubscriptionDto
import com.intellij.idea.plugin.hybris.settings.components.ApplicationSettingsComponent
import com.intellij.openapi.observable.properties.AtomicBooleanProperty
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import java.awt.Component

class CCv2SubscriptionDialog(
    parentComponent: Component,
    private val subscription: CCv2SubscriptionDto,
    dialogTitle: String
) : DialogWrapper(null, parentComponent, false, IdeModalityType.IDE) {

    private val enableCCv2Token by lazy { AtomicBooleanProperty(subscription.id == null) }

    init {
        title = dialogTitle
        super.init()

        if (subscription.ccv2Token == null) {
            ApplicationSettingsComponent.getInstance().loadCCv2Token(subscription.uuid) {
                subscription.ccv2Token = it
                enableCCv2Token.set(true)
            }
        } else {
            enableCCv2Token.set(true)
        }
    }

    private lateinit var idTextField: JBTextField

    override fun createCenterPanel() = panel {
        row {
            idTextField = textField()
                .label("Subscription code:")
                .align(AlignX.FILL)
                .addValidationRule("ID cannot be blank.") { it.text.isBlank() }
                .bindText(subscription::id.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)

        row {
            textField()
                .label("Name:")
                .align(AlignX.FILL)
                .bindText(subscription::name.toNonNullableProperty(""))
        }.layout(RowLayout.PARENT_GRID)

        row {
            passwordField()
                .label("CCv2 token:")
                .enabledIf(enableCCv2Token)
                .comment("Overrides default CCv2 token per subscription.")
                .align(AlignX.FILL)
                .bindText(subscription::ccv2Token.toNonNullableProperty(""))
                .component
        }.layout(RowLayout.PARENT_GRID)
    }

    override fun getPreferredFocusedComponent() = idTextField
}