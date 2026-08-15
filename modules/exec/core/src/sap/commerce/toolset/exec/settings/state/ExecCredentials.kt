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

package sap.commerce.toolset.exec.settings.state

import com.intellij.credentialStore.Credentials
import com.intellij.openapi.observable.properties.AtomicProperty
import com.intellij.openapi.observable.properties.ObservableMutableProperty
import sap.commerce.toolset.settings.state.Mutation

data class ExecCredentials(
    val mutation: Mutation,
    private val username: String,
    private val password: String,
) {
    val credentials: Credentials
        get() = Credentials(username, password)

    data class Mutable(
        var username: ObservableMutableProperty<String> = AtomicProperty(""),
        var password: ObservableMutableProperty<String> = AtomicProperty(""),
    ) {
        var mutation: Mutation = Mutation.NONE
            private set
        var loaded: Boolean = false
            private set

        fun load(credentials: Credentials?) = set(
            username = credentials?.userName ?: "",
            password = credentials?.getPasswordAsString() ?: ""
        )

        fun load(mutable: Mutable) = if (mutable.loaded) set(mutable.username.get(), mutable.password.get(), mutable.mutation)
        else Unit

        fun apply(mutable: Mutable) = set(mutable.username.get(), mutable.password.get(), Mutation.SAVE)

        private fun set(username: String, password: String, mutation: Mutation = Mutation.NONE) {
            this.loaded = true
            this.mutation = mutation
            this.username.set(username)
            this.password.set(password)
        }

        fun immutable() = ExecCredentials(mutation, username.get(), password.get())
    }
}
