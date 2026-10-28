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
package sap.commerce.toolset.java.decompilation

import org.jetbrains.org.objectweb.asm.ClassWriter
import org.jetbrains.org.objectweb.asm.Opcodes
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ClassFileUtilTest {

    @Test
    fun `class compiled from a groovy source file is detected as groovy`() {
        assertTrue(classFile(sourceFile = "CartLikeTypeInfoSpec.groovy").isGroovyCompiledClass())
    }

    @Test
    fun `class compiled from a java source file is not groovy`() {
        assertFalse(classFile(sourceFile = "CartLikeTypeInfo.java").isGroovyCompiledClass())
    }

    @Test
    fun `class without a source file attribute is not groovy`() {
        assertFalse(classFile(sourceFile = null).isGroovyCompiledClass())
    }

    @Test
    fun `bytes without the class file magic are not groovy`() {
        assertFalse("not a class file".toByteArray().isGroovyCompiledClass())
    }

    @Test
    fun `truncated class file is not groovy`() {
        val bytes = classFile(sourceFile = "Spec.groovy")

        assertFalse(bytes.copyOf(bytes.size / 2).isGroovyCompiledClass())
    }

    @Test
    fun `real kotlin compiled class is not groovy`() {
        val bytes = JarDecompileResult::class.java.getResourceAsStream("JarDecompileResult.class")!!.use { it.readAllBytes() }

        assertFalse(bytes.isGroovyCompiledClass())
    }

    private fun classFile(sourceFile: String?): ByteArray = ClassWriter(0).run {
        visit(Opcodes.V17, Opcodes.ACC_PUBLIC, "de/hybris/platform/Sample", null, "java/lang/Object", null)
        visitSource(sourceFile, null)
        visitEnd()
        toByteArray()
    }
}
