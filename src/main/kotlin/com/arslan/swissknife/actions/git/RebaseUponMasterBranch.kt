package com.arslan.swissknife.actions.git

import com.arslan.swissknife.confirm
import com.arslan.swissknife.mandatory
import com.arslan.swissknife.requireSuccess
import com.arslan.swissknife.runBackgroundTask
import com.arslan.swissknife.util.CommonUtil
import com.arslan.swissknife.util.CommonUtil.Companion.getBranchOptions
import com.arslan.swissknife.util.CommonUtil.Companion.selectSourceBranch
import com.arslan.swissknife.util.GitUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import git4idea.branch.GitBranchUtil
import git4idea.commands.Git
import git4idea.config.GitExecutableManager

class RebaseUponMasterBranch : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {

        val project = CommonUtil.getProject(e) ?: return
        val branchOptions = getBranchOptions();
        val optionId = selectSourceBranch(project, branchOptions ) ?: return
        val sourceBranch = branchOptions[optionId]
        val sourceBranchName = sourceBranch.name

        val repository = GitUtil.getRepo(project) ?: return

        val git = Git.getInstance()

        val remote = repository.remotes.firstOrNull() ?: return

        val currentBranchName = repository.currentBranchName ?: return

        if (currentBranchName.matches("release.*capg\\.20.*$".toRegex())) {
            if (!project.confirm("Are you sure you want to rebase on $sourceBranchName?")) return
        }

        project.runBackgroundTask("Update ${sourceBranchName} branch", false) { indicator ->
            indicator.text = "Fetching latest changes and updating ${sourceBranchName} branch"

            GitUtil.fetch(git, repository, remote, "${sourceBranchName}:${sourceBranchName}")
                .requireSuccess(project,  "Failed to fetch the latest changes") ?: return@runBackgroundTask

            val version = GitExecutableManager.getInstance().getVersion(project)

            indicator.text = "Fetching upstream branch for $sourceBranchName"
            val newBaseBranchUpstreamName = GitBranchUtil.getTrackInfo(repository, sourceBranchName)?.remoteBranch?.name
                .mandatory(project, "Could not get remote branch name for $sourceBranchName") ?: return@runBackgroundTask

            GitUtil.rebase(
                project,
                indicator,
                repository,
                version,
                currentBranchName,
                sourceBranchName,
                newBaseBranchUpstreamName
            )
        }
    }
}