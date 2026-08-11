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

package sap.commerce.toolset.exec.settings.state

import com.intellij.openapi.util.text.StringUtil

fun connectionPresentationName(scope: ExecConnectionScope, name: String?, fallback: () -> String): String = (name
    ?.takeIf { it.isNotBlank() }
    ?: fallback()
        .replace("-public.model-t.cc.commerce.ondemand.com", StringUtil.THREE_DOTS)
        .takeIf { it.isNotBlank() }
    )
    .let { scope.shortTitle + " : " + it }

/**
 * Persisted connections of the receiver which are not part of [retained] anymore, matched by [ConnectionSettingsState.uuid].
 *
 * Credentials of such connections are not referenced by any connection and have to be purged from the credential store,
 * whereas credentials of the retained ones must survive, even when the user did not open them before saving the settings.
 */
fun <T : ExecConnectionSettingsState> Collection<T>.obsoleteFor(retained: Collection<ExecConnectionSettingsState>): List<T> {
    val retainedUUIDs = retained.mapTo(mutableSetOf()) { it.uuid }

    return filterNot { retainedUUIDs.contains(it.uuid) }
}