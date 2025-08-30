package com.arslan.swissknife.actions.git

import com.arslan.swissknife.dto.SourceBranch
import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.requireSuccess
import com.arslan.swissknife.runBackgroundTask
import com.arslan.swissknife.util.CommonUtil
import com.arslan.swissknife.util.CommonUtil.Companion.getBranchOptions
import com.arslan.swissknife.util.CommonUtil.Companion.getSetting
import com.arslan.swissknife.util.CommonUtil.Companion.selectSourceBranch
import com.arslan.swissknife.util.GitUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git

class CreateFeatureBranch : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return

        val input =
            Messages.showInputDialog(project, "Enter Jira number", "Number", Messages.getQuestionIcon())

        if (input.isNullOrBlank()) {
            Messages.showErrorDialog(project, "Branch number cannot be empty.", "Error");
            return
        }

        val branchOptions = getBranchOptions()

        val optionId = selectSourceBranch(project, branchOptions) ?: return

        val sourceBranch = branchOptions[optionId]
        val newBranchName = getNewBranchName(sourceBranch, input)

        val repository = GitUtil.getRepo(project) ?: run {
            Messages.showErrorDialog(project, "No Git repository found in the project.", "Error");
            return;
        }

        val remote = GitUtil.getRemoteBranch(repository) ?: run {
            Messages.showErrorDialog(project, "No Remote branch found in the project.", "Error");
            return;
        }

        val git = Git.getInstance()

        project.runBackgroundTask("Creating Branch $newBranchName", false) { indicator ->
            val sourceBranchName = sourceBranch.name

            if (sourceBranchName == repository.currentBranch?.name){
                indicator.text = "Merging remote $sourceBranchName into local"
                git.merge(repository, "origin/$sourceBranchName", null, CommonUtil.consolePrinter)
            } else {
                indicator.text = "Fetching latest changes and updating $sourceBranchName branch"

                GitUtil.fetch(git, repository, remote, "$sourceBranchName:$sourceBranchName")
                    .requireSuccess(project, "Failed to fetch the latest changes") ?: return@runBackgroundTask
            }

            indicator.text = "Checkout new branch $newBranchName"
            GitUtil.checkout(git, repository, sourceBranchName, newBranchName)
                .requireSuccess(project, "Failed to checkout new branch $newBranchName") ?: return@runBackgroundTask

            repository.update()
        }
    }

    private fun getNewBranchName(
        sourceBranch: SourceBranch,
        input: String
    ): String {
        val suffix = if (sourceBranch.needSuffix) {
            val rawSourceBranchName = sourceBranch.name
                .replace("feature/", "")
                .replace("release/", "")
            "-${rawSourceBranchName}"
        } else ""
        val jiraProjectName = getSetting(SettingsEnum.JIRA_PROJECT_NAME)
        val branchName = "feature/${jiraProjectName}-${input.trim()}${suffix}"
        return branchName
    }
}