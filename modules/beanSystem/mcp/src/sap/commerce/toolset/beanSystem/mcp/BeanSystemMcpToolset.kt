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

package sap.commerce.toolset.beanSystem.mcp

import com.intellij.mcpserver.McpToolset
import com.intellij.mcpserver.annotations.McpDescription
import com.intellij.mcpserver.annotations.McpTool
import sap.commerce.toolset.ai.mcp.map
import sap.commerce.toolset.ai.mcp.resolveMapper
import sap.commerce.toolset.beanSystem.meta.model.BSMetaType

class BeanSystemMcpToolset : McpToolset {

    @McpTool(name = "sap_commerce_list_dto_beans")
    @McpDescription(
        """Lists the DTO beans defined in the current project's SAP Commerce (Hybris) bean system, as shown in the "Bean System" tool window.
        |A DTO bean is a `<bean>` (type='bean') declared in a `*-beans.xml` file — a data-transfer object, NOT a web-service bean (see 'sap_commerce_list_ws_beans') and NOT an event (see 'sap_commerce_list_event_beans').
        |This is the project's LOCAL model, parsed from the `*-beans.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "extensions", "matched", "total", "items": [{"name", "shortName", "extends", "template", "extension", "custom", "abstract", "deprecated", ...}]}. Boolean flags are present only when true and omitted otherwise.
        |A project can define many beans, so narrow the result with 'filter' (by name/package) and/or 'extensions' (by owning extension), and use 'detail' to control how much per-bean information is returned, keeping the response (and token usage) small."""
    )
    suspend fun listDtoBeans(
        @McpDescription(
            """Optional bean-name filter used to shrink the response and save tokens. Matched against the bean's fully-qualified name (package + class), so it filters by name AND package.
            |If the value is a valid regular expression it is matched with a regex search (e.g. '(?i)productdata' or '^de\.hybris\.platform\.commercefacades\.'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all DTO beans."""
        )
        filter: String? = null,

        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to beans owned by those extensions (e.g. 'commercefacades,core').
            |Matched case-insensitively and exactly against each bean's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include beans from all extensions."""
        )
        extensions: String? = null,

