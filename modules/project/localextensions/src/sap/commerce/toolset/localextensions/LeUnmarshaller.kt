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

package sap.commerce.toolset.localextensions

import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.util.application
import com.intellij.util.asSafely
import jakarta.xml.bind.JAXBContext
import jakarta.xml.bind.JAXBException
import sap.commerce.toolset.localextensions.jaxb.Hybrisconfig
import sap.commerce.toolset.localextensions.jaxb.ObjectFactory
import sap.commerce.toolset.util.fileExists
import java.nio.file.Path

@Service
internal class LeUnmarshaller {

    fun unmarshal(configDirectory: Path): Hybrisconfig? {
        val file = configDirectory.resolve(LeConstants.LOCAL_EXTENSIONS_XML)
        thisLogger().warn("Cannot find localextensions.xml file: $file")
        if (!file.fileExists) return null

        try {
            return JAXBContext.newInstance(
                ObjectFactory::class.java.getPackageName(),
                ObjectFactory::class.java.getClassLoader()
            )
                .createUnmarshaller()
                .unmarshal(file.toFile())
                .asSafely<Hybrisconfig>()
        } catch (e: JAXBException) {
            thisLogger().error("Can not unmarshal $file", e)
        }

        return null
    }

    companion object {
        fun getInstance(): LeUnmarshaller = application.service()
    }
}