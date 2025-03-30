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

        val findModel = FindModel().apply {
            stringToFind = keyword
            isCaseSensitive = false
            isWholeWordsOnly = false
            isFindAll = true
            isFindAllEnabled = true
            searchContext = FindModel.SearchContext.ANY
        }

        // Store unique files that contain the keyword
        val uniqueFiles = mutableSetOf<VirtualFile>()

        val usageViewPresentation = UsageViewPresentation().apply {
            searchString = keyword
            tabText = "Files containing \"$keyword\""
            isOpenInNewTab = true
            isCodeUsages = false  // Forces flat list view
            isMergeDupLinesAvailable = false // Prevents merging similar results
            isShowReadOnlyStatusAsRed = false
            isShowCancelButton = true
        }

        val processPresentation = FindUsagesProcessPresentation(usageViewPresentation).apply {
            isShowPanelIfOnlyOneUsage = false
        }

//        val usageList = mutableListOf<Usage>()

        val processor = Processor<UsageInfo> { usageInfo ->
            println(usageInfo)
            usageInfo.virtualFile?.let { uniqueFiles.add(it) }
//            usageList.add(UsageInfo2UsageAdapter(usageInfo))
            true
        }

        FindInProjectUtil.findUsages(findModel, project, processor, processPresentation)

        // Convert file results into Usage objects
        val usageList = uniqueFiles.map { file ->
            object : Usage {
                override fun getPresentation() = object : UsagePresentation {
                    override fun getText() = arrayOf(TextChunk(TextAttributes(), file.presentableUrl))  // Show only file name
                    override fun getPlainText(): String {
                        return file.presentableUrl
                    }

                    override fun getIcon() = null
                    override fun getTooltipText() = file.presentableUrl
                }

                override fun isValid() = true
                override fun isReadOnly() = false
                override fun getLocation(): FileEditorLocation? {
                    TODO("Not yet implemented")
                }

                override fun selectInEditor() {}

                override fun highlightInEditor() {}

                override fun navigate(requestFocus: Boolean) { /* Open file on click */ }
        } }


        if (usageList.isNotEmpty()) {
            val usageViewManager = UsageViewManager.getInstance(project)
            val usageTargets: Array<UsageTarget> = UsageTarget.EMPTY_ARRAY

            // Display the collected usages in the tool window
            usageViewManager.showUsages(usageTargets, usageList.toTypedArray(), usageViewPresentation)
        } else {
            println("No usages found for '$keyword'")
        }
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
