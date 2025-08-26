package com.arslan.swissknife.actions.git

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

@Deprecated("This function practically is not used")
class UpdateMasterBranch : AnAction(){

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

        val git = Git.getInstance()

        val remote = repository.remotes.stream()
            .findFirst()
            .orElseThrow { Exception("Remote not found") }



        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Update master branch", false
        ){
                override fun run(indicator: ProgressIndicator) {
                    runOperation(git, repository, remote, indicator, project)
                }
            }
        )
    }

    private fun runOperation(
        git: Git,
        repository: GitRepository,
        remote: GitRemote,
        indicator: ProgressIndicator,
        project: Project
    ) {
        val handleError: (String) -> Unit = { errorMessage ->
            CoroutineScope(Dispatchers.EDT).launch {
                Messages.showErrorDialog(
                    project,
                    errorMessage,
                    "Error"
                );
            }
        }

        val consolePrinter = GitLineHandlerListener { line, _ -> println(line) }

        indicator.text = "Fetching latest changes and updating master branch"
        val fetchResult = git.fetch(repository, remote, listOf(consolePrinter), "master:master")
        if (!fetchResult.success()) {
            handleError("Failed to fetch the latest changes: ${fetchResult.getErrorOutputAsJoinedString()}")
            return;
        }

        repository.update()
    }
}