        @McpDescription(
            """Controls how much information is returned per bean, to balance completeness against token usage:
            |- BASIC: bean identity only (name, shortName, extends, template, extension, and the custom/abstract/deprecated flags). No properties.
            |- MEMBERS: the above plus each bean's declared properties as {name, type, referencedType}.
            |- FULL: the above plus description, deprecatedSince, superEquals, imports, annotations, and per-property description/deprecated. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question. Properties are the bean's DECLARED properties, not inherited ones."""
        )
        detail: String = BSDetail.BASIC.name,

        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val beanDetail = BSDetail.resolve(detail)
        val context = BSMcpSearchContext(BSMetaType.META_BEAN, filter, extensions)
        val beans = BSMcpService.getInstance().searchBeans(context, beanDetail)
        return mapper.map(beans)
    }

    @McpTool(name = "sap_commerce_list_ws_beans")
    @McpDescription(
        """Lists the WS (web-service) DTO beans defined in the current project's SAP Commerce (Hybris) bean system, as shown in the "Bean System" tool window.
        |A WS DTO bean is a `<bean>` declared in a `*-beans.xml` file that is flagged as web-service related (via a 'wsRelated' hint) — a data-transfer object used by web services. These are a subset of DTO beans; plain DTO beans are 'sap_commerce_list_dto_beans' and events are 'sap_commerce_list_event_beans'.
        |This is the project's LOCAL model, parsed from the `*-beans.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "extensions", "matched", "total", "items": [{"name", "shortName", "extends", "template", "extension", "custom", "abstract", "deprecated", ...}]}. Boolean flags are present only when true and omitted otherwise.
        |Narrow the result with 'filter' (by name/package) and/or 'extensions' (by owning extension), and use 'detail' to control how much per-bean information is returned, keeping the response (and token usage) small."""
    )
    suspend fun listWsBeans(
        @McpDescription(
            """Optional bean-name filter used to shrink the response and save tokens. Matched against the bean's fully-qualified name (package + class), so it filters by name AND package.
            |If the value is a valid regular expression it is matched with a regex search (e.g. '(?i)wsdto' or '^de\.hybris\.platform\.'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all WS beans."""
        )
        filter: String? = null,

        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to beans owned by those extensions (e.g. 'ycommercewebservices,core').
            |Matched case-insensitively and exactly against each bean's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include beans from all extensions."""
        )
        extensions: String? = null,

        @McpDescription(
            """Controls how much information is returned per bean, to balance completeness against token usage:
            |- BASIC: bean identity only (name, shortName, extends, template, extension, and the custom/abstract/deprecated flags). No properties.
            |- MEMBERS: the above plus each bean's declared properties as {name, type, referencedType}.
            |- FULL: the above plus description, deprecatedSince, superEquals, imports, annotations, and per-property description/deprecated. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question. Properties are the bean's DECLARED properties, not inherited ones."""
        )
        detail: String = BSDetail.BASIC.name,

        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val beanDetail = BSDetail.resolve(detail)
        val context = BSMcpSearchContext(BSMetaType.META_WS_BEAN, filter, extensions)
        val beans = BSMcpService.getInstance().searchBeans(context, beanDetail)
        return mapper.map(beans)
    }

    @McpTool(name = "sap_commerce_list_event_beans")
    @McpDescription(
        """Lists the event beans defined in the current project's SAP Commerce (Hybris) bean system, as shown in the "Bean System" tool window.
        |An event bean is a `<bean type='event'>` declared in a `*-beans.xml` file — an application event, NOT a plain DTO bean (see 'sap_commerce_list_dto_beans') and NOT a web-service bean (see 'sap_commerce_list_ws_beans').
        |This is the project's LOCAL model, parsed from the `*-beans.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "extensions", "matched", "total", "items": [{"name", "shortName", "extends", "template", "extension", "custom", "abstract", "deprecated", ...}]}. Boolean flags are present only when true and omitted otherwise.
        |Narrow the result with 'filter' (by name/package) and/or 'extensions' (by owning extension), and use 'detail' to control how much per-bean information is returned, keeping the response (and token usage) small."""
    )
    suspend fun listEventBeans(
        @McpDescription(
            """Optional event-bean-name filter used to shrink the response and save tokens. Matched against the bean's fully-qualified name (package + class), so it filters by name AND package.
            |If the value is a valid regular expression it is matched with a regex search (e.g. '(?i)event' or '^de\.hybris\.platform\.'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all event beans."""
        )
        filter: String? = null,

        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to event beans owned by those extensions (e.g. 'commerceservices,core').
            |Matched case-insensitively and exactly against each bean's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include event beans from all extensions."""
        )
        extensions: String? = null,

        @McpDescription(
            """Controls how much information is returned per event bean, to balance completeness against token usage:
            |- BASIC: bean identity only (name, shortName, extends, template, extension, and the custom/abstract/deprecated flags). No properties.
            |- MEMBERS: the above plus each bean's declared properties as {name, type, referencedType}.
            |- FULL: the above plus description, deprecatedSince, superEquals, imports, annotations, and per-property description/deprecated. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question. Properties are the bean's DECLARED properties, not inherited ones."""
        )
        detail: String = BSDetail.BASIC.name,

        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val beanDetail = BSDetail.resolve(detail)
        val context = BSMcpSearchContext(BSMetaType.META_EVENT, filter, extensions)
        val beans = BSMcpService.getInstance().searchBeans(context, beanDetail)
        return mapper.map(beans)
    }

    @McpTool(name = "sap_commerce_list_bean_enums")
    @McpDescription(
        """Lists the enums defined in the current project's SAP Commerce (Hybris) bean system, as shown in the "Bean System" tool window.
        |A bean-system enum is an `<enum>` declared in a `*-beans.xml` file. This is the bean-system enum model, NOT the type-system enum types (which are declared in `*-items.xml` and exposed by 'sap_commerce_list_enum_types').
        |This is the project's LOCAL model, parsed from the `*-beans.xml` definitions — it does NOT query a remote server and does NOT require a HAC connection.
        |Returns a JSON object: {"detail", "filter", "extensions", "matched", "total", "items": [{"name", "shortName", "extension", "custom", "deprecated", ...}]}. Boolean flags are present only when true and omitted otherwise.
        |Narrow the result with 'filter' (by name/package) and/or 'extensions' (by owning extension), and use 'detail' to control how much per-enum information is returned, keeping the response (and token usage) small."""
    )
    suspend fun listBeanEnums(
        @McpDescription(
            """Optional enum-name filter used to shrink the response and save tokens. Matched against the enum's fully-qualified name (package + class), so it filters by name AND package.
            |If the value is a valid regular expression it is matched with a regex search (e.g. '(?i)ordertype' or '^de\.hybris\.platform\.'); otherwise it is treated as a plain, case-insensitive substring ('contains').
            |Omit to return all bean-system enums."""
        )
        filter: String? = null,

        @McpDescription(
            """Optional comma-separated list of extension names to restrict the result to enums owned by those extensions (e.g. 'commercefacades,core').
            |Matched case-insensitively and exactly against each enum's owning 'extension'. Combined with 'filter' using AND (both must match).
            |Omit to include enums from all extensions."""
        )
        extensions: String? = null,

        @McpDescription(
            """Controls how much information is returned per enum, to balance completeness against token usage:
            |- BASIC: enum identity only (name, shortName, extension, and the custom/deprecated flags). No values.
            |- MEMBERS: the above plus the enum's value names.
            |- FULL: the above plus description and deprecatedSince. Only non-empty values are included.
            |Default: BASIC. Prefer the smallest level that answers the question."""
        )
        detail: String = BSDetail.BASIC.name,

        @McpDescription("Output format for the response. Supported formats: JSON. Default: JSON.")
        outputFormat: String = "JSON",
    ): String {
        val mapper = resolveMapper(outputFormat)
        val enumDetail = BSDetail.resolve(detail)
        val context = BSMcpSearchContext(BSMetaType.META_ENUM, filter, extensions)
        val enums = BSMcpService.getInstance().searchEnums(context, enumDetail)
        return mapper.map(enums)
    }
}
