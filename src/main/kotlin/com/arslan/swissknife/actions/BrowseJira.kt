package com.arslan.swissknife.actions

import com.arslan.swissknife.enum.SettingsEnum
import com.arslan.swissknife.state.CapgSettings
import com.arslan.swissknife.util.GitUtil
import com.intellij.ide.BrowserUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.Messages

class BrowseJira : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {
        val project = getEventProject(e) ?: return

        val service = service<CapgSettings>()
        val jiraUrl = service.get(SettingsEnum.JIRA_BASE_URL)
        val projectName = service.get(SettingsEnum.JIRA_PROJECT_NAME)

        val currentBranchName = GitUtil.getCurrentBranchName(project) ?: return
        val defaultInput = GitUtil.extractJiraNumber(currentBranchName)
        val input = Messages.showInputDialog(
            project,
            "Enter Jira number",
            "Number",
            Messages.getQuestionIcon(),
            defaultInput,
            null
        )
        if (input.isNullOrBlank()) {
            Messages.showErrorDialog(project, "Jira number cannot be empty.", "Error");
            return
        }

        val url = "${jiraUrl}/${projectName}-${input}"

        BrowserUtil.browse(url)
    }
}