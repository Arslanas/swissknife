package com.arslan.swissknife.actions

import com.arslan.swissknife.util.CommonUtil.Companion.consolePrinter
import com.arslan.swissknife.util.CommonUtil.Companion.selectSourceBranch
import com.arslan.swissknife.util.Constants.Companion.BRANCH_OPTIONS
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.EDT
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.branch.GitBranchUtil
import git4idea.branch.GitRebaseParams
import git4idea.commands.Git
import git4idea.config.GitExecutableManager
import git4idea.rebase.GitRebaseUtils
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RebaseUponMasterBranch : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val optionId = selectSourceBranch(project) ?: return
        val sourceBranch = BRANCH_OPTIONS[optionId]
        val sourceBranchName = sourceBranch.name

        val repository = GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)
        if (repository == null) {
            Messages.showErrorDialog(project, "No Git repository found in the project.", "Error");
            return;
        }

        val git = Git.getInstance()

        val remote = repository.remotes.stream()
            .findFirst()
            .orElseThrow { Exception("Remote not found") }

        val currentBranchName = repository.currentBranchName ?: return

        if (currentBranchName.matches("release.*capg\\.20.*$".toRegex())) {
            val response = Messages.showYesNoDialog(
                project,
                "Are you sure you want to rebase on ${sourceBranchName}?",
                "Confirm Rebase On ${sourceBranchName} Branch",
                Messages.getQuestionIcon()
            )

            if (response != Messages.YES) {
                return
            }

        }

        ProgressManager.getInstance().run(
            object : Task.Backgroundable(project, "Update ${sourceBranchName} branch", false
        ){
                override fun run(indicator: ProgressIndicator) {
                    runOperation(git, repository, remote, indicator, project, sourceBranchName)
                }
            }
        )
    }

    private fun runOperation(
        git: Git,
        repository: GitRepository,
        remote: GitRemote,
        indicator: ProgressIndicator,
        project: Project,
        sourceBranchName : String
    ) {
        val handleError: (String) -> Unit = { errorMessage ->
            CoroutineScope(Dispatchers.EDT).launch {
                Messages.showErrorDialog(
                    project,
                    errorMessage,
                    "Error"
                );
            }
        }

        indicator.text = "Fetching latest changes and updating ${sourceBranchName} branch"
        val fetchResult = git.fetch(repository, remote, listOf(consolePrinter), "${sourceBranchName}:${sourceBranchName}")
        if (!fetchResult.success()) {
            handleError("Failed to fetch the latest changes: ${fetchResult.getErrorOutputAsJoinedString()}")
            return;
        }

        val version = GitExecutableManager.getInstance().getVersion(project)

        val currentBranchName = repository.currentBranchName
        indicator.text = "Fetching upstream branch for $sourceBranchName"
        val newBaseBranchUpstreamName = GitBranchUtil.getTrackInfo(repository, sourceBranchName)?.remoteBranch?.name

        if (newBaseBranchUpstreamName == null) {
            handleError("Could not get remote branch name for $sourceBranchName")
            return;
        }

        val gitRebaseParams = GitRebaseParams(
            version,
            currentBranchName,
            sourceBranchName,
            newBaseBranchUpstreamName,
            false,
            false
        )

        GitRebaseUtils.rebase(project, listOf(repository), gitRebaseParams, indicator)
    }
}