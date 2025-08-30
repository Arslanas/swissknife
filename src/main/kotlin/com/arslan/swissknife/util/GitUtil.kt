package com.arslan.swissknife.util

import com.arslan.swissknife.enum.SettingsEnum
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.branch.GitRebaseParams
import git4idea.commands.Git
import git4idea.commands.GitCommandResult
import git4idea.commands.GitLineHandlerListener
import git4idea.config.GitVersion
import git4idea.rebase.GitRebaseUtils
import git4idea.repo.GitRemote
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryManager
import org.jetbrains.annotations.NotNull


class GitUtil {


    companion object {

        fun rebase(
            project: Project,
            indicator: ProgressIndicator,
            repository: GitRepository,
            version: GitVersion,
            currentBranchName: String,
            sourceBranchName: String,
            newBaseBranchUpstreamName: String
        ) {
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

        fun checkout(git: Git, repository: GitRepository, sourceBranchName: String, newBranchName: String): GitCommandResult {
            return git.checkout(repository, sourceBranchName, newBranchName, false, false, CommonUtil.consolePrinter)
        }

        fun getRepo(project : Project): GitRepository? {
            return GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)
        }

        fun getRemoteBranch(project: Project): GitRemote? {
            val repo = getRepo(project) ?: return null
            return repo.remotes.firstOrNull()
        }

        fun fetch(git: Git, repo: GitRepository, remote: GitRemote, vararg params: String): GitCommandResult {
            return git.fetch(repo, remote, listOf(CommonUtil.consolePrinter), *params)
        }


        fun getRemoteBranch(repo : GitRepository): GitRemote? {
            return repo.remotes.firstOrNull()
        }

        fun getCurrentBranchName(project : Project): String? {
            val repository = GitRepositoryManager.getInstance(project).repositories.stream().findFirst().orElse(null)
            if (repository == null) {
                Messages.showErrorDialog(project, "No Git repository found in the project.", "Error");
                return null;
            }
            return repository.currentBranchName
        }

        fun extractJiraNumber(s : String) : String {
            // input example "feature/TRELLO-453-capg-mssql.2025"
            val projectName = CommonUtil.getSetting(SettingsEnum.JIRA_PROJECT_NAME)
            return s
                .replace("feature/", "")
                .replace("release/", "")
                .replace("${projectName}-", "")
                .split('-')
                .first()
        }
    }
}