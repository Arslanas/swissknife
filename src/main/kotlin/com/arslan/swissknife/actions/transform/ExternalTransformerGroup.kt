package com.arslan.swissknife.actions.transform

import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.vcsUtil.VcsSelectionUtil.getSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExternalTransformerGroup  : ActionGroup() {


    override fun getChildren(event: AnActionEvent?): Array<AnAction> {
        val settings = service<CapgSettings>()
        return settings.getTransformers().map {
            ExternalTransformer(it)
        }.toTypedArray()
    }

    class ExternalTransformer(private val id : String) : AnAction(id) {
        override fun actionPerformed(e: AnActionEvent) {
            val project = e.project ?: return
            val editor = e.getData(CommonDataKeys.EDITOR) ?: return

            val settings = service<CapgSettings>()
            val filePath = settings.getTransformerFilePath(id)!!
            val command = extractCommandFromFilePath(filePath)
            val text = getSelection()

            CoroutineScope(Dispatchers.IO).launch {
                val output = runScript(filePath, command, text)

                CoroutineScope(Dispatchers.EDT).launch {
                    Messages.showInfoMessage(output, "Output From JS Script")
                }
            }
        }

        private fun getSelection(): String {
            TODO("Not yet implemented")
        }

        private fun extractCommandFromFilePath(filePath: String): String {
            TODO("Not yet implemented")
        }


        fun runScript(filePath: String, command: String, text: String): String {

            val processBuilder = ProcessBuilder(command, filePath).redirectErrorStream(true);
            processBuilder.environment()["SELECTION_TEXT"] = text
            val process = processBuilder.start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            println(output)
            return output
        }
    }
}
