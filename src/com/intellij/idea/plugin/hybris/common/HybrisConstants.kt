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
package com.intellij.idea.plugin.hybris.common

import com.intellij.codeInsight.completion.CompletionUtilCore
import com.intellij.facet.FacetTypeId
import com.intellij.idea.plugin.hybris.facet.YFacet
import com.intellij.idea.plugin.hybris.facet.YFacetType
import com.intellij.idea.plugin.hybris.flexibleSearch.FlexibleSearchLanguage
import com.intellij.idea.plugin.hybris.flexibleSearch.editor.FlexibleSearchParameter
import com.intellij.idea.plugin.hybris.flexibleSearch.psi.FlexibleSearchTypes
import com.intellij.idea.plugin.hybris.impex.ImpexLanguage
import com.intellij.idea.plugin.hybris.polyglotQuery.psi.PolyglotQueryTypes
import com.intellij.idea.plugin.hybris.project.descriptors.HybrisProjectDescriptor
import com.intellij.idea.plugin.hybris.project.descriptors.ModuleDescriptor
import com.intellij.openapi.actionSystem.DataKey
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import com.intellij.psi.tree.IFileElementType
import java.time.format.DateTimeFormatter

object HybrisConstants {

    const val PLATFORM_VERSION_1811 = "1811"
    const val PLATFORM_VERSION_5_0 = "5.0"
    const val PLATFORM_VERSION_5_2 = "5.2"

    const val CCV2_DOMAIN = "portal.commerce.ondemand.com"

    const val CCV2_MANIFEST_NAME = "manifest.json"
    const val CCV2_CORE_CUSTOMIZE_NAME = "core-customize"
    const val CCV2_DATAHUB_NAME = "datahub"
    const val CCV2_JS_STOREFRONT_NAME = "js-storefront"
    val CCV2_COMMERCE_CLOUD_EXTENSIONS = arrayOf(
        "azurecloudhotfolder",
        "cloudmediaconversion",
        "cloudcommons",
        "cloudhotfolder",
        "cloudstorestorefront",
        "cloudstoreinitialdata",
        "cloudstorefulfilmentprocess",
        "cloudstorefacades",
        "cloudstorecore",
        "cloudstorecockpits"
    )
    val CCV2_COMMERCE_EXTENSION_PACKS = arrayOf(
        "hybris-commerce-integrations",
        "hybris-datahub-integration-suite",
        "cx-commerce-crm-integrations",
        "media-telco"
    )

    val Y_FACET_TYPE_ID = FacetTypeId<YFacet>(YFacetType.FACET_ID)

    const val NOTIFICATION_GROUP_HYBRIS = "[y] SAP Commerce"

    const val IDEA_EDITION_ULTIMATE = "Ultimate Edition"
    const val UPDATE_TIMESTAMP_PROPERTY = "sap.commerce.update.timestamp"

    const val IMPEX_CONFIG_PREFIX = "\$config"
    const val IMPEX_CONFIG_COMPLETE_PREFIX = "$IMPEX_CONFIG_PREFIX-"
    const val IMPEX = "ImpEx"
    const val IMPEX_FILE_EXTENSION = "impex"

    const val ACL = "Access Control Lists"
    const val ACL_FILE_EXTENSION = "acl"

    const val CONSOLE_TITLE_IMPEX = "[y] ImpEx Console"
    const val CONSOLE_TITLE_IMPEX_MONITOR = "[y] Monitor Console"
    const val CONSOLE_TITLE_GROOVY = "[y] Groovy Console"
    const val CONSOLE_TITLE_FLEXIBLE_SEARCH = "[y] FxS Console"
    const val CONSOLE_TITLE_SOLR_SEARCH = "[y] Solr search"
    const val CONSOLE_TITLE_POLYGLOT_QUERY = "[y] PolyglotQuery"

    const val ROOT_TAG_BUSINESS_PROCESS_XML = "process"
    const val ROOT_TAG_ITEMS_XML = "items"
    const val ROOT_TAG_DEPLOYMENT_MODEL_XML = "model"
    const val ROOT_TAG_EXTENSION_INFO_XML = "extensioninfo"

