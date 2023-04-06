/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019 EPAM Systems <hybrisideaplugin@epam.com>
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

import com.intellij.openapi.util.io.FileUtilRt
import java.io.File

/**
 * Created 10:30 PM 07 June 2015
 *
 * @author Alexander Bartash <AlexanderBartash></AlexanderBartash>@gmail.com>
 */
object HybrisConstants {

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
            "cx-commerce-crm-integrations",
            "media-telco"
    )

    const val NOTIFICATION_GROUP_HYBRIS = "[y] SAP Commerce"

    const val IDEA_EDITION_ULTIMATE = "Ultimate Edition"
    const val UPDATE_TIMESTAMP_PROPERTY = "sap.commerce.update.timestamp"

    const val IMPEX_CONFIG_PREFIX = "\$config"
    const val IMPEX_CONFIG_COMPLETE_PREFIX = "$IMPEX_CONFIG_PREFIX-"
    const val IMPEX_CATALOG_VERSION_ONLINE = "Online"
    const val IMPEX_CATALOG_VERSION_STAGED = "Staged"
    const val IMPEX_CONSOLE_TITLE = "[y] Impex Console"
    const val IMPEX_MONITOR_CONSOLE_TITLE = "[y] Monitor Console"
    const val IMPEX = "Impex"
    const val IMPEX_FILE_EXTENSION = "impex"

    const val GROOVY_CONSOLE_TITLE = "[y] Groovy Console"
    const val FLEXIBLE_SEARCH_CONSOLE_TITLE = "[y] FS Console"
    const val SOLR_SEARCH_CONSOLE_TITLE = "[y] Solr search"

    const val BUSINESS_PROCESS_ROOT_TAG = "process"

    const val DOT_PROJECT = ".project"
    const val SETTINGS_GRADLE = "settings.gradle"
    const val BUILD_GRADLE = "build.gradle"
    const val LOCAL_EXTENSIONS_XML = "localextensions.xml"
    const val EXTENSION_INFO_XML = "extensioninfo.xml"
    const val EXTENSIONS_XML = "extensions.xml"
    const val COCKPIT_NG_CONFIG_XML = "-config.xml"
    const val COCKPIT_NG_WIDGETS_XML = "widgets.xml"
    const val COCKPIT_NG_DEFINITION_XML = "definition.xml"

    const val HYBRIS_DATA_DIRECTORY = "data"
    const val HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX = "server.jar"

    const val EXTENSION_NAME_BACK_OFFICE = "backoffice"
    const val EXTENSION_NAME_CORE = "core"
    const val EXTENSION_NAME_CONFIG = "config"
    const val EXTENSION_NAME_HMC = "hmc"
    const val EXTENSION_NAME_HAC = "hac"
    const val EXTENSION_NAME_PLATFORM = "platform"
    const val EXTENSION_NAME_ADDONSUPPORT = "addonsupport"

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
    const val GROOVY_TEST_SRC_DIRECTORY = "groovytestsrc"
    const val SCALA_TEST_SRC_DIRECTORY = "scalatestsrc"

    const val HMC_MODULE_DIRECTORY = "hmc"
    const val HAC_MODULE_DIRECTORY = "hac"

    const val WEB_MODULE_DIRECTORY = "web"
    const val ADDON_SRC_DIRECTORY = "addonsrc"
    const val TEST_CLASSES_DIRECTORY = "testclasses"
    const val CLASSES_DIRECTORY = "classes"
    const val JAR_MODELS = "models.jar"
    const val SETTINGS_DIRECTORY = ".settings"
    const val EXTERNAL_TOOL_BUILDERS_DIRECTORY = ".externalToolBuilders"
    const val WEB_ROOT_DIRECTORY = "webroot"
    const val ACCELERATOR_ADDON_DIRECTORY = "acceleratoraddon"

    const val COMMON_WEB_SRC_DIRECTORY = "commonwebsrc"
    const val COMMON_WEB_MODULE_DIRECTORY = "commonweb"

    const val PLATFORM_HOME_PLACEHOLDER = "\${platformhome}"
    const val PLATFORM_EXTENSIONS_DIRECTORY_NAME = "ext"
    const val PLATFORM_BOOTSTRAP_DIRECTORY = "bootstrap"
    const val PLATFORM_MODEL_CLASSES_DIRECTORY = "modelclasses"
    const val PLATFORM_TOMCAT_6_DIRECTORY = "tomcat-6"
    const val PLATFORM_TOMCAT_DIRECTORY = "tomcat"
    const val PLATFORM_LIBRARY_GROUP = "Platform Bootstrap"

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
    const val JREBEL_PLUGIN_ID = "JRebelPlugin"
    const val CONFIGURATOR_FACTORY_ID = "$PLUGIN_ID.hybrisConfiguratorFactory"

    const val NEW_IDEA_MODULE_FILE_EXTENSION = ".iml"
    const val HYBRIS_ITEMS_XML_FILE_ENDING = "-items.xml"
    const val HYBRIS_BEANS_XML_FILE_ENDING = "-beans.xml"
    const val HYBRIS_IMPEX_XML_FILE_ENDING = ".$IMPEX_FILE_EXTENSION"

    const val DEBUG_PORT = "8000"

    const val PROPERTY_HAC_WEBROOT = "hac.webroot"
    const val PROPERTY_TOMCAT_SSL_PORT = "tomcat.ssl.port"
    const val PROPERTY_TOMCAT_HTTP_PORT = "tomcat.http.port"
    const val PROPERTY_SOLR_DEFAULT_PORT = "solrserver.instances.default.port"
    const val PROPERTY_SOLR_DEFAULT_USER = "solrserver.instances.default.user"
    const val PROPERTY_SOLR_DEFAULT_PASSWORD = "solrserver.instances.default.password"
    const val PROPERTY_DEPLOYMENT_TABLENAME_MAXLENGTH = "deployment.tablename.maxlength"
    const val PROPERTY_BUILD_COMPILER = "build.compiler"
    const val PROPERTY_OPTIONAL_CONFIG_DIR = "hybris.optional.config.dir"

    const val DEFAULT_DEPLOYMENT_TABLENAME_MAXLENGTH = 24

    const val DEFAULT_HOST_URL = "localhost"
    const val DEFAULT_SSL_PROTOCOL = "TLSv1.2"

    const val HTTP_PROTOCOL = "http://"
    const val HTTPS_PROTOCOL = "https://"

    const val URL_PORT_DELIMITER = ":"
    const val TOMCAT_JAVA_DEBUG_OPTIONS = "tomcat.debugjavaoptions"
    const val X_RUNJDWP_TRANSPORT = "-Xrunjdwp:transport="
    const val ADDRESS = "address="

    const val ANT_ENCODING = "-Dfile.encoding=UTF-8"
    const val ANT_HYBRIS_CONFIG_DIR = "-J-DHYBRIS_CONFIG_DIR="
    const val ANT_XMX = "-Xmx"
    const val ANT_PLATFORM_HOME = "PLATFORM_HOME"
    const val ANT_OPTS = "ANT_OPTS"
    const val ANT_HOME = "ANT_HOME"
    const val ANT_COMPILING_XML = "resources/ant/compiling.xml"
    const val ANT_LIB_DIR = "resources/ant/lib"
    const val ANT_BUILD_XML = "build.xml"
    const val ANT_HEAP_SIZE_MB = 512
    const val ANT_STACK_SIZE_MB = 128

    const val TS_TYPE_OBJECT = "java.lang.Object"
    const val TS_TYPE_ITEM = "Item"
    const val TS_TYPE_GENERIC_ITEM = "GenericItem"
    const val TS_TYPE_LOCALIZABLE_ITEM = "LocalizableItem"
    const val TS_TYPE_EXTENSIBLE_ITEM = "ExtensibleItem"
    const val TS_TYPE_CRON_JOB = "CronJob"
    const val TS_TYPE_CATALOG_VERSION = "CatalogVersion"
    const val TS_TYPE_LINK = "Link"
    const val TS_META_TYPE_ATTRIBUTE_DESCRIPTOR = "AttributeDescriptor"
    const val TS_JAVA_LANG_PREFIX = "java.lang."
    const val TS_ATTRIBUTE_LOCALIZED_PREFIX = "localized:"
    const val TS_UNIQUE_KEY_ATTRIBUTE_QUALIFIER = "uniqueKeyAttributeQualifier"
    const val TS_CATALOG_ITEM_TYPE = "catalogItemType"
    const val TS_CATALOG_VERSION_ATTRIBUTE_QUALIFIER = "catalogVersionAttributeQualifier"
    const val TS_CATALOG_SYNC_DEFAULT_ROOT_TYPE = "catalog.sync.default.root.type"
    const val TS_CATALOG_SYNC_DEFAULT_ROOT_TYPE_ORDER = "catalog.sync.default.root.type.order"
    const val TS_PRIMITIVE_BYTE = "byte"
    const val TS_PRIMITIVE_SHORT = "short"
    const val TS_PRIMITIVE_INT = "int"
    const val TS_PRIMITIVE_LONG = "long"
    const val TS_PRIMITIVE_FLOAT = "float"
    const val TS_PRIMITIVE_DOUBLE = "double"
    const val TS_PRIMITIVE_CHAR = "char"
    const val TS_PRIMITIVE_BOOLEAN = "boolean"
    val TS_PRIMITIVE_TYPES = setOf(TS_PRIMITIVE_BYTE, TS_PRIMITIVE_SHORT, TS_PRIMITIVE_INT, TS_PRIMITIVE_LONG, TS_PRIMITIVE_FLOAT, TS_PRIMITIVE_DOUBLE, TS_PRIMITIVE_CHAR, TS_PRIMITIVE_BOOLEAN)

    val TS_TYPECODE_MIN_ALLOWED = 10000
    val TS_TYPECODE_RANGE_B2BCOMMERCE = TS_TYPECODE_MIN_ALLOWED..10099
    val TS_TYPECODE_RANGE_COMMONS = 13200..13299
    val TS_TYPECODE_RANGE_XPRINT = 24400..24599
    val TS_TYPECODE_RANGE_PRINT = 23400..23999
    val TS_TYPECODE_RANGE_PROCESSING = 32700..32799

    val BP_NAVIGABLE_ELEMENTS = setOf("action", "end", "wait", "join", "notify", "split", "scriptAction", "choice")

    const val HYBRIS = "[y]"
    const val DEBUG_MODEL_RENDERER_PREFIX = HYBRIS
    const val SEARCH_SCOPE_Y_PREFIX = HYBRIS
    const val SEARCH_SCOPE_GROUP_PREFIX = "group:"
    const val HYBRIS_DATA_DIR_ENV = "HYBRIS_DATA_DIR"
    const val IMPORT_OVERRIDE_FILENAME = "hybris4intellij.properties"
    const val GROUP_OVERRIDE_KEY = "group.override"

    const val CLASS_ITEM_ROOT = "de.hybris.platform.core.model.ItemModel"
    const val CLASS_ENUM_ROOT = "de.hybris.platform.core.HybrisEnumValue"
    const val CLASS_ENUM_NAME = "HybrisEnumValue"
    const val CLASS_INTERCEPTOR_MAPPING = "de.hybris.platform.servicelayer.interceptor.impl.InterceptorMapping"
    const val CLASS_ANNOTATION_ACCESSOR = "de.hybris.bootstrap.annotations.Accessor"
    const val CLASS_CONFIG_IMPORT_PROCESSOR = "de.hybris.platform.commerceservices.impex.impl.ConfigPropertyImportProcessor"
    const val CLASS_CONVERTER = "de.hybris.platform.servicelayer.dto.converter.Converter"
    const val CLASS_POPULATOR = "de.hybris.platform.converters.Populator"
    const val CLASS_IMPEX_PROCESSOR = "de.hybris.platform.impex.jalo.imp.ImportProcessor"
    const val CLASS_IMPEX_TRANSLATOR = "de.hybris.platform.impex.jalo.translators.AbstractValueTranslator"
    const val CLASS_IMPEX_CELL_DECORATOR = "de.hybris.platform.util.CSVCellDecorator"

    const val MODEL_SUFFIX = "Model"
    const val TYPECODE_FIELD_NAME = "_TYPECODE"
    const val SOURCE_ATTRIBUTE_NAME = "source"
    const val TARGET_ATTRIBUTE_NAME = "target"
    const val CODE_ATTRIBUTE_NAME = "code"
    const val NAME_ATTRIBUTE_NAME = "name"
    const val DICTIONARY_NAME = "hybris_integration"
    const val DIALOG_TITLE = "hybris.copy.file.dialog."
    const val FLEXIBLE_SEARCH_FILE_EXTENSION = "fxs"

    const val LIB_DIRECTORY = "lib"
    const val BIN_DIRECTORY = "bin"
    const val RESOURCES_DIRECTORY = "resources"
    const val LOCAL_PROPERTIES = "local.properties"
    const val PROJECT_PROPERTIES = "project.properties"
    const val APPLICATION_CONTEXT_SPRING_FILES = "application-context"
    const val ADDITIONAL_WEB_SPRING_CONFIG_FILES = "additionalWebSpringConfigs"
    const val GLOBAL_CONTEXT_SPRING_FILES = "global-context"
    const val HYBRIS_CONFIG_DIR_KEY = "HYBRIS_CONFIG_DIR"
    const val HYBRIS_API_VERSION_KEY = "version.api"
    const val HYBRIS_VERSION_KEY = "version"
    const val JAVADOC_FALLBACK_URL = "https://help.sap.com/docs/SAP_COMMERCE/c5613bd3cc9942efb74d017b40eb0892/179bbc9b35274d7ca784e46b3beb40b2.html"
    const val JAVADOC_URL = "https://help.sap.com/doc/9fef7037b3304324b8891e84f19f2bf3/%s"

    const val QUOTE_LENGTH = 2
    // see: de.hybris.bootstrap.config.PlatformConfig -> readMaxDepthAttribute(..)
    const val DEFAULT_EXTENSIONS_PATH_DEPTH = 10

    const val COCKPIT_NG_NAMESPACE_KEY = "COCKPIT_NG_NAMESPACE"
    const val COCKPIT_NG_INITIALIZE_CONTEXT_TYPE = "ctx.TYPE_CODE"
    const val COCKPIT_NG_WIDGET_ID_STUB = "STUB_"

    private const val SRC_DIRECTORY = "src"
    private const val SCALA_SRC_DIRECTORY = "scalasrc"
    private const val WEB_XML_FILE_NAME = "web.xml"
    private const val WEB_INF_DIRECTORY = "WEB-INF"

    @JvmField
    val IMPEX_MODIFIER_BOOLEAN_VALUES = setOf("true", "false")
    @JvmField
    val IMPEX_MODIFIER_MODE_VALUES = setOf("append", "remove")

    @JvmField
    val DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES = FileUtilRt.toSystemDependentName("/.idea/idea-modules")

    @JvmField
    val FLEXIBLE_SEARCH_KEYWORDS = hashSetOf("SELECT", "FROM", "WHERE", "ORDER", "LEFT", "JOIN", "ON", "BY", "ASC", "DESC")
    @JvmField
    val RESERVED_TYPE_CODES_FILE = FileUtilRt.toSystemDependentName("resources/core/unittest/reservedTypecodes.txt")
    @JvmField
    val HYBRIS_SERVER_SHELL_SCRIPT_NAME = FileUtilRt.toSystemDependentName("bin/platform/hybrisserver.sh")

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
    val HYBRIS_OOTB_MODULE_PREFIX = FileUtilRt.toSystemDependentName("hybris/$PLATFORM_OOTB_MODULE_PREFIX")
    @JvmField
    val HYBRIS_OOTB_MODULE_PREFIX_2019 = FileUtilRt.toSystemDependentName("hybris/$PLATFORM_OOTB_MODULE_PREFIX_2019")

    @JvmField
    val EXCLUDE_TMP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tmp")
    @JvmField
    val EXCLUDE_TCSERVER_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tcServer")
    @JvmField
    val EXCLUDE_TOMCAT_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/$PLATFORM_TOMCAT_DIRECTORY")
    @JvmField
    val EXCLUDE_TOMCAT_6_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/$PLATFORM_TOMCAT_6_DIRECTORY")
    @JvmField
    val EXCLUDE_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/lib")
    @JvmField
    val EXCLUDE_RESOURCES_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/resources")
    @JvmField
    val EXCLUDE_ECLIPSEBIN_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/$ECLIPSE_BIN_DIRECTORY")

    @JvmField
    val EXCLUDE_BOOTSTRAP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/$PLATFORM_BOOTSTRAP_DIRECTORY")
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
    val EXCLUDE_TEMP_DIRECTORY = FileUtilRt.toSystemDependentName("/temp")
    @JvmField
    val EXCLUDE_IDEA_DIRECTORY = FileUtilRt.toSystemDependentName("/.idea")
    @JvmField
    val EXCLUDE_MACOSX_DIRECTORY = FileUtilRt.toSystemDependentName("/__MACOSX")

    @JvmField
    val CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("bin/custom")
    @JvmField
    val CONFIG_RELATIVE_PATH = FileUtilRt.toSystemDependentName("/../../$EXTENSION_NAME_CONFIG")
    @JvmField
    val ADVANCED_PROPERTIES = FileUtilRt.toSystemDependentName("$RESOURCES_DIRECTORY/advanced.properties")
    @JvmField
    val BUILD_NUMBER_FILE_PATH = FileUtilRt.toSystemDependentName("/bin/platform/build.number")

    @JvmField
    val WEB_ROOT_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/$WEB_ROOT_DIRECTORY")
    @JvmField
    val WEB_INF_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_ROOT_DIRECTORY_RELATIVE_PATH/$WEB_INF_DIRECTORY")
    @JvmField
    val WEB_XML_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_INF_DIRECTORY_RELATIVE_PATH/$WEB_XML_FILE_NAME")
    @JvmField
    val WEB_INF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("webroot/WEB-INF/lib")
    @JvmField
    val WEB_INF_CLASSES_DIRECTORY = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/webroot/WEB-INF/classes")
    @JvmField
    val WEB_SRC_DIRECTORY = FileUtilRt.toSystemDependentName("web/src")
    @JvmField
    val WEB_WEBINF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/$WEB_INF_LIB_DIRECTORY")

    @JvmField
    val COMMONWEB_WEBINF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("$COMMON_WEB_MODULE_DIRECTORY/$WEB_INF_LIB_DIRECTORY")
    @JvmField
    val HMC_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("hmc/$BIN_DIRECTORY")
    @JvmField
    val DOC_SOURCES_JAR_DIRECTORY = FileUtilRt.toSystemDependentName("doc/sources")

    @JvmField
    val PL_BOOTSTRAP_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("bootstrap/$BIN_DIRECTORY")
    @JvmField
    val PL_BOOTSTRAP_GEN_SRC_DIRECTORY = FileUtilRt.toSystemDependentName("bootstrap/$GEN_SRC_DIRECTORY")
    @JvmField
    val PL_TOMCAT_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat/lib")
    @JvmField
    val PL_TOMCAT_BIN_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat/$BIN_DIRECTORY")
    @JvmField
    val PL_TOMCAT_6_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat-6/lib")
    @JvmField
    val PL_TOMCAT_6_BIN_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat-6/$BIN_DIRECTORY")
    @JvmField
    val JAVA_COMPILER_OUTPUT_PATH = FileUtilRt.toSystemDependentName("/classes")

    @JvmField
    val HAC_WEB_INF_CLASSES = FileUtilRt.toSystemDependentName("/bin/platform/ext/hac/web/webroot/WEB-INF/classes")

    @JvmField
    val BACKOFFICE_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("backoffice/$BIN_DIRECTORY")
    @JvmField
    val BACKOFFICE_JAR_DIRECTORY = FileUtilRt.toSystemDependentName("resources/backoffice")
    @JvmField
    val BACKOFFICE_WEB_INF_LIB = FileUtilRt.toSystemDependentName("/bin/ext-backoffice/backoffice/web/webroot/WEB-INF/lib")
    @JvmField
    val BACKOFFICE_WEB_INF_LIB_2019 = FileUtilRt.toSystemDependentName("/bin/modules/backoffice-framework/backoffice/web/webroot/WEB-INF/lib")
    @JvmField
    val BACKOFFICE_WEB_INF_CLASSES = FileUtilRt.toSystemDependentName("/bin/ext-backoffice/backoffice/web/webroot/WEB-INF/classes")
    @JvmField
    val BACKOFFICE_WEB_INF_CLASSES_2019 = FileUtilRt.toSystemDependentName("/bin/modules/backoffice-framework/backoffice/web/webroot/WEB-INF/classes")

    @JvmField
    val QUERY_STORAGE_FOLDER_PATH = EXCLUDE_IDEA_DIRECTORY + File.separator + "consolestorage"
    @JvmField
    val SRC_DIR_NAMES = listOf(SRC_DIRECTORY, SCALA_SRC_DIRECTORY)
    @JvmField
    val TEST_SRC_DIR_NAMES = listOf(TEST_SRC_DIRECTORY, GROOVY_TEST_SRC_DIRECTORY, SCALA_TEST_SRC_DIRECTORY)

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
}