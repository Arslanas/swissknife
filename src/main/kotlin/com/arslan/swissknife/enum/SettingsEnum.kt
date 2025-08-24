package com.arslan.swissknife.enum

enum class SettingsEnum(val displayName: String, val key: String) {
    CREATE_MR_SCRIPT_PATH("Create MR script path", "CREATE_MR_SCRIPT_PATH"),
    JIRA_BASE_URL("Base URL for Jira", "JIRA_BASE_URL"),
    JIRA_PROJECT_NAME("Jira project name", "JIRA_PROJECT_NAME"),
    BRANCH_OPTIONS("Comma separated Branch options [name:isRequireSuffix]", "BRANCH_OPTIONS"),
}