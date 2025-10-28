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
import de.hybris.platform.jalo.security.Principal

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

def jaloSession = tenant.activeSession
def jaloConnection = tenant.jaloConnection
def typeManager = jaloSession.typeManager
def flexibleSearch = jaloConnection.getFlexibleSearch()

def user = modelService.getSource(userService.getUserForUID(userUid)) as Principal

def result = types
        .collect { key, value ->
            def composedType = typeManager.getComposedType(key)
            flexibleSearch.getQueryFilters(user, composedType, true, true, value)
        }
        .flatten()
        .unique { [it.code, it.restrictionType.code] }
        .collect {
            """
                {
                    "code": "${it.code}",
                    "typeCode": "${it.restrictionType.code}",
                    "query": "${it.query}"
                }"""
        }
        .join(",")

return "[$result]"