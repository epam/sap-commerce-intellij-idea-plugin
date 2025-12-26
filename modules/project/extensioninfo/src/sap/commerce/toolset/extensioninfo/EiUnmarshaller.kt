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

package sap.commerce.toolset.extensioninfo

import com.intellij.openapi.diagnostic.thisLogger
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import sap.commerce.toolset.HybrisConstants
import sap.commerce.toolset.exceptions.HybrisConfigurationException
import sap.commerce.toolset.extensioninfo.jaxb.ExtensionInfo
import sap.commerce.toolset.extensioninfo.jaxb.ObjectFactory
import java.io.File

object EiUnmarshaller {

    @Throws(HybrisConfigurationException::class)
    fun unmarshall(moduleRootDirectory: File): ExtensionInfo {
        val extensionFile = File(moduleRootDirectory, HybrisConstants.EXTENSION_INFO_XML)
        if (!extensionFile.isFile || !extensionFile.exists()) {
            throw HybrisConfigurationException("Can not find extensioninfo using path: $extensionFile")
        }

        val extensionInfo = unmarshalExtensionInfo(extensionFile)
        if (null == extensionInfo.extension || extensionInfo.extension.name.isBlank()) {
            throw HybrisConfigurationException("Can not unmarshall extensioninfo using path: $extensionFile")
        }
        return extensionInfo
    }

    @Throws(HybrisConfigurationException::class)
    private fun unmarshalExtensionInfo(extensionFile: File): ExtensionInfo = try {
        JAXBContext.newInstance(
            ObjectFactory::class.java.packageName,
            ObjectFactory::class.java.classLoader
        )
            .createUnmarshaller()
            .unmarshal(extensionFile) as ExtensionInfo
    } catch (e: JAXBException) {
        thisLogger().error("Can not unmarshal ${extensionFile.absolutePath}", e)
        throw HybrisConfigurationException("Can not unmarshal $extensionFile")
    }
}