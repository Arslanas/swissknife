package com.arslan.swissknife.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.AnnotatedElementsSearch

class GoToEntityByNameIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Go to Entity by Name"

    override fun getText(): String = "Go to Entity by Name"

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        return getWordAtElement(element) != null
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val word = getWordAtElement(element) ?: return
        val entityClass = findEntityClassByName(project, word) ?: return
        entityClass.navigate(true)
    }

    private fun getWordAtElement(element: PsiElement): String? {
        if (element is PsiIdentifier) {
            return element.text
        }
        return null
    }

    private fun findEntityClassByName(project: Project, entityName: String): PsiClass? {
        val scope = GlobalSearchScope.allScope(project)

        // Search @Entity classes
        val javaPsiFacade = JavaPsiFacade.getInstance(project)
        val entityAnnotations = listOfNotNull(
            javaPsiFacade.findClass("jakarta.persistence.Entity", scope),
            javaPsiFacade.findClass("javax.persistence.Entity", scope)
        )

        val annotatedClasses = entityAnnotations.flatMap { annotationClass ->
            AnnotatedElementsSearch.searchPsiClasses(annotationClass, scope).toList()
        }

        return annotatedClasses.firstOrNull { psiClass ->
            val annotation = psiClass.getAnnotation("jakarta.persistence.Entity")
                ?: psiClass.getAnnotation("javax.persistence.Entity")
            val nameValue = annotation?.findAttributeValue("name")?.text?.removeSurrounding("\"")
            nameValue == entityName
        }
    }
}
