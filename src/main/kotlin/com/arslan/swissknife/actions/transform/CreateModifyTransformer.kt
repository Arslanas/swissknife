package com.arslan.swissknife.actions.transform

import com.arslan.swissknife.state.CapgSettings
import com.arslan.swissknife.ui.ManageSettingsDialog
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField

class CreateModifyTransformer : AnAction() {

    val CREATE_NEW_ID = "Add new"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val settings = service<CapgSettings>()

        val options: Array<String> = listOf(CREATE_NEW_ID)
            .plus(settings.getTransformers())
            .toTypedArray()

        var selectedId = Messages.showEditableChooseDialog(
            "Select transformer for modifications",
            "Transformer Manager",
            Messages.getQuestionIcon(),
            options,
            options[0],
            null
        )


        if (CREATE_NEW_ID.equals(selectedId)){
            selectedId = Messages.showInputDialog(
                JBTextField(),
                "Enter transformer ID",
                "Enter Transformer ID",
                null
            )
        }


        if (selectedId == null) {
            return
        }


        val query = settings.getTransformerFilePath(selectedId) ?: ""

        val dialog = ManageSettingsDialog(query, selectedId, !settings.hasTransformer(selectedId), false, {settings.deleteTransformer(it)} )
        val wasOk = dialog.showAndGet()
        if (wasOk) {  // Waits for user action
            val input = dialog.getInputText()
            val response = Messages.showYesNoDialog(
                project,
                "Are you sure you want to create/modify ${selectedId} ?\n${input}",
                "Confirm Modification",
                Messages.getQuestionIcon()
            )

            if (response != Messages.YES) {
                return
            }

            settings.saveTransformer(selectedId, input)
        }
    }

}
