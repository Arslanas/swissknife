package com.arslan.swissknife.actions

import com.arslan.swissknife.intentions.GoToEntityByNameIntention
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys

class GoToEntityByNameAction : AnAction("Go to Entity by Name") {

    val entityByNameIntention = GoToEntityByNameIntention()

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val offset = editor.caretModel.offset
        val word = getWordAtOffset(document.text, offset) ?: run {
            HintManager.getInstance().showErrorHint(editor, "No word at caret.")
            return
        }

        entityByNameIntention.invoke(project, editor, word)
    }

    private fun getWordAtOffset(text: String, offset: Int): String? {
        if (offset < 0 || offset >= text.length) return null
        val regex = "\\w+".toRegex()
        return regex.findAll(text).firstOrNull { it.range.contains(offset) }?.value
    }
}