    const val DOT_PROJECT = ".project"
    const val GRADLE_SETTINGS = "settings.gradle"
    const val GRADLE_SETTINGS_KTS = "settings.gradle.kts"
    const val GRADLE_BUILD = "build.gradle"
    const val GRADLE_BUILD_KTS = "build.gradle.kts"
    const val LOCAL_EXTENSIONS_XML = "localextensions.xml"
    const val BUILD_CALLBACKS_XML = "buildcallbacks.xml"
    const val FILE_ANGULAR_JSON = "angular.json"
    const val EXTERNAL_DEPENDENCIES_XML = "external-dependencies.xml"
    const val UNMANAGED_DEPENDENCIES_TXT = "unmanaged-dependencies.txt"
    const val EXTENSION_INFO_XML = "extensioninfo.xml"
    const val EXTENSIONS_XML = "extensions.xml"
    const val COCKPIT_NG_DEFINITION_XML = "definition.xml"
    const val HYBRIS_LICENCE_JAR = "hybrislicence.jar"

    const val HYBRIS_DIRECTORY = "hybris"
    const val HYBRIS_DATA_DIRECTORY = "data"
    const val HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX = "server.jar"

    const val EXTENSION_NAME_BACK_OFFICE = "backoffice"
    const val EXTENSION_NAME_CORE = "core"
    const val EXTENSION_NAME_CONFIG = "config"
    const val EXTENSION_NAME_HMC = "hmc"
    const val EXTENSION_NAME_HAC = "hac"
    const val EXTENSION_NAME_PLATFORM = "platform"
    const val EXTENSION_NAME_PLATFORM_SERVICES = "platformservices"
    const val EXTENSION_NAME_ADDONSUPPORT = "addonsupport"
    const val EXTENSION_NAME_KOTLIN_NATURE = "kotlinnature"

    const val EXTENSION_META_KEY_BACKOFFICE_MODULE = "backoffice-module"
    const val EXTENSION_META_KEY_HAC_MODULE = "hac-module"
    const val EXTENSION_META_KEY_CLASSPATHGEN = "classpathgen"
    const val EXTENSION_META_KEY_DEPRECATED = "deprecated"
    const val EXTENSION_META_KEY_EXT_GEN = "extgen-template-extension"
    const val EXTENSION_META_KEY_MODULE_GEN = "modulegen-name"

    val EXTENSION_INFO_META_KEYS = listOf(
        EXTENSION_META_KEY_BACKOFFICE_MODULE,
        EXTENSION_META_KEY_HAC_MODULE,
        EXTENSION_META_KEY_CLASSPATHGEN,
        EXTENSION_META_KEY_DEPRECATED,
        EXTENSION_META_KEY_EXT_GEN,
        EXTENSION_META_KEY_MODULE_GEN
    )

    const val BACKOFFICE_MODULE_DIRECTORY = "backoffice"
    const val BACKOFFICE_LIBRARY_GROUP = "Backoffice Library"

    const val GEN_SRC_DIRECTORY = "gensrc"
    const val TEST_SRC_DIRECTORY = "testsrc"
    const val IDE_CONSOLES_PATH = "consoles/ide/"
    const val GROOVY_TEST_SRC_DIRECTORY = "groovytestsrc"
    const val KOTLIN_TEST_SRC_DIRECTORY = "kotlintestsrc"
    const val SCALA_TEST_SRC_DIRECTORY = "scalatestsrc"
    const val BOOTSTRAP_GEN_SRC_PATH = "bootstrap/gensrc"

    const val HMC_MODULE_DIRECTORY = "hmc"
    const val HAC_MODULE_DIRECTORY = "hac"
    const val WEB_MODULE_DIRECTORY = "web"
    const val COMMON_WEB_MODULE_DIRECTORY = "commonweb"

    const val ADDON_SRC_DIRECTORY = "addonsrc"
    const val TEST_CLASSES_DIRECTORY = "testclasses"
    const val CLASSES_DIRECTORY = "classes"
    const val JAR_MODELS = "models.jar"
    const val SETTINGS_DIRECTORY = ".settings"
    const val EXTERNAL_TOOL_BUILDERS_DIRECTORY = ".externalToolBuilders"
    const val WEB_ROOT_DIRECTORY = "webroot"
    const val ACCELERATOR_ADDON_DIRECTORY = "acceleratoraddon"

    const val COMMON_WEB_SRC_DIRECTORY = "commonwebsrc"

