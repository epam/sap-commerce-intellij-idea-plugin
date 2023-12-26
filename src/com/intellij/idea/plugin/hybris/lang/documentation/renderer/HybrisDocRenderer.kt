/*
 * This file is part of "SAP Commerce Developers Toolset" plugin for Intellij IDEA.
 * Copyright (C) 2019-2023 EPAM Systems <hybrisideaplugin@epam.com> and contributors
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

package com.intellij.idea.plugin.hybris.lang.documentation.renderer

import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.util.text.HtmlChunk

open class HybrisDocRenderer {

    private val entries = mutableListOf<String>()

    fun title(prefix: String, name: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            add(DocumentationMarkup.INFORMATION_ICON.toString())
            add(prefix.replace(" ", "&nbsp;"))
            add("&nbsp;")
            add(DocumentationMarkup.GRAYED_ELEMENT.addText("::").toString())
            add("&nbsp;")
            add(HtmlChunk.span().bold().addText(name).toString())
            add(DocumentationMarkup.CONTENT_END)
            hr()
        }
    }

    fun subHeader(vararg texts: String) {
        textsWithSeparator(*texts)
    }

    fun allowedValues(vararg texts: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            add("Allowed Values")
            texts
                .filter { it.isNotBlank() }
                .forEach { add("<p>$it</p>") }
            hr()
            add(DocumentationMarkup.CONTENT_END)
        }
    }

    fun booleanAllowedValues(defaultValue: Boolean) {
        allowedValues(
            "true / false",
            "Default: ${if (defaultValue) "true" else "false"}"
        )
    }

    fun texts(vararg texts: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            texts
                .filter { it.isNotBlank() }
                .forEach { add("<p>$it</p>") }
            add(DocumentationMarkup.CONTENT_END)
        }
    }

    private fun textsWithSeparator(vararg texts: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            texts
                .filter { it.isNotBlank() }
                .forEach { add("<p>$it</p>") }
            add(DocumentationMarkup.CONTENT_END)
            hr()
        }
    }

    fun contentsWithSeparator(vararg contents: String) {
        with(entries) {
            contents
                .filter { it.isNotBlank() }
                .forEach { add(it) }
            hr()
        }
    }

    fun contents(vararg contents: String) {
        with(entries) {
            contents
                .filter { it.isNotBlank() }
                .forEach { add(it) }
        }
    }

    fun list(vararg texts: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            add("<ul>")
            texts
                .filter { it.isNotBlank() }
                .forEach { add("<li>$it</li>") }
            add("</ul>")
            add(DocumentationMarkup.CONTENT_END)
        }
    }

    fun tip(vararg texts: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            add("<p><strong>Tip</strong></p>")
            texts
                .filter { it.isNotBlank() }
                .forEach { add("<p>$it</p>") }
            add(DocumentationMarkup.CONTENT_END)
        }
    }

    fun example(text: String) {
        with(entries) {
            add(DocumentationMarkup.CONTENT_START)
            add("<p><strong>Example</strong></p>")
            add("<pre><code>$text</code></pre>")
            add(DocumentationMarkup.CONTENT_END)
        }
    }

    fun externalLink(name: String, url: String) {
        with(entries) {
            add(HtmlChunk.link(url, DocumentationMarkup.EXTERNAL_LINK_ICON.addText(name)).toString())
            hr()
        }
    }

    private fun hr() {
        entries.add("<hr>")
    }

    fun build() = toString()

    override fun toString() = entries.joinToString("")
}

fun hybrisDoc(initializer: HybrisDocRenderer.() -> Unit) = HybrisDocRenderer().apply(initializer)
