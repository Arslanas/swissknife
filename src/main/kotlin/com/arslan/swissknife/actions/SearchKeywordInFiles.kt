package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.CustomFileUsage
import com.arslan.swissknife.ui.CustomInputDialog
import com.intellij.find.FindModel
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.usageView.UsageInfo
import com.intellij.usages.FindUsagesProcessPresentation
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageViewManager
import com.intellij.usages.UsageViewPresentation
import com.intellij.util.Processor

class SearchKeywordInFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val dialog = CustomInputDialog(project)
        if (!dialog.showAndGet()) {
            return // User canceled
        }

        val keyword = dialog.getInputText().trim()
        if (keyword.isEmpty()) {
            return
        }

        if (keyword.isEmpty()) {
            return
        }

        val usageViewPresentation = UsageViewPresentation().apply {
            searchString = keyword
            isOpenInNewTab = true
            isShowCancelButton = true
        }
        val findModel = FindModel().apply {
            stringToFind = keyword
            isCaseSensitive = dialog.isCaseSensitive()
            isRegularExpressions = dialog.isRegexp()
            isWholeWordsOnly = false
            isFindAll = true
            isFindAllEnabled = true
            fileFilter = dialog.getFileFilter()
            searchContext = FindModel.SearchContext.ANY
        }

        val processPresentation = FindUsagesProcessPresentation(usageViewPresentation).apply {
            isShowPanelIfOnlyOneUsage = false
        }



        val uniqueFiles = mutableSetOf<VirtualFile>()

        val processor = Processor<UsageInfo> { usageInfo ->
            usageInfo.virtualFile?.let { uniqueFiles.add(it) }
            true
        }

        FindInProjectUtil.findUsages(findModel, project, processor, processPresentation)


        val usageList = uniqueFiles.map { CustomFileUsage(it, project) }

        if (usageList.isNotEmpty()) {
            val usageViewManager = UsageViewManager.getInstance(project)
            val usageTargets: Array<UsageTarget> = UsageTarget.EMPTY_ARRAY
            usageViewPresentation.tabText = "Found ${usageList.size} files containing \"$keyword\""
            usageViewManager.showUsages(usageTargets, usageList.toTypedArray(), usageViewPresentation)
        } else {
            println("No usages found for '$keyword'")
        }
    }
}