    const val PLATFORM_HOME_PLACEHOLDER = "\${platformhome}"
    const val PLATFORM_EXTENSIONS_DIRECTORY_NAME = "ext"
    const val PLATFORM_BOOTSTRAP_DIRECTORY = "bootstrap"
    const val PLATFORM_MODEL_CLASSES_DIRECTORY = "modelclasses"
    const val PLATFORM_TOMCAT_6_DIRECTORY = "tomcat-6"
    const val PLATFORM_TOMCAT_DIRECTORY = "tomcat"
    const val PLATFORM_LIBRARY_GROUP = "Platform Bootstrap"
    const val PLATFORM_DATABASE_DRIVER_LIBRARY = "Database Drivers"

    const val TOMCAT_WRAPPER_CONFIG_DIR = "conf"
    const val DEFAULT_WRAPPER_FILENAME = "wrapper.conf"

    const val NODE_MODULES_DIRECTORY = "node_modules"
    const val JS_TARGET_DIRECTORY = "jsTarget"
    const val BOWER_COMPONENTS_DIRECTORY = "bower_components"
    const val CONFIG_LICENCE_DIRECTORY = "licence"
    const val ECLIPSE_BIN_DIRECTORY = "eclipsebin"
    const val JAVA_COMPILER_FAKE_OUTPUT_PATH = ECLIPSE_BIN_DIRECTORY
    const val STORAGE_HYBRIS_PROJECT_SETTINGS = "hybrisProjectSettings.xml"
    const val STORAGE_HYBRIS_INTEGRATION_SETTINGS = "hybrisIntegrationSettings.xml"
    const val STORAGE_HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS = "hybrisDeveloperSpecificProjectSettings.xml"
    const val STORAGE_HYBRIS_TS_VIEW = "hybrisTypeSystemViewSettings.xml"
    const val STORAGE_HYBRIS_BS_VIEW = "hybrisBeanSystemViewSettings.xml"
    const val PLUGIN_ID = "com.intellij.idea.plugin.sap.commerce"

    const val NEW_IDEA_MODULE_FILE_EXTENSION = ".iml"
    const val HYBRIS_ITEMS_XML_FILE_ENDING = "-items.xml"
    const val HYBRIS_BEANS_XML_FILE_ENDING = "-beans.xml"
    const val HYBRIS_IMPEX_XML_FILE_ENDING = ".$IMPEX_FILE_EXTENSION"
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
    const val PROPERTY_SOLR_DEFAULT_PORT = "solrserver.instances.default.port"
    const val PROPERTY_SOLR_DEFAULT_USER = "solrserver.instances.default.user"
    const val PROPERTY_SOLR_DEFAULT_PASSWORD = "solrserver.instances.default.password"
    const val PROPERTY_DEPLOYMENT_TABLENAME_MAXLENGTH = "deployment.tablename.maxlength"
    const val PROPERTY_BUILD_COMPILER = "build.compiler"
    const val PROPERTY_OPTIONAL_CONFIG_DIR = "hybris.optional.config.dir"
    const val PROPERTY_LANG_PACKS = "lang.packs"
    const val PROPERTY_IMPEX_HEADER_REPLACEMENT = "impex.header.replacement"
    const val PROPERTY_ENV_PROPERTY_PREFIX = "env.properties.prefix"

    const val PROPERTY_STANDALONE_JAVAOPTIONS = "standalone.javaoptions"
    const val PROPERTY_STANDALONE_JDKMODULESEXPORTS = "standalone.jdkmodulesexports"
    const val PROPERTY_BUNDLED_SERVER_TYPE = "bundled.server.type"

    const val DEFAULT_LANGUAGE_ISOCODE = "en"

    const val DEFAULT_DEPLOYMENT_TABLENAME_MAXLENGTH = 24

    const val DEFAULT_HOST_URL = "localhost"
    const val DEFAULT_SSL_PROTOCOL = "TLSv1.2"
    const val DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID"

    const val HTTP_PROTOCOL = "http://"
    const val HTTPS_PROTOCOL = "https://"

    const val URL_PORT_DELIMITER = ":"
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
    const val ANT_LIB_DIR = "resources/ant/lib"
    const val ANT_BUILD_XML = "build.xml"
    const val ANT_HEAP_SIZE_MB = 512
    const val ANT_STACK_SIZE_MB = 128

    private const val JAVA_LANG_PREFIX = "java.lang."

    const val IMPEX_PREFIX_DOC_ID = "&"
    const val IMPEX_PREFIX_MACRO = "$"

    const val BS_TYPE_OBJECT = "java.lang.Object"
    const val BS_SIGN_LESS_THAN = "<"
    const val BS_SIGN_GREATER_THAN = ">"
    const val BS_SIGN_LESS_THAN_ESCAPED = "&lt;"
    const val BS_SIGN_GREATER_THAN_ESCAPED = "&gt;"
    const val BS_JAVA_LANG_PREFIX = JAVA_LANG_PREFIX

