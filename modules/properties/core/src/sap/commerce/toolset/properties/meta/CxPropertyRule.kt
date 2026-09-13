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

package sap.commerce.toolset.properties.meta

/**
 * Properties which SAP Commerce Cloud documents as not being the project's to declare.
 *
 * The catalogue is transcribed from the SAP Help Portal, whose wording is the source of every message here:
 * - [Managed Properties](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/a30160b786b545959184898b51c737fa.html)
 *   — "Modifying these properties may cause your builds to fail."
 * - [Properties](https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/d090fb3dd48a418d967a1dfdca9fcac6.html)
 *   — "Automation introduces parameters that you can read but shouldn't be override as they're essential for Automation
 *   to work correctly", and "You can only modify the properties specified in the Customer column."
 *
 * Adding a rule is adding an entry: everything which reports, explains and fixes it is driven off this catalogue.
 *
 * @param scopes tiers of the chain the rule is reported in. A value injected by the platform at runtime matches the
 * catalogue too, but it is not the project's doing and there is nothing to fix about it.
 */
enum class CxPropertyRule(
    val title: String,
    val documentationUrl: String,
    val severity: CxPropertyRuleSeverity,
    val scopes: Set<CxPropertyScope>,
    val keys: Set<String>,
    val keyPrefixes: Set<String> = emptySet(),
) {

    /**
     * Injected per environment when the environment is deployed. A project which declares one of these hard-codes one
     * environment into a build meant to run in all of them, and the injected value wins anyway.
     */
    MANAGED_BY_AUTOMATION(
        title = "set by SAP Commerce Cloud automation for each environment",
        documentationUrl = "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/d090fb3dd48a418d967a1dfdca9fcac6.html",
        severity = CxPropertyRuleSeverity.ERROR,
        scopes = setOf(CxPropertyScope.PROJECT),
        keys = setOf(
            "db.url",
            "db.driver",
            "db.username",
            "db.password",
            "media.default.storage.strategy",
            "media.globalSettings.cloudAzureBlobStorageStrategy.connection",
            "media.globalSettings.cloudAzureBlobStorageStrategy.connection2",
            "media.globalSettings.cloudAzureBlobStorageStrategy.public.base.url",
            "media.globalSettings.cloudAzureBlobStorageStrategy.public.base.url2",
            "media.globalSettings.cloudAzureBlobStorageStrategy.local.cache",
            "azure.hotfolder.storage.account.connection-string",
            "azure.hotfolder.storage.account.name",
            "initialpassword.admin",
        ),
        keyPrefixes = setOf(
            "modelt.",
            "businessmetrics.",
            "ccv2.services.",
        ),
    ),

    /**
     * Written by the build process itself. The documentation is blunt about the consequence of touching them, so this
     * one is reported wherever it is declared, `local.properties` included.
     */
    MANAGED_BY_BUILD(
        title = "set by the SAP Commerce Cloud build process",
        documentationUrl = "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/a30160b786b545959184898b51c737fa.html",
        severity = CxPropertyRuleSeverity.WARNING,
        scopes = setOf(CxPropertyScope.PROJECT, CxPropertyScope.LOCAL),
        keys = setOf(
            "tomcat.generaloptions.jmxsettings",
            "tomcat.jmx.port",
            "tomcat.jmx.server.port",
            "tomcat.http.port",
            "tomcat.ssl.port",
            "tomcat.ajp.port",
            "tomcat.ajp.secureport",
            "proxy.http.port",
            "proxy.ssl.port",
            "tomcat.javaoptions",
            "java.mem",
            "tomcat.generaloptions.jvmsettings",
            "tomcat.generaloptions.dynatrace",
            "tomcat.generaloptions.GC",
            "standalone.javaoptions",
            "tenant.restart.on.connection.error",
            "regioncache.stats.enabled",
            "cms.cache.enabled",
            "regioncache.entityregion.size",
            "authserver.keystore.location",
            "authserver.keystore.key.generate",
            "authserver.keystore.type",
            "multicountrysampledataaddon.import.active",
            "runtime.config.refresh.time.seconds",
            "runtime.config.files.ccv2HotReloadProperties",
            "audit.siem.enabled",
            "audit.siem.denylist",
        ),
    ),

    /**
     * Overwritten or removed during an update unless they are edited in the Cloud Portal, using the `hcs_admin` aspect.
     */
    CLOUD_PORTAL_ONLY(
        title = "only safe to edit in the Cloud Portal, using the hcs_admin aspect",
        documentationUrl = "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/1be46286b36a4aa48205be5a96240672/a30160b786b545959184898b51c737fa.html",
        severity = CxPropertyRuleSeverity.WARNING,
        scopes = setOf(CxPropertyScope.PROJECT, CxPropertyScope.LOCAL),
        keys = setOf(
            "bootstrap.init.type.system.custom.indices.use.items.definitions",
            "bootstrap.init.type.system.custom.index.ignore.names.starting.with",
        ),
    ),
    ;

    fun matches(key: String) = key in keys || keyPrefixes.any { key.startsWith(it) }

    /** Files the rule is reported in, which is how an inspection knows whether to look at the one it was given. */
    val fileNames
        get() = scopes.mapNotNull { it.fileName }.toSet()

    companion object {
        fun of(key: String) = entries.find { it.matches(key) }

        fun of(severity: CxPropertyRuleSeverity) = entries.filterTo(mutableSetOf()) { it.severity == severity }
    }
}
