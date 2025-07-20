package com.arslan.swissknife.intentions

import com.arslan.swissknife.ui.FileResultDialog
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch
import com.intellij.psi.util.PsiUtilCore

class GoToEntityByNameIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Go to Entity by Name"

    override fun getText(): String = "Go to Entity by Name"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return getWordAtElement(element) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val word = getWordAtElement(element) ?: return
        invoke(project, editor, word)
    }

    fun invoke(project: Project, editor: Editor?, word: String) {
        val entities = findEntitiesByName(project, word)
        when {
            entities.isEmpty() -> {
                if (editor != null && (editor as? EditorEx) != null) {
                    HintManager.getInstance().showErrorHint(editor, "Entity not found: $word")
                }
            }
            entities.size == 1 -> {
                entities.first().navigate(true)
            }
            else -> {
                FileResultDialog(
                    project,
                    entities.mapNotNull(PsiUtilCore::getVirtualFile),
                    "Go to entity",
                    "Go to entity"
                ).show()
            }
        }
    }

    private fun getWordAtElement(element: PsiElement): String? {
        return (element as? PsiIdentifier)?.text
    }

    private fun findEntitiesByName(project: Project, entityName: String): List<PsiClass> {
        val scope = GlobalSearchScope.allScope(project)

        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val entityAnnotations = listOfNotNull(
            javaPsiFacade.findClass(JAKARTA_TABLE, scope),
            javaPsiFacade.findClass(JAVAX_TABLE, scope)
        )

        val annotatedClasses = entityAnnotations.flatMap { annotationClass ->
            AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).toList()
        }

        return annotatedClasses.filter { psiClass ->
            val annotation = psiClass.getAnnotation(JAKARTA_TABLE)
                ?: psiClass.getAnnotation(JAVAX_TABLE)
            val nameValue = annotation?.findAttributeValue("name")?.text?.removeSurrounding("\"")
            nameValue == entityName
        }
    }
}
