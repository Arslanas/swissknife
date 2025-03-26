package com.arslan.swissknife.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.ui.Messages
import java.util.*

class SearchKeywordInFiles : AnAction() {


    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

    }


}
