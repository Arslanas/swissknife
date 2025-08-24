package com.arslan.swissknife.actions

import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.util.CommonUtil
import com.arslan.swissknife.util.CommonUtil.Companion.getBranchOptions
import com.arslan.swissknife.util.CommonUtil.Companion.getSetting
import com.arslan.swissknife.util.CommonUtil.Companion.selectSourceBranch
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager

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

        val branchOptions = getBranchOptions()

        val optionId = selectSourceBranch(project, branchOptions) ?: return

        val sourceBranch = branchOptions[optionId]
        val suffix = if (sourceBranch.needSuffix) "-${sourceBranch.name}" else ""
        val jiraProjectName = getSetting(SettingsEnum.JIRA_PROJECT_NAME)
        val branchName =  "feature/${jiraProjectName}-${input.trim()}${suffix}"

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
                    runOperation(git, repository, remote, branchName, indicator, project, sourceBranch.name)
                }
            }
        )
    }

    private fun runOperation(
        git: Git,
        repository: GitRepository,
        remote: GitRemote,
        newBranchName: String,
        indicator: ProgressIndicator,
        project: Project,
        sourceBranch: String
    ) {
        indicator.text = "Fetching latest changes and updating ${sourceBranch} branch"
        if (sourceBranch.equals(repository.currentBranch?.name)){
            println("Already on ${sourceBranch} branch, so just merge the remote one")
            git.merge(repository, "origin/${sourceBranch}", null, CommonUtil.consolePrinter)
        } else {
            val fetchResult = git.fetch(repository, remote, listOf(CommonUtil.consolePrinter), "${sourceBranch}:${sourceBranch}")
            if (!fetchResult.success()) {
                CommonUtil.showError(project, "Failed to fetch the latest changes: ${fetchResult.getErrorOutputAsJoinedString()}")
                return;
            }
        }


        indicator.text = "Checkout new branch ${newBranchName}"
        val checkoutNewBranch = git.checkout(repository, sourceBranch, newBranchName, false, false, CommonUtil.consolePrinter )
        if (!checkoutNewBranch.success()) {
            CommonUtil.showError(project, "Failed to checkout new branch ${newBranchName}: ${checkoutNewBranch.getErrorOutputAsJoinedString()}")
            return;
        }

        repository.update()
    }
}