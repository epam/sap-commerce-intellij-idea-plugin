package com.intellij.idea.plugin.hybris.system.manifest.jsonSchema.providers

import com.intellij.idea.plugin.hybris.common.HybrisConstants
import com.intellij.idea.plugin.hybris.settings.HybrisProjectSettingsComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.jetbrains.jsonSchema.extension.JsonSchemaFileProvider
import com.jetbrains.jsonSchema.extension.JsonSchemaProviderFactory
import com.jetbrains.jsonSchema.extension.SchemaType

class ManifestJavascriptStorefrontJsonSchemaFileProvider(val project: Project) : JsonSchemaFileProvider {
    override fun isAvailable(file: VirtualFile) = HybrisConstants.CCV2_MANIFEST_NAME == file.name
            && HybrisConstants.CCV2_JS_STOREFRONT_NAME == file.parent.name
            && HybrisProjectSettingsComponent.getInstance(project).isHybrisProject()

    override fun getName() = "Javascript Storefront Manifest"
    override fun getSchemaFile() = JsonSchemaProviderFactory.getResourceFile(javaClass, "/schemas/manifest-js-storefront.schema.json")
    override fun getSchemaType() = SchemaType.embeddedSchema

    companion object {
        fun instance(project: Project): JsonSchemaFileProvider = project.getService(ManifestJavascriptStorefrontJsonSchemaFileProvider::class.java)
    }
}