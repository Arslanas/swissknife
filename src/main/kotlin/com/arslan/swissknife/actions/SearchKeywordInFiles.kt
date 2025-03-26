package com.arslan.swissknife.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import java.util.*
import kotlin.collections.HashMap

class SearchKeywordInFiles : AnAction() {


    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }


        val psiSearchHelper = PsiSearchHelper.getInstance(project)
        val scope = GlobalSearchScope.projectScope(project)

        val keyword = "main"
        val caseSensitivity = false

        val map = HashMap<String, Int>()
        val visited = HashSet<String>()

        psiSearchHelper.processElementsWithWord(
            { element: PsiElement, offset: Int ->
                val file = element.containingFile?.virtualFile
                if (file != null) {
                    if (visited.contains(file.name)) return@processElementsWithWord true
                    map.put(file.name, map.getOrDefault(file.name, 0) + 1)
                    visited.add(file.name)
                }
                true // Continue search
            },
            scope,
            keyword,
            UsageSearchContext.ANY,
            caseSensitivity
        )

        map.forEach {  key, value -> println("$key: $value") }
    }


}
