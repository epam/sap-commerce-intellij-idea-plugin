/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2020 EPAM Systems <hybrisideaplugin@epam.com>
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

const val sourceVersion="17"
const val kotlinApiVersion="1.7"


// https://www.jetbrains.com/intellij-repository/releases
// https://www.jetbrains.com/intellij-repository/snapshots
// https://www.jetbrains.org/intellij/sdk/docs/reference_guide/intellij_artifacts.html
// https://data.services.jetbrains.com/products?fields=code,name,releases.downloads,releases.version,releases.build,releases.type&code=IIC,IIU

const val intellijJvmArgs="-ea -Xms512m -Xmx3G -XX:MaxMetaspaceSize=400m"
const val intellijUpdateSinceUntilBuild=true

const val pluginName_ = "SAP-Commerce-Developers-Toolset"
const val pluginVersion = "2022.3"
const val pluginSinceBuild = "223.7126.7"
const val pluginUntilBuild = "223.*"

// Plugin Verifier integration -> https://github.com/JetBrains/intellij-plugin-verifier
// https://github.com/JetBrains/gradle-intellij-plugin#plugin-verifier-dsl
// See https://jb.gg/intellij-platform-builds-list for available build versions
const val pluginVerifierIdeVersions = "IIU-221.5080.40"

const val platformType = "IU"
const val platformVersion = "223-EAP-SNAPSHOT"
const val platformDownloadSources = true
// Plugin Dependencies -> https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_dependencies.html
// Example: platformPlugins = com.intellij.java, com.jetbrains.php:203.4449.22
const val platformPlugins = "ant, Spring, uml, junit, JavaEE, maven, eclipse, gradle, properties, Groovy, java-i18n, java, gradle-java, PsiViewer:223-SNAPSHOT, copyright"
