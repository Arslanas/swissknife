package com.arslan.swissknife.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction

class ReplacementAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)

        val project = e.getRequiredData(CommonDataKeys.PROJECT)
        val document = editor.document
        editor.caretModel.allCarets.forEach {
            WriteCommandAction.runWriteCommandAction(project, {
                document.replaceString(it.selectionStart, it.selectionEnd, "editorBasics")
            })
        }
    }

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        val project = e.project

        e.presentation.isEnabledAndVisible = project != null && editor.selectionModel.hasSelection()
    }
}
