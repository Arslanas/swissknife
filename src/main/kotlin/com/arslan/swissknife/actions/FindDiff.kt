package com.arslan.swissknife.actions

import com.intellij.diff.DiffContentFactory
import com.intellij.diff.DiffManager
import com.intellij.diff.requests.SimpleDiffRequest
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent

class FindDiff : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        DiffDialog(project).show()
    }

    class DiffDialog(private val project: Project) : DialogWrapper(project, false) {

        private var a = ""
        private var b = ""

        init {
            title = "Compare Texts"
            init()
            peer.isModal = false
        }


        override fun getDimensionServiceKey(): String {
            return "FindDiff"
        }

        override fun createCenterPanel(): JComponent {
            return panel {
                row {
                    textArea().bindText({a}, {a = it}).align(Align.FILL).resizableColumn()
                    textArea().bindText({b}, {b = it}).align(Align.FILL).resizableColumn()
                }.resizableRow()
            }
        }

        override fun doOKAction() {
            super.doOKAction()

            val leftText = a
            val rightText = b

            val contentFactory = DiffContentFactory.getInstance()
            val leftContent = contentFactory.create(project, leftText)
            val rightContent = contentFactory.create(project, rightText)

            val request = SimpleDiffRequest(
                "Text Comparison",
                leftContent, rightContent,
                "Left", "Right"
            )

            DiffManager.getInstance().showDiff(project, request)
        }
    }

}