    const val TS_MAX_RECURSION_LEVEL = 2
    const val TS_TYPE_OBJECT = "java.lang.Object"
    const val TS_RELATION_ORDERING_POSTFIX = "POS"
    const val TS_TYPE_JAVA_CLASS = "java.lang.Class"
    const val TS_TYPE_ITEM = "Item"
    const val TS_TYPE_GENERIC_ITEM = "GenericItem"
    const val TS_TYPE_LOCALIZABLE_ITEM = "LocalizableItem"
    const val TS_TYPE_EXTENSIBLE_ITEM = "ExtensibleItem"
    const val TS_TYPE_SCRIPT = "Script"
    const val TS_TYPE_TRIGGER = "Trigger"
    const val TS_TYPE_CRON_JOB = "CronJob"
    const val TS_TYPE_CATALOG_VERSION = "CatalogVersion"
    const val TS_TYPE_LINK = "Link"
    const val TS_TYPE_SEARCH_RESTRICTION = "SearchRestriction"
    const val TS_TYPE_AFTER_RETENTION_CLEANUP_RULE = "AfterRetentionCleanupRule"
    const val TS_TYPE_ENUMERATION_VALUE = "EnumerationValue"
    const val TS_TYPE_ATTRIBUTE_DESCRIPTOR = "AttributeDescriptor"
    const val TS_TYPE_RELATION_DESCRIPTOR = "RelationDescriptor"
    const val TS_TYPE_USER = "User"
    const val TS_TYPE_USER_GROUP = "UserGroup"
    const val TS_META_VIEW_TYPE = "ViewType"
    const val TS_COMPOSED_TYPE = "ComposedType"
    const val TS_JAVA_LANG_PREFIX = JAVA_LANG_PREFIX
    const val TS_ATTRIBUTE_LOCALIZED_PREFIX = "localized:"
    const val TS_UNIQUE_KEY_ATTRIBUTE_QUALIFIER = "uniqueKeyAttributeQualifier"
    const val TS_CATALOG_ITEM_TYPE = "catalogItemType"
    const val TS_CATALOG_VERSION_ATTRIBUTE_QUALIFIER = "catalogVersionAttributeQualifier"
    const val TS_PRIMITIVE_BYTE = "byte"
    const val TS_PRIMITIVE_SHORT = "short"
    const val TS_PRIMITIVE_INT = "int"
    const val TS_PRIMITIVE_LONG = "long"
    const val TS_PRIMITIVE_FLOAT = "float"
    const val TS_PRIMITIVE_DOUBLE = "double"
    const val TS_PRIMITIVE_CHAR = "char"
    const val TS_PRIMITIVE_BOOLEAN = "boolean"
    val TS_PRIMITIVE_TYPES =
        setOf(TS_PRIMITIVE_BYTE, TS_PRIMITIVE_SHORT, TS_PRIMITIVE_INT, TS_PRIMITIVE_LONG, TS_PRIMITIVE_FLOAT, TS_PRIMITIVE_DOUBLE, TS_PRIMITIVE_CHAR, TS_PRIMITIVE_BOOLEAN)

    const val TS_TYPECODE_MIN_ALLOWED = 10000
    val TS_TYPECODE_RANGE_B2BCOMMERCE = TS_TYPECODE_MIN_ALLOWED..10099
    val TS_TYPECODE_RANGE_COMMONS = 13200..13299
    val TS_TYPECODE_RANGE_XPRINT = 24400..24599
    val TS_TYPECODE_RANGE_PRINT = 23400..23999
    val TS_TYPECODE_RANGE_PROCESSING = 32700..32799

    val BP_NAVIGABLE_ELEMENTS = setOf("action", "end", "wait", "join", "notify", "split", "scriptAction", "choice")

    const val HYBRIS = "[y]"
    const val DEBUG_MODEL_RENDERER_PREFIX = HYBRIS
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
    const val CLASS_NAME_FLEXIBLE_SEARCH_QUERY = "FlexibleSearchQuery"

    val CLASS_FQN_IMPEX_TRANSLATORS = arrayOf(
        CLASS_FQN_IMPEX_SPECIAL_TRANSLATOR,
        CLASS_FQN_IMPEX_HEADER_TRANSLATOR,
        CLASS_FQN_IMPEX_ABSTRACT_TRANSLATOR
    )

