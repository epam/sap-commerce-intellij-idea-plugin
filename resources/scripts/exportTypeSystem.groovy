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

import de.hybris.platform.jalo.JaloSession
import de.hybris.platform.jalo.JaloSystemException
import de.hybris.platform.util.JaloTools
import de.hybris.platform.util.zip.SafeZipEntry
import org.znerd.xmlenc.XMLOutputter

import java.util.zip.ZipOutputStream

protected static void doExport(JaloSession jaloSession, ZipOutputStream zos, XMLOutputter xout, String extensionName) {
    try {
        String packageName = extensionName.replace('.', '_')
        zos.putNextEntry(new SafeZipEntry(packageName + "-items.xml"))
        JaloTools.exportSystem(jaloSession, xout, extensionName)
        xout.getWriter().flush()
        zos.closeEntry()
    } catch (IOException e) {
        throw new JaloSystemException(e)
    }
}

def baos = new ByteArrayOutputStream()
def zos = new ZipOutputStream(baos)
def bufferedwriter = new BufferedWriter(new OutputStreamWriter(zos, "UTF-8"))
def xout = new XMLOutputter(bufferedwriter, "UTF-8")
def jaloSession = JaloSession.currentSession

jaloSession.extensionManager.extensions
        .collect { it.name }
        .toSet()
        .forEach {
            xout.reset(bufferedwriter, "UTF-8")
            doExport(jaloSession, zos, xout, it)
            xout.reset()
        }

bufferedwriter.close()
zos.close()

Base64.encoder.encodeToString(baos.toByteArray())