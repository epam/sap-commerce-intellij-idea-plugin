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

package sap.commerce.toolset.flexibleSearch.mcp

import sap.commerce.toolset.flexibleSearch.exec.context.FlexibleSearchExecResult

/**
 * Number of the data rows returned by the server, `null` when the response carried no result list.
 */
internal val FlexibleSearchExecResult.rowCount: Int?
    get() = rows?.size

/**
 * Whether the result may have been capped by the `maxCount` limit of the request.
 *
 * The server does not report whether more rows were available, therefore an exactly-[maxCount] sized result
 * is reported as capped even when it happens to be complete. A false positive is intentional: it tells the
 * caller to re-run the query with a higher `maxCount`, whereas a silent cap is indistinguishable from a
 * complete result set.
 */
internal fun FlexibleSearchExecResult.maxCountReached(maxCount: Int): Boolean? = rowCount?.let { it >= maxCount }
