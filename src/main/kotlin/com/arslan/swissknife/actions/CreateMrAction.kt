package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.CreateMrDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

class CreateMrAction : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val dialog = CreateMrDialog(project)
        if (!dialog.showAndGet()) {
            return
        }

        val title = dialog.mrTitle

        Messages.showInfoMessage(title, "Create Mr Action")
    }
}