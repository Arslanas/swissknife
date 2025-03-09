package com.arslan.swissknife.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import java.util.*

class SearchAction : AnAction() {
    private val log: Logger = Logger.getInstance(SearchAction::class.java)
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.PSI_FILE)
        val lang = e.getData(CommonDataKeys.PSI_FILE)!!.language
        val lowercase = lang.displayName.lowercase(Locale.getDefault())
        val languageTag = "+[$lowercase]"

        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.caretModel
        val selectedText = caretModel.currentCaret.selectedText

        val query = selectedText!!.replace(' ', '+') + languageTag
        println("Tryin to loookup 3 in StackOverflow : $query")
//        BrowserUtil.browse("https://stackoverflow.com/search?q=$query")
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val caretModel = editor.caretModel
        e.presentation.isEnabledAndVisible = caretModel.currentCaret.hasSelection()
    }
}