    const val MODEL_SUFFIX = "Model"
    const val TYPECODE_FIELD_NAME = "_TYPECODE"
    const val ATTRIBUTE_SOURCE = "source"
    const val ATTRIBUTE_TARGET = "target"
    const val ATTRIBUTE_KEY = "key"
    const val ATTRIBUTE_VALUE = "value"
    const val ATTRIBUTE_CODE = "code"
    const val ATTRIBUTE_NAME = "name"
    const val ATTRIBUTE_PK = "pk"
    const val DICTIONARY_NAME = "hybris_integration"
    const val DIALOG_TITLE = "hybris.copy.file.dialog."

    const val FLEXIBLE_SEARCH_FILE_EXTENSION = "fxs"
    const val FXS_TABLE_ALIAS_SEPARATOR_DOT = "."
    const val FXS_TABLE_ALIAS_SEPARATOR_COLON = ":"
    const val FXS_TABLE_POSTFIX_EXCLAMATION_MARK = "!"
    const val FXS_TABLE_POSTFIX_STAR = "*"

    const val LIB_DIRECTORY = "lib"
    const val BIN_DIRECTORY = "bin"
    const val RESOURCES_DIRECTORY = "resources"
    const val LOCAL_PROPERTIES_FILE = "local.properties"
    const val PROJECT_PROPERTIES_FILE = "project.properties"
    const val PLATFORMHOME_PROPERTIES_FILE = "platformhome.properties"
    const val ENV_PROPERTIES_FILE = "env.properties"
    const val ADVANCED_PROPERTIES_FILE = "advanced.properties"
    const val SPRING_WEB_FILE_SET_NAME = "web application context"
    const val APPLICATION_CONTEXT_SPRING_FILES = "application-context"
    const val ADDITIONAL_WEB_SPRING_CONFIG_FILES = "additionalWebSpringConfigs"
    const val GLOBAL_CONTEXT_SPRING_FILES = "global-context"

    const val ENV_HYBRIS_CONFIG_DIR = PROPERTY_HYBRIS_CONFIG_DIR
    const val ENV_HYBRIS_RUNTIME_PROPERTIES = "HYBRIS_RUNTIME_PROPERTIES"
    const val ENV_HYBRIS_OPT_CONFIG_DIR = "HYBRIS_OPT_CONFIG_DIR"
    const val ENV_HYBRIS_BOOTSTRAP_BIN_DIR = PROPERTY_HYBRIS_BOOTSTRAP_BIN_DIR

    const val HYBRIS_API_VERSION_KEY = "version.api"
    const val HYBRIS_VERSION_KEY = "version"
    const val URL_HELP_JAVADOC_FALLBACK = "https://help.sap.com/docs/SAP_COMMERCE/c5613bd3cc9942efb74d017b40eb0892/179bbc9b35274d7ca784e46b3beb40b2.html"
    const val URL_HELP_JAVADOC = "https://help.sap.com/doc/9fef7037b3304324b8891e84f19f2bf3/%s/en-US"
    const val URL_HELP_GENERATING_API_TOKENS = "https://help.sap.com/docs/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/0fa6bcf4736c46f78c248512391eb467/b5d4d851cbd54469906a089bb8dd58d8.html"

    const val SPRING_NAMESPACE = "http://www.springframework.org/schema/beans"

    const val QUOTE_LENGTH = 2

    // see: de.hybris.bootstrap.config.PlatformConfig -> readMaxDepthAttribute(..)
    const val DEFAULT_EXTENSIONS_PATH_DEPTH = 10

    const val COCKPIT_NG_NAMESPACE_KEY = "COCKPIT_NG_NAMESPACE"
    const val COCKPIT_NG_INITIALIZE_CONTEXT_TYPE = "ctx.TYPE_CODE"
    const val COCKPIT_NG_WIDGET_ID_STUB = "STUB_"
    const val COCKPIT_NG_TEMPLATE_BEAN_REFERENCE_PREFIX = "SPRING_BEAN_"

    const val SCHEMA_COCKPIT_NG_WIDGETS = "http://www.hybris.com/schema/cockpitng/widgets.xsd"
    const val SCHEMA_COCKPIT_NG_CONFIG = "http://www.hybris.com/cockpit/config"

    const val ANT_TARGET_UPDATE_MAVEN_DEPENDENCIES = "updateMavenDependencies"

