package com.arslan.swissknife.intentions

import com.arslan.swissknife.ui.FileResultDialog
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.PsiNavigateUtil

class GoToUnitTestIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "CAPG"

    override fun getText(): String = "CAPG : Go to unit test"

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiClass = element.parent as? PsiClass ?: return false
        if (psiClass.nameIdentifier?.text?.uppercase()?.contains("TEST") == true) return false
        return element == psiClass.nameIdentifier
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val psiClass = element.parent as? PsiClass ?: return

        val candidates = findTestClassFor(project, psiClass)

        when {
            candidates.isEmpty() -> Messages.showInfoMessage("There are no test classes", "Not Found")
            candidates.size == 1 -> ApplicationManager.getApplication().invokeLater { PsiNavigateUtil.navigate(candidates.first()) }
            else -> FileResultDialog(project, candidates.mapNotNull(PsiUtilCore::getVirtualFile), "Go to repo", "Go to repo").show()
        }
    }

    private fun findTestClassFor(project: Project, sourceClass: PsiClass): List<PsiClass> {
        val sourceNameUpper = sourceClass.name?.uppercase() ?: return emptyList()
        val scope = GlobalSearchScope.allScope(project)

        return PsiShortNamesCache.getInstance(project)
            .allClassNames
            .filter { name ->
                val upper = name.uppercase()
                upper.contains("TEST") && upper.contains(sourceNameUpper)
            }
            .flatMap { name ->
                PsiShortNamesCache.getInstance(project)
                    .getClassesByName(name, scope)
                    .asList()
            }
            .filter { clazz -> hasTestAnnotation(clazz) }
    }

    private fun hasTestAnnotation(psiClass: PsiClass): Boolean {
        val testAnnotations = listOf("org.junit.Test", "org.junit.jupiter.api.Test")

        return psiClass.allMethods.any { method ->
            method.annotations.any { annotation ->
                annotation.qualifiedName in testAnnotations
            }
        }
    }
}
