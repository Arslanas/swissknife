package com.arslan.swissknife.util

import com.arslan.swissknife.dto.SourceBranch
import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.commands.Git
import git4idea.commands.GitCommand
import git4idea.commands.GitLineHandler
import git4idea.commands.GitLineHandlerListener
import git4idea.repo.GitRepository
import kotlinx.coroutines.*


class CommonUtil {


    companion object {
        val consolePrinter = GitLineHandlerListener { line, _ -> println(line) }

        fun getProject(e: AnActionEvent): Project?{
            return e.project ?: run{
                Messages.showErrorDialog("No project found", "Error")
                return null
            }
        }

        fun showError(project: Project, errorMessage: String) {
            CoroutineScope(Dispatchers.EDT).launch {
                Messages.showErrorDialog(
                    project,
                    errorMessage,
                    "Error"
                );
            }
        }

        fun getBranchOptions(): List<SourceBranch> {
            val settings = service<CapgSettings>()
            val rawString = settings.get(SettingsEnum.BRANCH_OPTIONS)?: return emptyList()
            return rawString.split(',').map{
                val pair = it.split(':')
                SourceBranch(pair[0], pair[1].lowercase() == "true")
            }
        }

        fun getSetting(setting: SettingsEnum): String {
            val settings = service<CapgSettings>()
            return settings.get(setting)?: return ""
        }

        fun selectSourceBranch(project: Project, branchOptions: List<SourceBranch>): Int?{
            val optionId = Messages.showDialog(project, "Choose source branch",
                "",  branchOptions.map { it.name }.toTypedArray(), -1, Messages.getInformationIcon())
            if (optionId < 0) return null
            return optionId
        }

        fun getLastCommitMessage(project: Project, repository: GitRepository): String{
            val output = mutableListOf<String>()

            val gitLineHandler = GitLineHandler(project, repository.root, GitCommand.LOG)
            gitLineHandler.addParameters("--oneline", "-n", "1")

            gitLineHandler.addLineListener({ line, _ -> output.add(line)})

            val git = Git.getInstance()

            git.runCommand(gitLineHandler)

            return extractCommitMessage(output.firstOrNull() ?: "")
        }

        private fun extractCommitMessage(commitLine: String): String {
            val regex = """^\S+\s+""".toRegex()
            val matchResult = regex.find(commitLine)
            val firstPart = matchResult?.groups?.get(0)?.value?.trim() ?: ""
            return commitLine.substringAfter(firstPart).trim()
        }
    }
}