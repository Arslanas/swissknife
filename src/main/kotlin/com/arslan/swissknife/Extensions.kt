package com.arslan.swissknife

import com.arslan.swissknife.util.CommonUtil
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import git4idea.commands.GitCommandResult

fun GitCommandResult.requireSuccess(
    project: Project,
    message: String
): GitCommandResult? {
    if (this.success()) return this
    CommonUtil.showError(project, "$message: ${this.getErrorOutputAsJoinedString()}")
    return null
}


fun String?.mandatory(
    project: Project,
    message: String
): String? {
    if (this.isNullOrBlank()) {
        CommonUtil.showError(project, message)
        return null
    }
    return this
}


fun Project.runBackgroundTask(
    title: String,
    cancellable: Boolean,
    action: (ProgressIndicator) -> Unit
) {
    ProgressManager.getInstance().run(
        object : Task.Backgroundable(this, title, cancellable) {
            override fun run(indicator: ProgressIndicator) = action(indicator)
        }
    )
}