    const val SECURE_STORAGE_SERVICE_NAME_SAP_CX_CCV2_TOKEN = "SAP CX CCv2 Token"

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

    const val KOTLIN_SRC_DIRECTORY = "kotlinsrc"
    private const val SRC_DIRECTORY = "src"
    private const val GROOVY_SRC_DIRECTORY = "groovysrc"
    private const val SCALA_SRC_DIRECTORY = "scalasrc"

    // kotlinnature extension integration
    const val KOTLIN_COMPILER_FALLBACK_VERSION = "1.8.21"
    const val KOTLIN_COMPILER_VERSION_PROPERTY_KEY = "kotlinnature.compiler.version"

    val OCC_DEFAULT_LEVEL_MAPPINGS = setOf("BASIC", "DEFAULT", "FULL")

    val CCV2_DATE_TIME_FORMATTER_LOCAL: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd | HH:mm:ss")

    @JvmField
    val IMPEX_MODIFIER_BOOLEAN_VALUES = setOf("true", "false")

    @JvmField
    val DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES = FileUtilRt.toSystemDependentName("/.idea/idea-modules")

    val RESERVED_TYPE_CODES_FILE = FileUtilRt.toSystemDependentName("resources/core/unittest/reservedTypecodes.txt")

    @JvmField
    val HYBRIS_SERVER_SHELL_SCRIPT_NAME = FileUtilRt.toSystemDependentName("bin/platform/hybrisserver.sh")

    @JvmField
    val HYBRIS_SERVER_BASH_SCRIPT_NAME = FileUtilRt.toSystemDependentName("bin/platform/hybrisserver.bat")

    @JvmField
    val PLATFORM_MODULE = FileUtilRt.toSystemDependentName("hybris/bin/platform")

    @JvmField
    val PLATFORM_MODULE_PREFIX = FileUtilRt.toSystemDependentName("/bin/platform/")

    @JvmField
    val PLATFORM_EXT_MODULE_PREFIX = FileUtilRt.toSystemDependentName("bin/platform/ext/")

    @JvmField
    val PLATFORM_OOTB_MODULE_PREFIX = FileUtilRt.toSystemDependentName("bin/ext-")

    @JvmField
    val PLATFORM_OOTB_MODULE_PREFIX_2019 = FileUtilRt.toSystemDependentName("bin/modules/")

    @JvmField
    val PLATFORM_DB_DRIVER = FileUtilRt.toSystemDependentName("lib/dbdriver")

    @JvmField
    val HYBRIS_OOTB_MODULE_PREFIX = FileUtilRt.toSystemDependentName("hybris/bin/ext-")

    @JvmField
    val HYBRIS_OOTB_MODULE_PREFIX_2019 = FileUtilRt.toSystemDependentName("hybris/bin/modules/")

    @JvmField
    val EXCLUDE_TMP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tmp")

    @JvmField
    val EXCLUDE_TCSERVER_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tcServer")

    @JvmField
    val EXCLUDE_TOMCAT_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tomcat")

    @JvmField
    val EXCLUDE_TOMCAT_6_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tomcat-6")

    @JvmField
    val EXCLUDE_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/lib")

    @JvmField
    val EXCLUDE_RESOURCES_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/resources")

    @JvmField
    val EXCLUDE_ECLIPSEBIN_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/eclipsebin")

    @JvmField
    val EXCLUDE_BOOTSTRAP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/bootstrap")

    @JvmField
    val EXCLUDE_ANT_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/apache-ant-")

    @JvmField
    val EXCLUDE_IDEA_MODULE_FILES_DIRECTORY = FileUtilRt.toSystemDependentName("/idea-module-files")

    @JvmField
    val EXCLUDE_LOG_DIRECTORY = FileUtilRt.toSystemDependentName("/log")

    @JvmField
    val EXCLUDE_DATA_DIRECTORY = FileUtilRt.toSystemDependentName("/data")

    @JvmField
    val EXCLUDE_SVN_DIRECTORY = FileUtilRt.toSystemDependentName("/.svn")

    @JvmField
    val EXCLUDE_GIT_DIRECTORY = FileUtilRt.toSystemDependentName("/.git")

    @JvmField
    val EXCLUDE_GITHUB_DIRECTORY = FileUtilRt.toSystemDependentName("/.github")

    @JvmField
    val EXCLUDE_GRADLE_DIRECTORY = FileUtilRt.toSystemDependentName("/.gradle")

