package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.FileResultDialog
import com.intellij.codeInsight.hint.HintManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.PsiUtilCore

class GoToEntityByNameAction : AnAction("Go to Entity by Name") {

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val document = editor.document
        val offset = editor.caretModel.offset

        val word = getWordAtOffset(document.text, offset) ?: run {
            HintManager.getInstance().showErrorHint(editor, "No word at caret.")
            return
        }

        val entities = findEntitiesByName(project, word)

        when {
            entities.isEmpty() -> {
                HintManager.getInstance().showErrorHint(editor, "Entity not found: $word")
            }
            entities.size == 1 -> {
                entities.first().navigate(true)
            }
            else -> {
                FileResultDialog(project, entities.mapNotNull(PsiUtilCore::getVirtualFile), "Go to entity", "Go to entity").show()
            }
        }
    }

    private fun getWordAtOffset(text: String, offset: Int): String? {
        if (offset < 0 || offset >= text.length) return null
        val regex = "\\w+".toRegex()
        return regex.findAll(text).firstOrNull { it.range.contains(offset) }?.value
    }

    private fun findEntitiesByName(project: Project, entityName: String): List<PsiClass> {
        val scope = GlobalSearchScope.allScope(project)

        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val entityAnnotations = listOfNotNull(
            javaPsiFacade.findClass("jakarta.persistence.Entity", scope),
            javaPsiFacade.findClass("javax.persistence.Entity", scope)
        )

        val annotatedClasses = entityAnnotations.flatMap { annotationClass ->
            AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).toList()
        }

        return annotatedClasses.filter { psiClass ->
            val annotation = psiClass.getAnnotation("jakarta.persistence.Entity")
                ?: psiClass.getAnnotation("javax.persistence.Entity")
            val nameValue = annotation?.findAttributeValue("name")?.text?.removeSurrounding("\"")
            nameValue == entityName
        }
    }
}
