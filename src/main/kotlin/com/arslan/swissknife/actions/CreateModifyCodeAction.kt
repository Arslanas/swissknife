package com.arslan.swissknife.actions

import com.arslan.swissknife.state.CapgSettings
import com.arslan.swissknife.ui.ManageSettingsDialog
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBTextField

class CreateModifyCodeAction : AnAction() {

    val CREATE_NEW_QUERY_ID = "Add new"

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val settings = service<CapgSettings>()

        val options: Array<String> = listOf(CREATE_NEW_QUERY_ID)
            .plus(settings.getQueryMap().keys.sorted())
            .toTypedArray()

        var selectedQueryId = Messages.showEditableChooseDialog(
            "Select query for modifications",
            "Release Branches",
            Messages.getQuestionIcon(),
            options,
            options[0],
            null
        )


        if (CREATE_NEW_QUERY_ID.equals(selectedQueryId)){
            selectedQueryId = Messages.showInputDialog(
                JBTextField(),
                "Enter query ID",
                "Enter query ID",
                null
            )
        }


        if (selectedQueryId == null) {
            return
        }


        val query = settings.getQuery(selectedQueryId) ?: ""

        val dialog = ManageSettingsDialog(query, selectedQueryId, !settings.hasQuery(selectedQueryId), true, { settings.deleteQuery(it)})
        val wasOk = dialog.showAndGet()
        if (wasOk) {  // Waits for user action
            val input = dialog.getInputText()
            val response = Messages.showYesNoDialog(
                project,
                "Are you sure you want to create/modify query ${selectedQueryId} ?\n${input}",
                "Confirm Query modification",
                Messages.getQuestionIcon()
            )

            if (response != Messages.YES) {
                return
            }

            settings.saveQuery(selectedQueryId, input)
        }
    }

}
