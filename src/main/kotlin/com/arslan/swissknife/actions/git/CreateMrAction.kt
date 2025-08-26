package com.arslan.swissknife.actions.git

import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.state.CapgSettings
import com.arslan.swissknife.ui.CreateMrDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Deprecated("This function practically is not used")
class CreateMrAction : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val repository = GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)

        val currentBranch = repository.currentBranch!!.name

        val releaseBranches = repository.branches.remoteBranches
            .map { it.nameForRemoteOperations }
            .filter { it.contains("release") }
            .sortedDescending()

        val dialog = CreateMrDialog(project, releaseBranches, currentBranch)
        if (!dialog.showAndGet()) {
            return
        }

        // TODO : automate title, take it from Jira
        val title = dialog.mrTitle
        val targetBranch = dialog.targetBranch

        val response = Messages.showYesNoDialog(
            project,
            """
                Confirm MR details :
                Source branch : origin/$currentBranch
                Target branch : origin/$targetBranch
                Title : $title
            """.trimIndent(),
            "Confirm Release Branch MR",
            Messages.getQuestionIcon()
        )
        val confirmed = response == Messages.YES

        if (!confirmed) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val output = runJsFile(title, targetBranch, currentBranch)

            CoroutineScope(Dispatchers.EDT).launch {
                Messages.showInfoMessage(output, "Output From JS Script")
            }
        }
    }

    fun runJsFile(title: String, targetBranch: String, currentBranch: String): String {
        val service = service<CapgSettings>()
        val filePath = service.get(SettingsEnum.CREATE_MR_SCRIPT_PATH)
        val processBuilder = ProcessBuilder("node", filePath).redirectErrorStream(true);
        val json = Json.encodeToString(mapOf(
            "sourceBranch" to currentBranch,
            "targetBranch" to targetBranch,
            "title" to title,
        ))
        processBuilder.environment()["CREATE_MR_DETAILS"] = json
        val process = processBuilder.start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        println(output)
        return output
    }
}