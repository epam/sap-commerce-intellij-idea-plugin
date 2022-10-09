package com.intellij.idea.plugin.hybris.tools.remote.console

import com.intellij.execution.console.LanguageConsoleImpl
import com.intellij.idea.plugin.hybris.settings.HybrisRemoteConnectionSettings
import com.intellij.idea.plugin.hybris.tools.remote.console.preprocess.HybrisConsolePreProcessor
import com.intellij.idea.plugin.hybris.tools.remote.http.impex.HybrisHttpResult
import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import java.util.concurrent.TimeUnit
import javax.swing.Icon

/**
 * @author Nosov Aleksandr <nosovae.dev@gmail.com>
 */
abstract class HybrisConsole(project: Project, title: String, language: Language) : LanguageConsoleImpl(project, title, language) {

    init {
        this.printDefaultText()
    }

    abstract fun execute(query: String): HybrisHttpResult

    abstract fun title(): String

    abstract fun tip(): String

    open fun icon(): Icon? = null

    open fun preProcessors(): List<HybrisConsolePreProcessor> = listOf()

    open fun printDefaultText() {
        setInputText("")
    }

    open fun connectionType(): HybrisRemoteConnectionSettings.Type {
        return HybrisRemoteConnectionSettings.Type.Hybris
    }

    open fun onSelection() {
        //NOP
    }

}


data class TimeOption(val name: String, val value: Int, val unit: TimeUnit)

data class CatalogVersionOption(val name: String, val value: String)