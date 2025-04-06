package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.CreateMrDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import java.io.File

class CreateMrAction : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val dialog = CreateMrDialog(project)
        if (!dialog.showAndGet()) {
            return
        }

        val title = dialog.mrTitle
        val targetBranch = dialog.targetBranch

        val response = Messages.showYesNoDialog(
            project,
            """
                Confirm MR details :
                Target branch : origin/$targetBranch
                Title : $title
            """.trimIndent(),
            "Confirm MR",
            Messages.getQuestionIcon()
        )
        val confirmed = response == Messages.YES

        if (!confirmed) {
            return
        }

        val output = runJsFile()

        Messages.showInfoMessage(output, "Output From JS Script")


    }

    fun runJsFile(): String {
        val process = ProcessBuilder("node", "C:\\D\\APPLICATIONS\\IDE_PLUGINS\\swissknife\\js\\Create_MR.js")
            .redirectErrorStream(true)
            .start()

        val output = process.inputStream.bufferedReader().readText()
        process.waitFor()
        return output
    }
}