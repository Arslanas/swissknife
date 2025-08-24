package com.arslan.swissknife.util


class SourceBranch(
    val name: String,
    val needSuffix: Boolean
){}

class Constants {

    companion object {
        val BRANCH_OPTIONS = arrayOf(
            SourceBranch("master", false),
            SourceBranch("developer", false),
            SourceBranch("capg-mssql.2025", true)
        )


    }
}