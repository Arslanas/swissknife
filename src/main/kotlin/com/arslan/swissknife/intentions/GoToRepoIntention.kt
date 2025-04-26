package com.arslan.swissknife.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.PsiNavigateUtil

class GoToRepoIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "Go To Repo"

    override fun getText(): String = "Go To Repository Interface"

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiClass = element.parent as? PsiClass ?: return false
        return psiClass.isEntityClass() && element == psiClass.nameIdentifier
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val psiClass = element.parent as? PsiClass ?: return
        val qualifiedName = psiClass.qualifiedName ?: return

        val scope = GlobalSearchScope.allScope(project)
        val candidates = PsiShortNamesCache.getInstance(project)
            .getAllClassNames()
            .flatMap { PsiShortNamesCache.getInstance(project).getClassesByName(it, scope).asList() }
            .filter { it.isRepositoryForEntity(qualifiedName) }

        val targetRepo = candidates.firstOrNull() ?: return

        ApplicationManager.getApplication().invokeLater {
            PsiNavigateUtil.navigate(targetRepo)
        }
    }

    private fun PsiClass.isEntityClass(): Boolean {
        return this.annotations.any {
            it.qualifiedName == "jakarta.persistence.Entity" ||
            it.qualifiedName == "javax.persistence.Entity"
        }
    }

    private fun PsiClass.isRepositoryForEntity(entityQualifiedName: String): Boolean {
        if (!this.isInterface) return false

        return this.extendsListTypes.any { type ->
            val base = type.resolve()?.qualifiedName
            (base == "org.springframework.data.repository.CrudRepository" ||
             base == "org.springframework.data.jpa.repository.JpaRepository")
                    && (type as? PsiClassType)?.parameters?.firstOrNull()?.resolve()?.qualifiedName == entityQualifiedName
        }
    }
}
