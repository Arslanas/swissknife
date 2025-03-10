package com.arslan.swissknife.actions

import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class InsertCodeActionGroup : ActionGroup(){


    override fun getChildren(event: AnActionEvent?): Array<AnAction> {
        val settings = service<CapgSettings>()
        return settings.getQueryMap().keys.sorted().map {
            InsertCodeAction(it)
        }.toTypedArray()
    }

    class InsertCodeAction(private val id:String) : AnAction(id) {

        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return

            val settings = service<CapgSettings>()
            val query = settings.getQuery(id)!!

            insertTextAtCaret(editor, project, query)
        }

        fun insertTextAtCaret(editor: Editor, project: Project, text: String) {
            val document: Document = editor.document
            val caretModel: CaretModel = editor.caretModel
            val offset: Int = caretModel.offset

            WriteCommandAction.runWriteCommandAction(project) {
                document.insertString(offset, text)
                caretModel.moveToOffset(offset + text.length) // Move caret after inserted text
            }
        }
    }



}