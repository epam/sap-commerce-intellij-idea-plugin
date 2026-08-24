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


import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import groovy.json.JsonOutput

/*
This script is being used by the Plugin to execute a FlexibleSearch query which expects more rows than the
HAC FlexibleSearch console is willing to return, therefore the query is executed on the Service Layer instead.

The following contract is expected:
 - `placeholder_query` will be used to inject the FlexibleSearch query, it must not contain a triple quote.
 - `placeholder_columnCount` will be used to inject the number of the columns of the query.
 - `placeholder_maxCount` will be used to inject the maximum number of the rows to return.
 - script must print the results as a return value of the script.
 - result must be a json array of the rows, each row being a json array of the column values as strings,
   also for a single column query, which is returned by the Service Layer as a flat list of the values.
 - `null` column value must be reported as a json `null`.

======= Example =======
-- Output --
[["8796093054993","MWST"],["8796093087761","ZDVP"]]
 */

def fss = flexibleSearchService as FlexibleSearchService

def query = new FlexibleSearchQuery('''placeholder_query''')
query.setResultClassList([String.class] * placeholder_columnCount)
query.setCount(placeholder_maxCount)

def rows = fss.search(query).result
        .collect { row ->
            // a single column query is returned as a flat list of the values, not as a list of the rows
            (row instanceof List ? row : [row]).collect { value -> value?.toString() }
        }

return JsonOutput.toJson(rows)
