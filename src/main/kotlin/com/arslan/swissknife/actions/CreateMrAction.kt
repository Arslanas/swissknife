package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.CreateMrDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.commands.GitLineHandlerListener
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.*
import java.awt.Desktop
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

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

        val title = dialog.title

        Messages.showInfoMessage(title, "Create Mr Action")
    }
}