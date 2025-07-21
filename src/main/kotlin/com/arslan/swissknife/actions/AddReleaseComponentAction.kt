package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.releaseComponent.SelectReleaseComponentDialog
import com.arslan.swissknife.util.CommonUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddReleaseComponentAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        e.project ?: return

        // check if the file is yml and if not then do not show the action otherwise show
//        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("No project found", "Error")
            return
        }
        SelectReleaseComponentDialog(project, mapOf("TFF" to "3.54.6")).show()
    }
}