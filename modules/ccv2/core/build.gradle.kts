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

import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

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

fun properties(key: String) = providers.gradleProperty(key)

plugins {
    id("org.jetbrains.intellij.platform.module")
    alias(libs.plugins.kotlin) // Kotlin support
}

sourceSets {
    main {
        java.srcDirs("src", "gen")
        resources.srcDirs("resources")
    }
    test {
        java.srcDirs("tests")
    }
}

idea {
    module {
        generatedSourceDirs.add(file("gen"))
    }
}

// OpenAPI - Gradle plugin
// https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator-gradle-plugin
// OpenAPI - Kotlin generator
// https://openapi-generator.tech/docs/generators/kotlin/
val ccv2OpenApiSpecs = listOf(
    Triple("ccv1OpenApiGenerate", "commerce-cloud-management-api-v1.yaml", "sap.commerce.toolset.ccv1"),
    Triple("ccv2OpenApiGenerate", "commerce-cloud-management-api-v2.yaml", "sap.commerce.toolset.ccv2"),
)
val ccv2OpenApiTasks = ccv2OpenApiSpecs.mapIndexed { index, (taskName, schema, packagePrefix) ->
    tasks.register<GenerateTask>(taskName) {
        group = "openapi tools"
        generatorName.set("kotlin")

        inputSpec.set("$projectDir/resources/specs/$schema")
        outputDir.set("$projectDir/gen")

        // Custom template required to enable request-specific headers for Authentication
        // https://openapi-generator.tech/docs/templating
        // https://github.com/OpenAPITools/openapi-generator/tree/master/modules/openapi-generator/src/main/resources/kotlin-client/libraries/jvm-okhttp
        templateDir.set("$projectDir/resources/openapi/templates")

        apiPackage.set("$packagePrefix.api")
        packageName.set("$packagePrefix.invoker")
        modelPackage.set("$packagePrefix.model")

        cleanupOutput.set(index == 0)
        skipOperationExample.set(true)
        generateApiDocumentation.set(false)
        generateApiTests.set(false)
        generateModelTests.set(false)

        globalProperties.set(
            mapOf(
                "modelDocs" to "false",
            )
        )
        configOptions.set(
            mapOf(
                "useSettingsGradle" to "false",
                "omitGradlePluginVersions" to "true",
                "omitGradleWrapper" to "true",
                "useCoroutines" to "true",
                "sourceFolder" to "",
            )
        )

        if (index > 0) {
            val previousTaskName = ccv2OpenApiSpecs[index - 1].first
            dependsOn(previousTaskName)
        }
    }
}

tasks {
    compileJava {
        dependsOn(ccv2OpenApiTasks)
    }

    compileKotlin {
        dependsOn(ccv2OpenApiTasks)
    }
}

dependencies {
    implementation(libs.bundles.openapi)
    implementation(project(":shared-core"))
    implementation(project(":project-core"))
    implementation(project(":project-import-core"))

    intellijPlatform {
        intellijIdeaUltimate(properties("intellij.version")) {
            useInstaller = false
        }

        bundledPlugins(
            "com.intellij.modules.json",
        )
    }
}
