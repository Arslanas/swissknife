package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.CustomFileUsage
import com.intellij.find.FindModel
import com.intellij.find.FindSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.find.impl.FindInProjectUtil
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.findPsiFile
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import com.intellij.util.Processor

class SearchKeywordInFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val keyword = "main"

        val usageViewPresentation = UsageViewPresentation().apply {
            searchString = keyword
            tabText = "Files containing \"$keyword\""
            isOpenInNewTab = true
            isShowCancelButton = true
        }
        val findModel = FindModel().apply {
            stringToFind = keyword
            isCaseSensitive = false
            isWholeWordsOnly = false
            isFindAll = true
            isFindAllEnabled = true
            searchContext = FindModel.SearchContext.ANY
        }

        val processPresentation = FindUsagesProcessPresentation(usageViewPresentation).apply {
            isShowPanelIfOnlyOneUsage = false
        }



        val uniqueFiles = mutableSetOf<VirtualFile>()

        val processor = Processor<UsageInfo> { usageInfo ->
            println(usageInfo)
            usageInfo.virtualFile?.let { uniqueFiles.add(it) }
            true
        }

        FindInProjectUtil.findUsages(findModel, project, processor, processPresentation)


        val usageList = uniqueFiles.map { CustomFileUsage(it)}

        if (usageList.isNotEmpty()) {
            val usageViewManager = UsageViewManager.getInstance(project)
            val usageTargets: Array<UsageTarget> = UsageTarget.EMPTY_ARRAY
            usageViewManager.showUsages(usageTargets, usageList.toTypedArray(), usageViewPresentation)
        } else {
            println("No usages found for '$keyword'")
        }
    }
}
