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

package sap.commerce.toolset

import java.util.*
import java.util.regex.Pattern

object HybrisConstants {

    const val TOOLWINDOW_ID = "SAP CX"

    const val PLATFORM_VERSION_1811 = "1811"
    const val PLATFORM_VERSION_5_0 = "5.0"
    const val PLATFORM_VERSION_5_2 = "5.2"

    const val NOTIFICATION_GROUP_HYBRIS = "[y] SAP Commerce"

    const val UPDATE_TIMESTAMP_PROPERTY = "sap.commerce.update.timestamp"

    const val SERVER_JAR_SUFFIX = "server.jar"

    const val LIBRARY_GROUP_BACKOFFICE = "Backoffice Library"
    const val LIBRARY_GROUP_PLATFORM = "Platform Bootstrap"

    const val PATH_IDE_CONSOLES = "consoles/ide/"
    const val PATH_BOOTSTRAP_GEN_SRC = "bootstrap/gensrc"

    const val JAR_MODELS = "models.jar"
    const val SETTINGS_DIRECTORY = ".settings"
    const val EXTERNAL_TOOL_BUILDERS_DIRECTORY = ".externalToolBuilders"
    const val SPOCK_META_INF_SERVICES_DIRECTORY = "resources/META-INF/services"

    const val PLATFORM_HOME_PLACEHOLDER = "\${platformhome}"
    const val PLATFORM_DATABASE_DRIVER_LIBRARY = "Database Drivers"

    const val STORAGE_HYBRIS_PROJECT_SETTINGS = "hybrisProjectSettings.xml"
    const val STORAGE_HYBRIS_INTEGRATION_SETTINGS = "hybrisIntegrationSettings.xml"
    const val STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS = "hybrisDeveloperSpecificProjectSettings.xml"
    const val PLUGIN_ID = "com.intellij.idea.plugin.sap.commerce"

    const val NEW_IDEA_MODULE_FILE_EXTENSION = ".iml"
    const val HYBRIS_ITEMS_XML_FILE_ENDING = "-items.xml"
    const val HYBRIS_BEANS_XML_FILE_ENDING = "-beans.xml"
    const val CORE_ADVANCED_DEPLOYMENT_FILE = "core-advanced-deployment.xml"

    const val DEBUG_HOST = "localhost"
    const val DEBUG_PORT = "8000"

    // Properties defined in the env.properties File
    const val PROPERTY_HYBRIS_BIN_DIR = "HYBRIS_BIN_DIR"
    const val PROPERTY_HYBRIS_TEMP_DIR = "HYBRIS_TEMP_DIR"
    const val PROPERTY_HYBRIS_ROLES_DIR = "HYBRIS_ROLES_DIR"
    const val PROPERTY_HYBRIS_LOG_DIR = "HYBRIS_LOG_DIR"
    const val PROPERTY_HYBRIS_BOOTSTRAP_BIN_DIR = "HYBRIS_BOOTSTRAP_BIN_DIR"
    const val PROPERTY_HYBRIS_DATA_DIR = "HYBRIS_DATA_DIR"
    const val PROPERTY_HYBRIS_CONFIG_DIR = "HYBRIS_CONFIG_DIR"

    const val PROPERTY_PLATFORMHOME = "platformhome"

    const val PROPERTY_HAC_WEBROOT = "hac.webroot"
    const val PROPERTY_ADMIN_INITIAL_PASSWORD = "initialpassword.admin"
    const val PROPERTY_TOMCAT_SSL_PORT = "tomcat.ssl.port"

    const val PROPERTY_DEPLOYMENT_TABLENAME_MAXLENGTH = "deployment.tablename.maxlength"
    const val PROPERTY_BUILD_COMPILER = "build.compiler"
    const val PROPERTY_OPTIONAL_CONFIG_DIR = "hybris.optional.config.dir"
    const val PROPERTY_LANG_PACKS = "lang.packs"
    const val PROPERTY_INSTALLED_TENANTS = "installed.tenants"
    const val PROPERTY_IMPEX_HEADER_REPLACEMENT = "impex.header.replacement"
    const val PROPERTY_ENV_PROPERTY_PREFIX = "env.properties.prefix"

