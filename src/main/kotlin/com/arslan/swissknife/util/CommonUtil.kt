package com.arslan.swissknife.util

import com.arslan.swissknife.util.Constants.Companion.BRANCH_OPTIONS
import com.intellij.openapi.application.EDT
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

        fun showError(project: Project, errorMessage: String) {
            CoroutineScope(Dispatchers.EDT).launch {
                Messages.showErrorDialog(
                    project,
                    errorMessage,
                    "Error"
                );
            }
        }

        fun selectSourceBranch(project: Project): Int?{
            val optionId = Messages.showDialog(project, "Choose source branch",
                "",  BRANCH_OPTIONS.map { it.name }.toTypedArray(), 0, Messages.getInformationIcon())
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