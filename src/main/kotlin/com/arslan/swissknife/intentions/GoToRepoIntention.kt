package com.arslan.swissknife.intentions

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.util.PsiNavigateUtil

class GoToRepoIntention : PsiElementBaseIntentionAction() {

    override fun getFamilyName(): String = "CAPG"

    override fun getText(): String = "CAPG : Go to repository"

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val psiClass = element.parent as? PsiClass ?: return false
        return psiClass.isEntityClass() && element == psiClass.nameIdentifier
    }

    override fun invoke(project: Project, editor: Editor, element: PsiElement) {
        val psiClass = element.parent as? PsiClass ?: return
        val qualifiedName = psiClass.qualifiedName ?: return

        val candidates = findRepositoryForEntity(project, qualifiedName)

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

    private fun findRepositoryForEntity(project: Project, entityQualifiedName: String): List<PsiClass> {
        val searchScope = GlobalSearchScope.allScope(project)
        val psiFacade = JavaPsiFacade.getInstance(project)

        val repoBases = listOf(
            "org.springframework.data.repository.CrudRepository",
            "org.springframework.data.jpa.repository.JpaRepository"
        )

        val repoCandidates = repoBases.flatMap { fqName ->
            val baseClass = psiFacade.findClass(fqName, searchScope) ?: return@flatMap emptyList()
            ClassInheritorsSearch.search(baseClass, searchScope, true).findAll()
        }

        return repoCandidates
            .filterIsInstance<PsiClass>()
            .filter { isRepositoryForEntity(it, entityQualifiedName) }
    }
//val baseClass = psiFacade.findClass("org.springframework.data.repository.CrudRepository", searchScope)
//ClassInheritorsSearch.search(baseClass!!, searchScope, true).findAll()
//
    private fun isRepositoryForEntity(clazz: PsiClass, entityQualifiedName: String): Boolean {
        if (!clazz.isInterface) return false

        return clazz.extendsListTypes.any { type ->
            if (type !is PsiClassType) return@any false

            val resolvedClass = type.resolve()
            val qName = resolvedClass?.qualifiedName ?: return@any false

            if (qName != "org.springframework.data.repository.CrudRepository" &&
                qName != "org.springframework.data.jpa.repository.JpaRepository") return@any false

            val typeArguments = type.parameters
            if (typeArguments.isEmpty()) return@any false

            val entityType = typeArguments[0]
            val entityPsiClass = (entityType as? PsiClassType)?.resolve() ?: return@any false

            return@any entityPsiClass.qualifiedName == entityQualifiedName
        }
    }
}