    const val PROPERTY_STANDALONE_JAVAOPTIONS = "standalone.javaoptions"
    const val PROPERTY_STANDALONE_JDKMODULESEXPORTS = "standalone.jdkmodulesexports"
    const val PROPERTY_BUNDLED_SERVER_TYPE = "bundled.server.type"

    const val DEFAULT_LANGUAGE_ISOCODE = "en"

    const val DEFAULT_DEPLOYMENT_TABLENAME_MAXLENGTH = 24

    const val TOMCAT_JAVA_DEBUG_OPTIONS = "tomcat.debugjavaoptions"
    const val X_RUNJDWP_TRANSPORT = "-Xrunjdwp:transport="
    const val ADDRESS = "address="

    const val ANT_ENCODING = "-Dfile.encoding=UTF-8"
    const val ANT_HYBRIS_CONFIG_DIR = "-J-D$PROPERTY_HYBRIS_CONFIG_DIR="
    const val ANT_XMX = "-Xmx"
    const val ANT_PLATFORM_HOME = "PLATFORM_HOME"
    const val ANT_OPTS = "ANT_OPTS"
    const val ANT_HOME = "ANT_HOME"
    const val ANT_COMPILING_XML = "resources/ant/compiling.xml"
    const val ANT_BUILD_XML = "build.xml"
    const val ANT_HEAP_SIZE_MB = 512
    const val ANT_STACK_SIZE_MB = 128

    const val JAVA_LANG_PREFIX = "java.lang."

    const val TS_TYPE_ITEM = "Item"
    const val TS_TYPE_GENERIC_ITEM = "GenericItem"
    const val TS_TYPE_LOCALIZABLE_ITEM = "LocalizableItem"
    const val TS_TYPE_EXTENSIBLE_ITEM = "ExtensibleItem"
    const val TS_TYPE_CRON_JOB = "CronJob"

    const val HYBRIS = "[y]"
    const val SEARCH_SCOPE_Y_PREFIX = HYBRIS
    const val IMPORT_OVERRIDE_FILENAME = "hybris4intellij.properties"
    const val GROUP_OVERRIDE_KEY = "group.override"

    const val CLASS_FQN_JALO_ITEM_ROOT = "de.hybris.platform.jalo.Item"
    const val CLASS_FQN_ITEM_ROOT = "de.hybris.platform.core.model.ItemModel"
    const val CLASS_FQN_ENUM_ROOT = "de.hybris.platform.core.HybrisEnumValue"
    const val CLASS_NAME_ENUM = "HybrisEnumValue"
    const val CLASS_FQN_INTERCEPTOR_TYPE = "de.hybris.platform.servicelayer.interceptor.impl.InterceptorExecutionPolicy.InterceptorType"
    const val CLASS_FQN_INTERCEPTOR_MAPPING = "de.hybris.platform.servicelayer.interceptor.impl.InterceptorMapping"
    const val CLASS_FQN_CMS_RESTRICTION_EVALUATOR_MAPPING = "de.hybris.platform.cms2.servicelayer.services.evaluator.CMSRestrictionEvaluatorMapping"
    const val CLASS_FQN_ANNOTATION_ACCESSOR = "de.hybris.bootstrap.annotations.Accessor"
    const val CLASS_FQN_CONFIG_IMPORT_PROCESSOR = "de.hybris.platform.commerceservices.impex.impl.ConfigPropertyImportProcessor"
    const val CLASS_FQN_CONVERTER = "de.hybris.platform.servicelayer.dto.converter.Converter"
    const val CLASS_FQN_POPULATOR = "de.hybris.platform.converters.Populator"
    const val CLASS_FQN_IMPEX_PROCESSOR = "de.hybris.platform.impex.jalo.imp.ImportProcessor"
    const val CLASS_FQN_IMPEX_ABSTRACT_TRANSLATOR = "de.hybris.platform.impex.jalo.translators.AbstractValueTranslator"
    const val CLASS_FQN_IMPEX_SPECIAL_TRANSLATOR = "de.hybris.platform.impex.jalo.translators.SpecialValueTranslator"
    const val CLASS_FQN_IMPEX_HEADER_TRANSLATOR = "de.hybris.platform.impex.jalo.translators.HeaderCellTranslator"
    const val CLASS_FQN_IMPEX_CELL_DECORATOR = "de.hybris.platform.util.CSVCellDecorator"
    const val CLASS_FQN_CNG_WIDGET_COMPONENT_RENDERER = "com.hybris.cockpitng.widgets.common.WidgetComponentRenderer"
    const val CLASS_FQN_CNG_COLLECTION_BROWSER_MOLD_STRATEGY = "com.hybris.cockpitng.widgets.collectionbrowser.mold.CollectionBrowserMoldStrategy"
    const val CLASS_FQN_FLEXIBLE_SEARCH_QUERY = "de.hybris.platform.servicelayer.search.FlexibleSearchQuery"
    const val CLASS_FQN_CODE_GENERATOR = "de.hybris.bootstrap.codegenerator.CodeGenerator"
    const val CLASS_FQN_UNIT_TEST = "de.hybris.bootstrap.annotations.UnitTest"
    const val CLASS_NAME_FLEXIBLE_SEARCH_QUERY = "FlexibleSearchQuery"

