package com.arslan.swissknife.util

import com.arslan.swissknife.enum.SettingsEnum
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import git4idea.repo.GitRepositoryManager

class GitUtil {


    companion object {
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