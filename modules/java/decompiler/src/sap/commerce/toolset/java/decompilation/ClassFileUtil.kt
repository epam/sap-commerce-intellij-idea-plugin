/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for IntelliJ IDEA.
 * Copyright (C) 2019-2026 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

@file:JvmName("ClassFileUtil")

package sap.commerce.toolset.java.decompilation

import org.jetbrains.org.objectweb.asm.ClassReader
import org.jetbrains.org.objectweb.asm.ClassVisitor
import org.jetbrains.org.objectweb.asm.Opcodes

private const val GROOVY_SOURCE_FILE_SUFFIX = ".groovy"

/**
 * Detects classes produced by the Groovy compiler via the `SourceFile` class attribute.
 *
 * Groovy-compiled classes (for example Spock specifications shipped inside OOTB `*server.jar` files) make the bundled
 * Fernflower decompiler explode both time- and memory-wise, while the produced Java sources are useless anyway.
 *
 * Malformed or truncated input is never considered a Groovy class.
 */
fun ByteArray.isGroovyCompiledClass(): Boolean = runCatching {
    var groovy = false

    ClassReader(this).accept(object : ClassVisitor(Opcodes.API_VERSION) {
        override fun visitSource(source: String?, debug: String?) {
            groovy = source?.endsWith(GROOVY_SOURCE_FILE_SUFFIX) == true
        }
    }, ClassReader.SKIP_CODE or ClassReader.SKIP_FRAMES)

    groovy
}.getOrDefault(false)
