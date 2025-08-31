package com.arslan.swissknife.actions.transform

import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
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
            val caretMapWithText = getSelectionByCaret(editor)

            CoroutineScope(Dispatchers.IO).launch {
                caretMapWithText.keys.forEach{
                    val text = caretMapWithText[it] ?: return@forEach
                    val output = runScript(filePath, command, text).replace("\r\n", "\n").removeSuffix("\n")
                    WriteCommandAction.runWriteCommandAction(project, {
                        editor.document.replaceString(it.selectionStart, it.selectionEnd, output)
                    })
                }
            }
        }

        private fun getSelectionByCaret(editor: Editor): HashMap<Caret, String?> {
            val map = HashMap<Caret, String?>()
            editor.caretModel.allCarets.forEach {
                map.put(it, it.selectedText)
            }
            return map
        }

        val commandMap = mapOf(Pair("js", "node"), Pair("py", "python"), Pair("java", "java"))

        private fun extractCommandFromFilePath(filePath: String): String {
            return commandMap.get(filePath.substringAfter('.'))!!
        }


        fun runScript(filePath: String, command: String, text: String): String {

            val processBuilder = ProcessBuilder(command, filePath).redirectErrorStream(true);
            processBuilder.environment()["SELECTION_TEXT"] = text
            val process = processBuilder.start()

            val output = process.inputStream.bufferedReader().readText()
            process.waitFor()
            return output
        }
    }
}
