package com.arslan.swissknife.actions.git

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.vcs.log.VcsLogDataKeys
import com.intellij.vcs.log.data.AbstractDataGetter.Companion.getCommitDetails
import com.intellij.vcs.log.data.VcsLogData

@Deprecated("Not used anymore")
class OpenGitLabMRAction : AnAction("Open Merge Request on GitLab") {

    override fun update(e: AnActionEvent) {
        val commits = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION)
        e.presentation.isEnabledAndVisible = !commits?.commits.isNullOrEmpty()
    }

    override fun actionPerformed(e: AnActionEvent) {
        val commits = e.getData(VcsLogDataKeys.VCS_LOG_COMMIT_SELECTION)
        if (commits?.commits.isNullOrEmpty()) return

        val commitHash = commits?.commits?.first()?.hash?.asString()

        val commitDetails =
            (e.getData(VcsLogDataKeys.VCS_LOG_DATA_PROVIDER) as VcsLogData).commitDetailsGetter.getCommitDetails(commits?.ids!!)

        commitDetails.first().id.asString() // hash
        commitDetails.first().author // author
        commitDetails.first().fullMessage // message - could extract jira number and open in browser

        // You'd likely want to derive these dynamically
        val gitlabBaseUrl = "https://gitlab.com"
        val projectPath = "your-group/your-repo" // Could be from remote URL
        val mrUrl = "$gitlabBaseUrl/$projectPath/-/commit/$commitHash"

        Messages.showInfoMessage(mrUrl, "Merge Request on GitLab")
    }
}
