package com.arslan.swissknife.actions

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

class Jirafolder : AnAction(){

    val path: Path  = Paths.get("${System.getProperty("user.home")}\\Workspace\\jira")
    val template: Path  = path.resolve("NewFileTemplate.xlsx")
    val templateAnalysis: Path  = path.resolve("AnalysisTemplate.xlsx")
    val templateNotes: Path  = path.resolve("NotesTemplate.txt")
    val templateSqls: Path  = path.resolve("SQL.sql")

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }
        val repository = GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)
        if (repository == null) {
            Messages.showErrorDialog(project, "No Git repository found in the project.", "Error");
            return;
        }

        val jiraNumber = repository.currentBranch?.name?.substringAfter("TREASPROD-")

        if (jiraNumber == null) {
            return
        }

        val jiraFolderPath = path.resolve(jiraNumber)
        if (!Files.exists(jiraFolderPath)) {
            Files.createDirectories(jiraFolderPath)
            Files.copy(template, jiraFolderPath.resolve("DevTest-${jiraNumber}.xlsx"))
            Files.copy(templateAnalysis, jiraFolderPath.resolve("Analysis-${jiraNumber}.xlsx"))
            Files.copy(templateNotes, jiraFolderPath.resolve("Notes-${jiraNumber}.txt"))
            Files.copy(templateSqls, jiraFolderPath.resolve("SQL-${jiraNumber}.sql"))
        }

        Desktop.getDesktop().open(jiraFolderPath.toFile())
    }
}