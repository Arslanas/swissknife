package com.arslan.swissknife.actions

import com.arslan.swissknife.util.SwissknifeUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
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

class CreateReleaseBranch : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val repository = GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)
        val git = Git.getInstance()

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(
                project, "Creating Branch from release branch", false
            ) {
                override fun run(indicator: ProgressIndicator) {
                    val output = repository.branches.remoteBranches
                        .map { it.nameForRemoteOperations }
                        .filter { it.contains("release") }
                        .sortedDescending()

                    if (output.isEmpty()) return

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
                        )

                        if (selectedReleaseBranch == null) {
                            return@launch
                        }


                        indicator.text = "Waiting for user to input Jira number"
                        val jiraNumber =
                            Messages.showInputDialog(project, "Input Jira number", "Number", Messages.getQuestionIcon())
                        if (jiraNumber == null) return@launch

                        if (jiraNumber.trim().isEmpty() == true) {
                            Messages.showErrorDialog(project, "Branch number cannot be empty.", "Error");
                            return@launch
                        }

                        val rawReleaseBranchName = selectedReleaseBranch.substringAfter("release/")
                        val resultBranch = "feature/TREASPROD-$jiraNumber-$rawReleaseBranchName"
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
                                val checkoutResult = git.checkout(
                                    repository,
                                    "origin/$selectedReleaseBranch",
                                    resultBranch,
                                    false,
                                    false,
                                    SwissknifeUtil.consolePrinter
                                )

                                val unsetUpstream = GitLineHandler(project, repository.root, GitCommand.BRANCH)
                                unsetUpstream.addParameters("--unset-upstream")
                                unsetUpstream.addLineListener(SwissknifeUtil.consolePrinter)
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
        )

    }
}