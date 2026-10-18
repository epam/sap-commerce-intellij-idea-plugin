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

package sap.commerce.toolset.properties.exec

/**
 * The accumulated snapshot of one connection.
 *
 * Written by the fetch coroutines and read by the UI, so the page is published through a volatile field and every
 * write that derives the next page from the current one holds the lock.
 */
class CxRemotePropertyState(initial: CxRemotePropertyStatePage? = null) {
    @Volatile
    private var statePage = initial

    fun initialized(): Boolean = statePage != null

    fun get(): CxRemotePropertyStatePage? = statePage

    /** Replaces the accumulated list. Used for the first page of a fresh filter. */
    fun replace(newState: CxRemotePropertyStatePage) {
        synchronized(this) {
            statePage = newState
        }
    }

    /**
     * Appends the newly fetched page to the accumulated list. If the filter/page-size of
     * [newState] does not match the current snapshot, this falls back to [replace] — the
     * old snapshot becomes stale once the filter changes mid-flight.
     */
    fun append(newState: CxRemotePropertyStatePage) {
        synchronized(this) {
            val current = statePage
            statePage = if (current != null
                && current.keyFilter == newState.keyFilter
                && current.valueFilter == newState.valueFilter
                && current.pageSize == newState.pageSize
            ) {
                val seenKeys = current.properties.mapTo(HashSet(current.properties.size)) { it.key }
                val merged = current.properties + newState.properties.filter { it.key !in seenKeys }
                newState.copy(properties = merged.sortedBy { it.key })
            } else {
                newState
            }
        }
    }

    fun clear() {
        synchronized(this) {
            statePage = null
        }
    }
}
