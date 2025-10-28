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

package scripts

import de.hybris.platform.core.Registry
import de.hybris.platform.core.model.type.ComposedTypeModel
import de.hybris.platform.jalo.security.Principal
import de.hybris.platform.servicelayer.model.ModelService
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery
import de.hybris.platform.servicelayer.search.FlexibleSearchService
import de.hybris.platform.servicelayer.type.TypeService
import de.hybris.platform.servicelayer.user.UserService

/*
======= Version: 2025.2.4.5 =======

This script is being used by the Plugin to retrieve FlexibleSearch restrictions for the given User and respective ComposedTypes.

Unfortunately, this script relies on deprecated Jalo Layer, because this functionality is not yet fully migrated to Service Layer.

The following contract is expected:
 - types with the exclamation mark `!` will be indicate a need to exclude sub types of the specific type.
 - types with the star `*` will be excluded completely as it disables flexible search restrictions internally in [y].
 - `placeholder_userUid` will be used to inject target User UID in double quotes.
 - `placeholder_Types` will be used to inject target Types as comma separated array of Type codes in double quotes and boolean flag representing a need to include/exclude sub types.
 - script must print the results as a return value of the script.
 - each search restriction details must be defined as a json object.
 - each search restriction details must consist of three values
    - 1: "code"     -> search restriction code.
    - 2: "typeCode" -> search restriction query.
    - 3: "query"    -> code of the restricted type associated with the search restriction.

======= Example =======
-- Input --
def userUid = "cmsmanager"
def types =  [
        ["User", false],
        ["PrincipalGroupRelation", false],
        ["SearchRestriction", false],
        ["Product", true],
        ["User", true] // repeated
]

-- Output --
[
 {
 "code": "HideSystemPrincipals",
 "typeCode": "Principal",
 "query": "{uid} not in ( 'anonymous', 'admin', 'admingroup' )",
 },
 {
 "code": "Backend_visibility",
 "typeCode": "VariantProduct",
 "query": "{catalogVersion} IN ( ?session.catalogversions ) OR EXISTS ({{SELECT {pk} FROM {Product AS base} WHERE {base:pk}={item:baseProduct} }})",
 },
 {
 "code": "Backend_visibility",
 "typeCode": "Product",
 "query": "{catalogVersion} IN ( ?session.catalogversions ) OR EXISTS ({{SELECT {pk} FROM {Category AS cat JOIN CategoryProductRelation AS c2p ON {cat:pk}={c2p:source} } WHERE {c2p:target}={item:pk} AND {cat:catalogVersion} NOT IN({{SELECT {pk} FROM { ClassificationSystemVersion} }})}})",
 }]
 */

def userUid = "placeholder_userUid"
def types = placeholder_types



def tenant = Registry.getCurrentTenantNoFallback()
def us = userService as UserService
def ms = modelService as ModelService
def fss = flexibleSearchService as FlexibleSearchService
def ts = typeService as TypeService

def jaloConnection = tenant.jaloConnection
def flexibleSearch = jaloConnection.getFlexibleSearch()

def user = us.getUserForUID(userUid)

def query = """
SELECT {sr.code}, {ct.code}, {p.uid}, {sr.query}
FROM {
           SearchRestriction* as sr
      JOIN ComposedType       as ct on {ct.pk} = {sr.restrictedType}
      JOIN Principal          as p  on {p.pk} = {sr.principal}
     }
WHERE
    {p.uid} in (?principalUids)
    AND {ct.code} in (?restrictedTypeCodes)
    AND {sr.active} = ?active
"""

def principalUids = flexibleSearch.getPrincipalsForSearchRestrictions(ms.getSource(user) as Principal, true)
        .collect { it.uid }

def restrictedTypeCodes = types
        .collect { key, value ->
            // key   -> ComposedType code
            // value -> include / exclude sub types
            def combinedTypes = new HashSet<ComposedTypeModel>()
            def type = ts.getComposedTypeForCode(key)

            combinedTypes.add(type)
            combinedTypes.addAll(type.getAllSuperTypes())
            if (value) combinedTypes.addAll(type.getAllSubTypes())

            return combinedTypes
        }
        .flatten()
        .<ComposedTypeModel> toSet()
        .collect { it.code }

def fxsQuery = new FlexibleSearchQuery(query, [
        "principalUids": principalUids,
        "restrictedTypeCodes": restrictedTypeCodes,
        "active": true,
])
fxsQuery.setResultClassList([String.class, String.class, String.class, String.class])

def result = fss.<List<List<String>>>search(fxsQuery).result
        .collect {
            """
                {
                    "code": "${it[0]}",
                    "typeCode": "${it[1]}",
                    "principal": "${it[2]}",
                    "query": "${it[3]}"
                }"""
        }
        .join(",")

return "[$result]"