    val CLASS_FQN_IMPEX_TRANSLATORS = arrayOf(
        CLASS_FQN_IMPEX_SPECIAL_TRANSLATOR,
        CLASS_FQN_IMPEX_HEADER_TRANSLATOR,
        CLASS_FQN_IMPEX_ABSTRACT_TRANSLATOR
    )

    const val SPRING_WEB_FILE_SET_NAME = "web application context"
    const val APPLICATION_CONTEXT_SPRING_FILES = "application-context"
    const val ADDITIONAL_WEB_SPRING_CONFIG_FILES = "additionalWebSpringConfigs"
    const val GLOBAL_CONTEXT_SPRING_FILES = "global-context"

    const val ENV_HYBRIS_CONFIG_DIR = PROPERTY_HYBRIS_CONFIG_DIR
    const val ENV_HYBRIS_RUNTIME_PROPERTIES = "HYBRIS_RUNTIME_PROPERTIES"
    const val ENV_HYBRIS_OPT_CONFIG_DIR = "HYBRIS_OPT_CONFIG_DIR"
    const val ENV_HYBRIS_BOOTSTRAP_BIN_DIR = PROPERTY_HYBRIS_BOOTSTRAP_BIN_DIR

    const val URL_HELP_JAVADOC_FALLBACK = "https://help.sap.com/docs/SAP_COMMERCE/c5613bd3cc9942efb74d017b40eb0892/179bbc9b35274d7ca784e46b3beb40b2.html"
    const val URL_HELP_JAVADOC = "https://help.sap.com/doc/9fef7037b3304324b8891e84f19f2bf3/%s/en-US"
    const val URL_HELP_GENERATING_API_TOKENS = "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html"

    const val SPRING_NAMESPACE = "http://www.springframework.org/schema/beans"

    const val QUOTE_LENGTH = 2

    // see: de.hybris.bootstrap.config.PlatformConfig -> readMaxDepthAttribute(..)
    const val DEFAULT_EXTENSIONS_PATH_DEPTH = 10

    val DEFAULT_JUNK_FILE_NAMES = listOf(
        ".classpath",
        ".directory",
        ".externalToolBuilders",
        ".idea",
        ".pmd",
        ".project",
        ".ruleset",
        ".settings",
        ".springBeans",
        "beans.xsd",
        "classes",
        "eclipsebin",
        "extensioninfo.xsd",
        "items.xsd",
        "platformhome.properties",
        "ruleset.xml",
        "testclasses"
    )

    val DEFAULT_EXTENSIONS_RESOURCES_TO_EXCLUDE = listOf(
        "solrserver",
        "npmancillary"
    )

    val DEFAULT_EXCLUDED_FROM_INDEX = listOf(
        "smartedit-custom-build",
        "smartedit-build",
        "node_modules",
        "apps/**/node_modules",
        "common/temp/node_modules"
    )

