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

package sap.commerce.toolset.project

import com.intellij.openapi.util.Key
import sap.commerce.toolset.project.descriptor.HybrisProjectDescriptor
import java.nio.file.Path
import kotlin.io.path.Path

object ProjectConstants {

    @JvmStatic
    val KEY_FINALIZE_PROJECT_IMPORT: Key<HybrisProjectDescriptor> = Key.create("hybrisProjectImportFinalize")

    object Directory {
        const val BOOTSTRAP = "bootstrap"

        const val TMP = "tmp"
        const val TC_SERVER = "tcServer"
        const val TOMCAT = "tomcat"
        const val TOMCAT_6 = "tomcat-6"
        const val ANT = "apache-ant"
        const val DATA = "data"
        const val LOG = "log"
        const val LIB = "lib"
        const val BIN = "bin"
        const val TEMP = "temp"
        const val GRADLE = ".gradle"
        const val SVN = ".svn"
        const val GIT = ".git"
        const val HG = ".hg"
        const val GITHUB = ".github"
        const val IDEA = ".idea"
        const val MACO_SX = "__MACOSX"
        const val IDEA_MODULE_FILES = "idea-module-files"

        const val CLASSES = "classes"
        const val TEST_CLASSES = "testclasses"
        const val MODEL_CLASSES = "modelclasses"

        const val SRC = "src"
        const val GEN_SRC = "gensrc"
        const val ADDON_SRC = "addonsrc"
        const val COMMON_WEB_SRC = "commonwebsrc"
        const val GROOVY_SRC = "groovysrc"
        const val KOTLIN_SRC = "kotlinsrc"
        const val SCALA_SRC = "scalasrc"

        const val TEST_SRC = "testsrc"
        const val GROOVY_TEST_SRC = "groovytestsrc"
        const val KOTLIN_TEST_SRC = "kotlintestsrc"
        const val SCALA_TEST_SRC = "scalatestsrc"

        const val RESOURCES = "resources"
        const val ECLIPSE_BIN = "eclipsebin"
        const val NODE_MODULES = "node_modules"
        const val JS_STOREFRONT = "js-storefront"

        const val LICENCE = "licence"
        const val BOWER_COMPONENTS = "bower_components"
        const val JS_TARGET = "jsTarget"

        const val EXT = "ext"
        const val HYBRIS = "hybris"
        const val ACCELERATOR_ADDON = "acceleratoraddon"
        const val WEB_ROOT = "webroot"

        val PATH_BOOTSTRAP: Path = Path("platform", BOOTSTRAP)

        @JvmField
        val SRC_DIR_NAMES = listOf(SRC, GROOVY_SRC, KOTLIN_SRC, SCALA_SRC)

        @JvmField
        val ALL_SRC_DIR_NAMES = listOf(TEST_SRC, SRC, GROOVY_SRC, KOTLIN_SRC, SCALA_SRC)

        @JvmField
        val TEST_SRC_DIR_NAMES = listOf(TEST_SRC, GROOVY_TEST_SRC, KOTLIN_TEST_SRC, SCALA_TEST_SRC)
    }

    object Extension {
        const val BACK_OFFICE = "backoffice"
        const val CORE = "core"
        const val CONFIG = "config"
        const val HMC = "hmc"
        const val HAC = "hac"
        const val PLATFORM = "platform"
        const val PLATFORM_SERVICES = "platformservices"
        const val ADDON_SUPPORT = "addonsupport"
        const val KOTLIN_NATURE = "kotlinnature"
        const val COMMON_WEB = "commonweb"
        const val WEB = "web"

        const val ADVANCED_SAVED_QUERY = "advancedsavedquery"
        const val CATALOG = "catalog"
        const val COMMENTS = "comments"
        const val COMMONS = "commons"
        const val DELIVERY_ZONE = "deliveryzone"
        const val EUROPE1 = "europe1"
        const val IMPEX = "impex"
        const val MAINTENANCE_WEB = "maintenanceweb"
        const val MEDIA_WEB = "mediaweb"
        const val OAUTH2 = "oauth2"
        const val PAYMENT_STANDARD = "paymentstandard"
        const val PROCESSING = "processing"
        const val SCRIPTING = "scripting"
        const val TEST_WEB = "testweb"
        const val VALIDATION = "validation"
        const val WORKFLOW = "workflow"
    }

    object File {
        const val LOCAL_PROPERTIES = "local.properties"
        const val PROJECT_PROPERTIES = "project.properties"
        const val PLATFORM_HOME_PROPERTIES = "platformhome.properties"
        const val ENV_PROPERTIES = "env.properties"
        const val ADVANCED_PROPERTIES = "advanced.properties"
    }

    val PLATFORM_EXTENSION_NAMES = setOf(
        Extension.ADVANCED_SAVED_QUERY,
        Extension.CATALOG,
        Extension.COMMENTS,
        Extension.COMMONS,
        Extension.CORE,
        Extension.DELIVERY_ZONE,
        Extension.EUROPE1,
        Extension.HAC,
        Extension.IMPEX,
        Extension.MAINTENANCE_WEB,
        Extension.MEDIA_WEB,
        Extension.OAUTH2,
        Extension.PAYMENT_STANDARD,
        Extension.PLATFORM_SERVICES,
        Extension.PROCESSING,
        Extension.SCRIPTING,
        Extension.TEST_WEB,
        Extension.VALIDATION,
        Extension.WORKFLOW,
    )

}