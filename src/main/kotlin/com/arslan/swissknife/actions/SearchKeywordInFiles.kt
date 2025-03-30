package com.arslan.swissknife.actions

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
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.usageView.UsageInfo
import com.intellij.usages.FindUsagesProcessPresentation
import com.intellij.usages.UsageViewPresentation
import com.intellij.util.Processor

class SearchKeywordInFiles : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val findModel = FindModel().apply {
            stringToFind = "yourKeyword"
            isCaseSensitive = true
            isWholeWordsOnly = false
        }

        val keyword = "main"
        val usageViewPresentation = UsageViewPresentation().apply {
            searchString = keyword
            isOpenInNewTab = true
            tabText = "Search results for \"$keyword\""
        }

        val processPresentation = FindUsagesProcessPresentation(usageViewPresentation).apply {
            isShowPanelIfOnlyOneUsage = false
        }

        val processor = Processor<UsageInfo> { usageInfo ->
            println("Found usage at: ${usageInfo.virtualFile?.path}:${usageInfo.navigationRange?.startOffset}")
            true // Return true to continue processing
        }

        FindInProjectUtil.findUsages(findModel, project, processor, processPresentation)
    }


//    override fun actionPerformed(e: AnActionEvent) {
//
//        val project = e.project ?: run{
//            Messages.showErrorDialog("No project found", "Error")
//            return
//        }
//
//
//        val psiSearchHelper = PsiSearchHelper.getInstance(project)
//        val scope = GlobalSearchScope.projectScope(project)
//
//        val keyword = "main"
//        val caseSensitivity = false
//
//        val visited = HashSet<VirtualFile>()
//
//        psiSearchHelper.processElementsWithWord(
//            { element: PsiElement, offset: Int ->
//                val file = element.containingFile?.virtualFile
//                if (file != null) {
//                    if (visited.contains(file)) return@processElementsWithWord true
//                    visited.add(file)
//                }
//                true // Continue search
//            },
//            scope,
//            keyword,
//            UsageSearchContext.ANY,
//            caseSensitivity
//        )
//
//
//        val map = HashMap<String, Int>()
//
//        visited.forEach({ file ->
//            val content = VfsUtilCore.loadText(file) // Read file content
//            val count = countOccurrences(content, keyword)
//            if (count > 0) {
//                map[file.path] = count
//            }
//        })
//
//
//
//        map.forEach {  key, value -> println("$key: $value") }
//    }


    private fun countOccurrences(text: String, keyword: String): Int {
        return Regex(keyword, RegexOption.IGNORE_CASE).findAll(text).count() // Match whole words only
    }
}