    val OCC_DEFAULT_LEVEL_MAPPINGS = setOf("BASIC", "DEFAULT", "FULL")

    @JvmField
    val IMPEX_MODIFIER_BOOLEAN_VALUES = setOf("true", "false")

    const val PLATFORM_EXT_MODULE_PREFIX = "bin/platform/ext/"
    const val HYBRIS_OOTB_MODULE_PREFIX = "hybris/bin/ext-"
    const val HYBRIS_OOTB_MODULE_PREFIX_2019 = "hybris/bin/modules/"

    @JvmField
    val LOCAL_GROUP_OVERRIDE_COMMENTS = """
        In this file you can override default module grouping and add additional ant parameters.
        Add a property group.override and value group name.
        If you use subgroups use / as a separator. For example group.override=mygroup/mysubgroup
        """.trimIndent()

    @JvmField
    val GLOBAL_GROUP_OVERRIDE_COMMENTS = """
        In this file you can override default module group for your extensions.
        Add a property <modulename>.group.override and group name as a value.
        If you use subgroups use / as a separator. For example myextension.group.override=mygroup/mysubgroup.
        It is recommended to keep custom hybris modules within custom group i.e. custom/subgroup, so that the generated search scopes would function correctly.

        Use ANT_OPTS to override ant properties. Current default value is
        ANT_OPTS=-Xmx512m -Dfile.encoding=UTF-8
        """.trimIndent()

    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/ActionsBundle.properties
    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/EditorBundle.properties
    @JvmField
    val UNDO_REDO_EDITOR_ACTIONS = arrayOf("Undo", "Redo")

    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/ActionsBundle.properties
    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/EditorBundle.properties
    @JvmField
    val TYPING_EDITOR_ACTIONS = arrayOf(
        "Typing",
        "Delete to Word Start",
        "Delete to Word End",
        "Duplicate Line or Selection",
        "Duplicate Entire Lines",
        "Backspace",
        "Delete",
        "Delete Line",
        "Cut",
        "Paste",
        "Paste _without Formatting",
        "Paste without formatting, autoimport, literal escaping etc.",
        "Paste from X clipboard",
        "Hungry Backspace",
        "Acts as the Backspace except that removes all whitespace symbols before the caret (if any)",
        "Move Line Up",
        "Move Line Down",
        "Move Statement Up",
        "Move Statement Down",
        "Move Element Left",
        "Move Element Right",
        "Reformat Code",
        "Undo Reformat Code",
        "Auto-Indent Lines"
    )

    val CHARS_UPPERCASE_REGEX = "[A-Z]".toRegex()
    val CHARS_LOWERCASE_REGEX = "[a-z]".toRegex()

    object Documentation {
        const val WIKI_ACL = "https://github.com/epam/sap-commerce-intellij-idea-plugin/wiki/Languages:-Access-Control-Lists"
    }

    object Locales {
        val LOCALIZED_FILE_NAME: Pattern = Pattern.compile("(_[a-zA-Z]{2,8}(_[a-zA-Z]{2}|[0-9]{3})?(_[\\w\\-]+)?)\\.[^_]+$")
        val LOCALES_CODES by lazy {
            val locales = Locale.getAvailableLocales()
                .flatMap {
                    try {
                        listOf(it.language, it.isO3Language)
                    } catch (_: MissingResourceException) {
                        listOf(it.language)
                    }
                }
            (Locale.getISOLanguages() + locales)
                .filter { it.isNotBlank() }
                .toSortedSet()
        }
    }

    object Languages {
        object ImpEx {
            const val EXTENSION = "impex"
        }

        object FlexibleSearch {
            const val EXTENSION = "fxs"
        }

        object PolyglotQuery {
            const val EXTENSION = "pgq"
        }

        object Acl {
            const val NAME = "Access Control Lists"
            const val EXTENSION = "acl"
        }
    }

    object Folding {
        const val NO_VALUE = "🚫" // ? or 🚫 ∅
        const val DESCRIPTION_PREFIX = "ⓘ"
    }
}