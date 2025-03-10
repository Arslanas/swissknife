package com.arslan.swissknife.actions

import com.arslan.swissknife.util.SwissknifeUtil
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

class CreateFeatureBranch : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val input =
            Messages.showInputDialog(project, "Enter Jira number", "Number", Messages.getQuestionIcon())

        if (input == null) {
            return
        }

        if (input.trim().isEmpty() == true) {
            Messages.showErrorDialog(project, "Branch number cannot be empty.", "Error");
            return
        }



        val branchName = "feature/TREASPROD-${input.trim()}"

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
            object : Task.Backgroundable(project, "Creating Branch $branchName", false
        ){
                override fun run(indicator: ProgressIndicator) {
                    runOperation(git, repository, remote, branchName, indicator, project)
                }
            }
        )
    }

    private fun runOperation(
        git: Git,
        repository: GitRepository,
        remote: GitRemote,
        branchName: String,
        indicator: ProgressIndicator,
        project: Project
    ) {
        indicator.text = "Fetching latest changes and updating master branch"
        if ("master".equals(repository.currentBranch?.name)){
            println("Already on master branch, so just merge the remote one")
            git.merge(repository, "origin/master", null, SwissknifeUtil.consolePrinter)
        } else {
            val fetchResult = git.fetch(repository, remote, listOf(SwissknifeUtil.consolePrinter), "master:master")
            if (!fetchResult.success()) {
                SwissknifeUtil.showError(project, "Failed to fetch the latest changes: ${fetchResult.getErrorOutputAsJoinedString()}")
                return;
            }
        }


        indicator.text = "Checkout new branch ${branchName}"
        val checkoutNewBranch = git.checkout(repository, "master", branchName, false, false, SwissknifeUtil.consolePrinter )
        if (!checkoutNewBranch.success()) {
            SwissknifeUtil.showError(project, "Failed to checkout new branch ${branchName}: ${checkoutNewBranch.getErrorOutputAsJoinedString()}")
            return;
        }

        repository.update()
    }
}