    @JvmField
    val EXCLUDE_TEMP_DIRECTORY = FileUtilRt.toSystemDependentName("/temp")

    @JvmField
    val EXCLUDE_IDEA_DIRECTORY = FileUtilRt.toSystemDependentName("/.idea")

    @JvmField
    val EXCLUDE_MACOSX_DIRECTORY = FileUtilRt.toSystemDependentName("/__MACOSX")

    @JvmField
    val CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("bin/custom")

    @JvmField
    val CONFIG_RELATIVE_PATH = FileUtilRt.toSystemDependentName("/../../config")

    @JvmField
    val ADVANCED_PROPERTIES = FileUtilRt.toSystemDependentName("resources/advanced.properties")

    @JvmField
    val BUILD_NUMBER_FILE_PATH = FileUtilRt.toSystemDependentName("/bin/platform/build.number")

    @JvmField
    val WEBROOT_WEBINF_WEB_XML_PATH = FileUtilRt.toSystemDependentName("webroot/WEB-INF/web.xml")

    @JvmField
    val WEBROOT_WEBINF_CLASSES_PATH = FileUtilRt.toSystemDependentName("webroot/WEB-INF/classes")

    @JvmField
    val WEBROOT_WEBINF_LIB_PATH = FileUtilRt.toSystemDependentName("webroot/WEB-INF/lib")

    @JvmField
    val ACCELERATOR_ADDON_WEB_PATH = FileUtilRt.toSystemDependentName("acceleratoraddon/web")

    @JvmField
    val DOC_SOURCES_JAR_PATH = FileUtilRt.toSystemDependentName("doc/sources")

    @JvmField
    val DOC_SOURCES_PARENT_JAR_PATH = FileUtilRt.toSystemDependentName("../doc/sources")

    @JvmField
    val PL_BOOTSTRAP_LIB_PATH = FileUtilRt.toSystemDependentName("bootstrap/bin")

    @JvmField
    val PL_BOOTSTRAP_GEN_SRC_PATH = FileUtilRt.toSystemDependentName("bootstrap/gensrc")

    @JvmField
    val PL_TOMCAT_LIB_PATH = FileUtilRt.toSystemDependentName("tomcat/lib")

    @JvmField
    val PL_TOMCAT_BIN_PATH = FileUtilRt.toSystemDependentName("tomcat/bin")

    @JvmField
    val PL_TOMCAT_6_LIB_PATH = FileUtilRt.toSystemDependentName("tomcat-6/lib")

    @JvmField
    val PL_TOMCAT_6_BIN_PATH = FileUtilRt.toSystemDependentName("tomcat-6/bin")

    @JvmField
    val JAVA_COMPILER_OUTPUT_PATH = FileUtilRt.toSystemDependentName("/classes")

    @JvmField
    val HAC_WEB_INF_CLASSES = FileUtilRt.toSystemDependentName("/bin/platform/ext/hac/web/webroot/WEB-INF/classes")

    @JvmField
    val BACKOFFICE_LIB_PATH = FileUtilRt.toSystemDependentName("backoffice/bin")

    @JvmField
    val BACKOFFICE_JAR_PATH = FileUtilRt.toSystemDependentName("resources/backoffice")

    @JvmField
    val SRC_DIR_NAMES = listOf(SRC_DIRECTORY, GROOVY_SRC_DIRECTORY, KOTLIN_SRC_DIRECTORY, SCALA_SRC_DIRECTORY)

    @JvmField
    val ALL_SRC_DIR_NAMES = listOf(GEN_SRC_DIRECTORY, SRC_DIRECTORY, GROOVY_SRC_DIRECTORY, KOTLIN_SRC_DIRECTORY, SCALA_SRC_DIRECTORY)

    @JvmField
    val TEST_SRC_DIR_NAMES = listOf(TEST_SRC_DIRECTORY, GROOVY_TEST_SRC_DIRECTORY, KOTLIN_TEST_SRC_DIRECTORY, SCALA_TEST_SRC_DIRECTORY)

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

