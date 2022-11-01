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

    const val IMPEX_CATALOG_VERSION_ONLINE = "Online"
    const val IMPEX_CATALOG_VERSION_STAGED = "Staged"
    const val IMPEX_CONSOLE_TITLE = "Hybris Impex Console"
    const val IMPEX_MONITOR_CONSOLE_TITLE = "Hybris Monitor Console"
    const val IMPEX_FILE_EXTENSION = "impex"

    const val GROOVY_CONSOLE_TITLE = "Hybris Groovy Console"
    const val FLEXIBLE_SEARCH_CONSOLE_TITLE = "Hybris FS Console"
    const val SOLR_SEARCH_CONSOLE_TITLE = "Hybris Solr search"

    const val DOT_PROJECT = ".project"
    const val SETTINGS_GRADLE = "settings.gradle"
    const val BUILD_GRADLE = "build.gradle"
    const val LOCAL_EXTENSIONS_XML = "localextensions.xml"
    const val EXTENSION_INFO_XML = "extensioninfo.xml"
    const val EXTENSIONS_XML = "extensions.xml"

    const val HYBRIS_DATA_DIRECTORY = "data"
    const val HYBRIS_PLATFORM_CODE_SERVER_JAR_SUFFIX = "server.jar"

    const val EXTENSION_NAME_BACK_OFFICE = "backoffice"
    const val EXTENSION_NAME_CORE = "core"
    const val EXTENSION_NAME_CONFIG = "config"
    const val EXTENSION_NAME_HMC = "hmc"
    const val EXTENSION_NAME_HAC_MODULE = "hac"
    const val EXTENSION_NAME_PLATFORM = "platform"

    const val BACKOFFICE_MODULE_META_KEY_NAME = "backoffice-module"
    const val BACKOFFICE_MODULE_DIRECTORY = "backoffice"
    const val BACKOFFICE_LIBRARY_GROUP = "Backoffice Library"

    const val GEN_SRC_DIRECTORY = "gensrc"
    const val TEST_SRC_DIRECTORY = "testsrc"
    const val GROOVY_TEST_SRC_DIRECTORY = "groovytestsrc"
    const val SCALA_TEST_SRC_DIRECTORY = "scalatestsrc"

    const val HMC_MODULE_DIRECTORY = "hmc"

    const val HAC_MODULE_META_KEY_NAME = "hac-module"
    const val HAC_MODULE_DIRECTORY = "hac"
    const val HAC_WEBROOT_KEY = "hac.webroot"

    const val WEB_MODULE_DIRECTORY = "web"
    const val ADDON_SRC_DIRECTORY = "addonsrc"
    const val TEST_CLASSES_DIRECTORY = "testclasses"
    const val CLASSES_DIRECTORY = "classes"
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
    const val HYBRIS_PROJECT_SETTINGS_FILE_NAME = "hybrisProjectSettings.xml"
    const val HYBRIS_PROJECT_SETTINGS_COMPONENT_NAME = "HybrisProjectSettings"
    const val HYBRIS_INTEGRATION_SETTINGS_FILE_NAME = "hybrisIntegrationSettings.xml"
    const val HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_FILE_NAME = "hybrisDeveloperSpecificProjectSettings.xml"
    const val HYBRIS_DEVELOPER_SPECIFIC_PROJECT_SETTINGS_COMPONENT_NAME = "HybrisDeveloperSpecificProjectSettings"
    const val DEFAULT_DIRECTORY_NAME_FOR_IDEA_MODULE_FILES = "idea-module-files"
    const val PLUGIN_ID = "com.intellij.idea.plugin.sap.commerce"
    const val JREBEL_PLUGIN_ID = "JRebelPlugin"
    const val CONFIGURATOR_FACTORY_ID = "$PLUGIN_ID.hybrisConfiguratorFactory"

    const val NEW_IDEA_MODULE_FILE_EXTENSION = ".iml"
    const val HYBRIS_ITEMS_XML_FILE_ENDING = "-items.xml"
    const val HYBRIS_BEANS_XML_FILE_ENDING = "-beans.xml"
    const val HYBRIS_IMPEX_XML_FILE_ENDING = ".$IMPEX_FILE_EXTENSION"

    const val DESCRIPTOR_TYPE = "descriptorType"
    const val READ_ONLY = "importedAsReadOnly"
    const val DEBUG_PORT = "8000"

    const val TOMCAT_SSL_PORT_KEY = "tomcat.ssl.port"
    const val TOMCAT_HTTP_PORT_KEY = "tomcat.http.port"

    const val DEFAULT_TOMCAT_HTTP_PORT = "9001"
    const val DEFAULT_TOMCAT_SSL_PORT = "9002"
    const val DEFAULT_SOLR_TOMCAT_SSL_PORT = "8983"

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

    const val TS_ITEMS_VALIDATION_WARN = "hybris.ts.items.validation.warn"
    const val TS_ATOMIC_DEFAULT_EXTENDS = "java.lang.Object"
    const val TS_IMPLICIT_SUPER_CLASS_NAME = "GenericItem"
    const val TS_ATTRIBUTE_DEFAULT_META_TYPE = "AttributeDescriptor"
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
    val TS_TYPECODE_RANGE_B2BCOMMERCE = TS_TYPECODE_MIN_ALLOWED .. 10099
    val TS_TYPECODE_RANGE_COMMONS = 13200 .. 13299
    val TS_TYPECODE_RANGE_XPRINT = 24400 .. 24599
    val TS_TYPECODE_RANGE_PRINT = 23400 .. 23999
    val TS_TYPECODE_RANGE_PROCESSING = 32700 .. 32799

    const val SEARCH_SCOPE_Y_PREFIX = "[y]"
    const val SEARCH_SCOPE_GROUP_PREFIX = "group:"
    const val HYBRIS_DATA_DIR_ENV = "HYBRIS_DATA_DIR"
    const val IMPORT_OVERRIDE_FILENAME = "hybris4intellij.properties"
    const val GROUP_OVERRIDE_KEY = "group.override"
    const val BUILD_COMPILER_KEY = "build.compiler"
    const val ITEM_ROOT_CLASS = "de.hybris.platform.core.model.ItemModel"
    const val ENUM_ROOT_CLASS = "de.hybris.platform.core.HybrisEnumValue"
    const val MODEL_SUFFIX = "Model"
    const val SOURCE_ATTRIBUTE_NAME = "source"
    const val TARGET_ATTRIBUTE_NAME = "target"
    const val CODE_ATTRIBUTE_NAME = "code"
    const val NAME_ATTRIBUTE_NAME = "name"
    const val DICTIONARY_NAME = "hybris_integration"
    const val OPTIONAL_CONFIG_DIR_KEY = "hybris.optional.config.dir"
    const val DIALOG_TITLE = "copy.file.dialog."
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
    const val DEFAULT_JAVADOC_ROOT_URL = "https://download.hybris.com/api/%s/commercesuite"
    const val HYBRIS_6_0_PLUS_JAVADOC_ROOT_URL = "https://help.hybris.com/%s/api/commercesuite/index.html"

    private const val SRC_DIRECTORY = "src"
    private const val SCALA_SRC_DIRECTORY = "scalasrc"
    private const val WEB_XML_FILE_NAME = "web.xml"
    private const val WEB_INF_DIRECTORY = "WEB-INF"

    @JvmField val RESERVED_TYPE_CODES_FILE = FileUtilRt.toSystemDependentName("resources/core/unittest/reservedTypecodes.txt")
    @JvmField val HYBRIS_SERVER_SHELL_SCRIPT_NAME = FileUtilRt.toSystemDependentName("bin/platform/hybrisserver.sh")

    @JvmField val PLATFORM_MODULE_PREFIX = FileUtilRt.toSystemDependentName("/bin/platform/")
    @JvmField val PLATFORM_EXT_MODULE_PREFIX = FileUtilRt.toSystemDependentName("bin/platform/ext/")
    @JvmField val PLATFORM_OOTB_MODULE_PREFIX = FileUtilRt.toSystemDependentName("bin/ext-")
    @JvmField val PLATFORM_OOTB_MODULE_PREFIX_2019 = FileUtilRt.toSystemDependentName("bin/modules/")
    @JvmField val PLATFORM_DB_DRIVER = FileUtilRt.toSystemDependentName("lib/dbdriver")

    @JvmField val HYBRIS_OOTB_MODULE_PREFIX = FileUtilRt.toSystemDependentName("hybris/") + PLATFORM_OOTB_MODULE_PREFIX
    @JvmField val HYBRIS_OOTB_MODULE_PREFIX_2019 = FileUtilRt.toSystemDependentName("hybris/") + PLATFORM_OOTB_MODULE_PREFIX_2019

    @JvmField val EXCLUDE_TMP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tmp")
    @JvmField val EXCLUDE_TCSERVER_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tcServer")
    @JvmField val EXCLUDE_TOMCAT_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tomcat")
    @JvmField val EXCLUDE_TOMCAT_6_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/tomcat-6")
    @JvmField val EXCLUDE_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/lib")
    @JvmField val EXCLUDE_RESOURCES_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/resources")
    @JvmField val EXCLUDE_ECLIPSEBIN_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/eclipsebin")

    @JvmField val EXCLUDE_BOOTSTRAP_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/bootstrap")
    @JvmField val EXCLUDE_ANT_DIRECTORY = FileUtilRt.toSystemDependentName("/platform/apache-ant-")
    @JvmField val EXCLUDE_IDEA_MODULE_FILES_DIRECTORY = FileUtilRt.toSystemDependentName("/idea-module-files")
    @JvmField val EXCLUDE_LOG_DIRECTORY = FileUtilRt.toSystemDependentName("/log")
    @JvmField val EXCLUDE_DATA_DIRECTORY = FileUtilRt.toSystemDependentName("/data")
    @JvmField val EXCLUDE_SVN_DIRECTORY = FileUtilRt.toSystemDependentName("/.svn")
    @JvmField val EXCLUDE_GIT_DIRECTORY = FileUtilRt.toSystemDependentName("/.git")
    @JvmField val EXCLUDE_TEMP_DIRECTORY = FileUtilRt.toSystemDependentName("/temp")
    @JvmField val EXCLUDE_IDEA_DIRECTORY = FileUtilRt.toSystemDependentName("/.idea")
    @JvmField val EXCLUDE_MACOSX_DIRECTORY = FileUtilRt.toSystemDependentName("/__MACOSX")

    @JvmField val CUSTOM_MODULES_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("bin/custom")
    @JvmField val CONFIG_RELATIVE_PATH = FileUtilRt.toSystemDependentName("/../../$EXTENSION_NAME_CONFIG")
    @JvmField val ADVANCED_PROPERTIES = FileUtilRt.toSystemDependentName("$RESOURCES_DIRECTORY/advanced.properties")
    @JvmField val BUILD_NUMBER_FILE_PATH = FileUtilRt.toSystemDependentName("/bin/platform/build.number")

    @JvmField val WEB_ROOT_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/$WEB_ROOT_DIRECTORY")
    @JvmField val WEB_INF_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_ROOT_DIRECTORY_RELATIVE_PATH/$WEB_INF_DIRECTORY")
    @JvmField val WEB_XML_DIRECTORY_RELATIVE_PATH = FileUtilRt.toSystemDependentName("$WEB_INF_DIRECTORY_RELATIVE_PATH/$WEB_XML_FILE_NAME")
    @JvmField val WEB_INF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("webroot/WEB-INF/lib")
    @JvmField val WEB_INF_CLASSES_DIRECTORY = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/webroot/WEB-INF/classes")
    @JvmField val WEB_SRC_DIRECTORY = FileUtilRt.toSystemDependentName("web/src")
    @JvmField val WEB_WEBINF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("$WEB_MODULE_DIRECTORY/$WEB_INF_LIB_DIRECTORY")

    @JvmField val COMMONWEB_WEBINF_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("$COMMON_WEB_MODULE_DIRECTORY/$WEB_INF_LIB_DIRECTORY")
    @JvmField val HMC_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("hmc/bin")
    @JvmField val DOC_SOURCES_JAR_DIRECTORY = FileUtilRt.toSystemDependentName("doc/sources")

    @JvmField val PL_BOOTSTRAP_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("bootstrap/bin")
    @JvmField val PL_BOOTSTRAP_GEN_SRC_DIRECTORY = FileUtilRt.toSystemDependentName("bootstrap/gensrc")
    @JvmField val PL_TOMCAT_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat/lib")
    @JvmField val PL_TOMCAT_BIN_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat/bin")
    @JvmField val PL_TOMCAT_6_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat-6/lib")
    @JvmField val PL_TOMCAT_6_BIN_DIRECTORY = FileUtilRt.toSystemDependentName("tomcat-6/bin")
    @JvmField val JAVA_COMPILER_OUTPUT_PATH = FileUtilRt.toSystemDependentName("/classes")

    @JvmField val HAC_WEB_INF_CLASSES = FileUtilRt.toSystemDependentName("/bin/platform/ext/hac/web/webroot/WEB-INF/classes")

    @JvmField val BACKOFFICE_LIB_DIRECTORY = FileUtilRt.toSystemDependentName("backoffice/bin")
    @JvmField val BACKOFFICE_JAR_DIRECTORY = FileUtilRt.toSystemDependentName("resources/backoffice")
    @JvmField val BACKOFFICE_WEB_INF_LIB = FileUtilRt.toSystemDependentName("/bin/ext-backoffice/backoffice/web/webroot/WEB-INF/lib")
    @JvmField val BACKOFFICE_WEB_INF_LIB_2019 = FileUtilRt.toSystemDependentName("/bin/modules/backoffice-framework/backoffice/web/webroot/WEB-INF/lib")
    @JvmField val BACKOFFICE_WEB_INF_CLASSES = FileUtilRt.toSystemDependentName("/bin/ext-backoffice/backoffice/web/webroot/WEB-INF/classes")
    @JvmField val BACKOFFICE_WEB_INF_CLASSES_2019 = FileUtilRt.toSystemDependentName("/bin/modules/backoffice-framework/backoffice/web/webroot/WEB-INF/classes")

    @JvmField val QUERY_STORAGE_FOLDER_PATH = EXCLUDE_IDEA_DIRECTORY + File.separator + "consolestorage"
    @JvmField val SRC_DIR_NAMES = listOf(SRC_DIRECTORY, SCALA_SRC_DIRECTORY)
    @JvmField val TEST_SRC_DIR_NAMES = listOf(TEST_SRC_DIRECTORY, GROOVY_TEST_SRC_DIRECTORY, SCALA_TEST_SRC_DIRECTORY)

    @JvmField val LOCAL_GROUP_OVERRIDE_COMMENTS = """
        In this file you can override default module grouping and add additional ant parameters.
        Add a property group.override and value group name.
        If you use subgroups use / as a separator. For example group.override=mygroup/mysubgroup
        """.trimIndent()
    @JvmField val GLOBAL_GROUP_OVERRIDE_COMMENTS = """
        In this file you can override default module group for your extensions.
        Add a property <modulename>.group.override and group name as a value.
        If you use subgroups use / as a separator. For example myextension.group.override=mygroup/mysubgroup.
        It is recommended to keep custom hybris modules within custom group i.e. custom/subgroup, so that the generated search scopes would function correctly.

        Use ANT_OPTS to override ant properties. Current default value is
        ANT_OPTS=-Xmx512m -Dfile.encoding=UTF-8
        """.trimIndent()
    @JvmField val DICTIONARY_WORDS = setOf(
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
    @JvmField val UNDO_REDO_EDITOR_ACTIONS = arrayOf("Undo", "Redo")

    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/ActionsBundle.properties
    // See ideaIU-LATEST-EAP-SNAPSHOT/lib/resources_en.jar!/messages/EditorBundle.properties
    @JvmField val TYPING_EDITOR_ACTIONS = arrayOf(
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