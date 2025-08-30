package com.arslan.swissknife.actions.git

import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.mandatory
import com.arslan.swissknife.runBackgroundTask
import com.arslan.swissknife.util.CommonUtil
import com.arslan.swissknife.util.GitUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateReleaseBranch : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = CommonUtil.getProject(e) ?: return

        val repository = GitUtil.getRepo(project) ?: return
        val git = Git.getInstance()

        project.runBackgroundTask("Creating Branch from release branch", false) { indicator ->
            val output = repository.branches.remoteBranches
                .map { it.nameForRemoteOperations }
                .filter { it.contains("release") }
                .sortedDescending()

            if (output.isEmpty()) return@runBackgroundTask

            val releaseBranches: List<String> = output


            CoroutineScope(Dispatchers.EDT).launch {
                indicator.text = "Waiting for user to select release branch"
                val selectedReleaseBranch = Messages.showEditableChooseDialog(
                    "Select a release branch from which to create a release feature branch",
                    "Release Branches",
                    Messages.getQuestionIcon(),
                    releaseBranches.toTypedArray(),
                    releaseBranches.get(0),
                    null
                ).mandatory(project, "Release branch number cannot be empty.") ?: return@launch


                indicator.text = "Waiting for user to input Jira number"
                val jiraNumber =
                    Messages.showInputDialog(project, "Input Jira number", "Number", Messages.getQuestionIcon())
                        .mandatory(project, "Branch number cannot be empty.") ?: return@launch

                val rawReleaseBranchName = selectedReleaseBranch.substringAfter("release/")
                val projectName = CommonUtil.getSetting(SettingsEnum.JIRA_PROJECT_NAME)
                val resultBranch = "feature/$projectName-$jiraNumber-$rawReleaseBranchName"
                val response = Messages.showYesNoDialog(
                    project,
                    "Are you sure you want to create and switch to branch:\n\n$resultBranch ?",
                    "Confirm Branch Creation",
                    Messages.getQuestionIcon()
                )
                val confirmed = response == Messages.YES

                if (confirmed) {
                    val checkoutResult = withContext(Dispatchers.IO)  {
                        indicator.text = "Checkout branch $resultBranch"

                        val checkoutResult = GitUtil.checkout(git, repository, "origin/$selectedReleaseBranch", resultBranch)

                        val unsetUpstream = GitLineHandler(project, repository.root, GitCommand.BRANCH)
                        unsetUpstream.addParameters("--unset-upstream")
                        unsetUpstream.addLineListener(CommonUtil.consolePrinter)
                        val unsetUpstreamResult = git.runCommand(unsetUpstream)

                        repository.update()

                        listOf(checkoutResult, unsetUpstreamResult)
                    }

                    val error = checkoutResult.firstOrNull{ !it.success() }
                    if (error != null){
                        Messages.showErrorDialog(
                            project, "Checkout failed from $selectedReleaseBranch to $resultBranch :\n${error.errorOutputAsJoinedString}",
                            "Checkout Failed"
                        )
                    }
                }
            }

        }

    }
}