    @JvmField
    val DICTIONARY_WORDS = setOf(
        "creationtime",
        "modifiedtime",
        "argumenttype",
        "atomictype",
        "autocreate",
        "backoffice",
        "beanutils",
        "builddate",
        "cockpitng",
        "collectiontype",
        "columntype",
        "creationmode",
        "cronjobs",
        "defaultvalue",
        "dontoptimize",
        "elementtype",
        "extname",
        "hybris",
        "impex",
        "itemtype",
        "jalo",
        "jaloclass",
        "jaloonly",
        "jalosession",
        "jspc",
        "jstl",
        "maptypes",
        "metatype",
        "metatype",
        "nimda",
        "NOPMD",
        "partof",
        "pojos",
        "positiveshort",
        "postgresql",
        "propertytable",
        "releasedate",
        "returntype",
        "servicelayer",
        "solr",
        "solrconfig",
        "sqlserver",
        "taglibs",
        "typecode",
        "typegroup",
        "typesystem",
        "webroot",
        "ybackoffice",
        "ybootstrap"
    )

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

    @JvmStatic
    val KEY_FINALIZE_PROJECT_IMPORT: Key<Triple<HybrisProjectDescriptor, List<ModuleDescriptor>, Boolean>> = Key.create("hybrisProjectImportFinalize")
    val KEY_ANT_UPDATE_MAVEN_DEPENDENCIES = Key.create<Boolean>("notification_update_external-dependencies.xml")
    val KEY_LOGGER_IDENTIFIER_DATA_CONTEXT = DataKey.create<String>("sap.cx.logger.identifier")

    const val FXS_DUMMY_IDENTIFIER = CompletionUtilCore.DUMMY_IDENTIFIER_TRIMMED
    val FXS_SUPPORTED_ELEMENT_TYPES = setOf(
        FlexibleSearchTypes.TABLE_ALIAS_NAME,
        FlexibleSearchTypes.COLUMN_ALIAS_NAME
    )

    val IMPEX_FILE_NODE_TYPE = IFileElementType(ImpexLanguage)
    val FXS_FILE_NODE_TYPE = IFileElementType(FlexibleSearchLanguage)

    val CHARS_UPPERCASE_REGEX = "[A-Z]".toRegex()
    val CHARS_LOWERCASE_REGEX = "[a-z]".toRegex()
    val PGQ_RESERVED_KEYWORDS = setOf(
        PolyglotQueryTypes.AND,
        PolyglotQueryTypes.ASC,
        PolyglotQueryTypes.BY,
        PolyglotQueryTypes.DESC,
        PolyglotQueryTypes.GET,
        PolyglotQueryTypes.IS,
        PolyglotQueryTypes.NOT,
        PolyglotQueryTypes.NULL,
        PolyglotQueryTypes.OR,
        PolyglotQueryTypes.ORDER,
        PolyglotQueryTypes.WHERE
    )

    val FXS_RESERVED_KEYWORDS = setOf(
        FlexibleSearchTypes.ALL,
        FlexibleSearchTypes.AND,
        FlexibleSearchTypes.AS,
        FlexibleSearchTypes.ASC,
        FlexibleSearchTypes.BETWEEN,
        FlexibleSearchTypes.BY,
        FlexibleSearchTypes.CASE,
        FlexibleSearchTypes.CAST,
        FlexibleSearchTypes.DESC,
        FlexibleSearchTypes.DISTINCT,
        FlexibleSearchTypes.ELSE,
        FlexibleSearchTypes.END,
        FlexibleSearchTypes.EXISTS,
        FlexibleSearchTypes.FROM,
        FlexibleSearchTypes.FULL,
        FlexibleSearchTypes.GROUP,
        FlexibleSearchTypes.HAVING,
        FlexibleSearchTypes.IN,
        FlexibleSearchTypes.INNER,
        FlexibleSearchTypes.INTERVAL,
        FlexibleSearchTypes.IS,
        FlexibleSearchTypes.JOIN,
        FlexibleSearchTypes.LEFT,
        FlexibleSearchTypes.LIKE,
        FlexibleSearchTypes.LIMIT,
        FlexibleSearchTypes.NOT,
        FlexibleSearchTypes.NULL,
        FlexibleSearchTypes.OFFSET,
        FlexibleSearchTypes.ON,
        FlexibleSearchTypes.OR,
        FlexibleSearchTypes.ORDER,
        FlexibleSearchTypes.OUTER,
        FlexibleSearchTypes.RIGHT,
        FlexibleSearchTypes.SELECT,
        FlexibleSearchTypes.THEN,
        FlexibleSearchTypes.UNION,
        FlexibleSearchTypes.USING,
        FlexibleSearchTypes.WHEN,
        FlexibleSearchTypes.WHERE,
    )

    object Documentation {
        const val WIKI_ACL = "https://github.com/epam/sap-commerce-intellij-idea-plugin/wiki/Languages:-Access-Control-Lists"
    }
}
