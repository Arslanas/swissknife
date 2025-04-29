package com.arslan.swissknife.intentions

import com.arslan.swissknife.ui.FileResultDialog
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
import com.intellij.psi.util.PsiUtilCore
import com.intellij.util.PsiNavigateUtil

const val CRUD = "org.springframework.data.repository.CrudRepository"

const val JPA = "org.springframework.data.jpa.repository.JpaRepository"

const val JAKARTA_PERSISTENCE_ENTITY = "jakarta.persistence.Entity"

const val JAVAX_PERSISTENCE_ENTITY = "javax.persistence.Entity"

const val JAKARTA_TABLE = "jakarta.persistence.Table"

const val JAVAX_TABLE = "javax.persistence.Table"

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

        if (candidates.isEmpty()) return

        if (candidates.size == 1){
            ApplicationManager.getApplication().invokeLater {
                PsiNavigateUtil.navigate(candidates.first())
            }
        } else {
            FileResultDialog(project, candidates.mapNotNull(PsiUtilCore::getVirtualFile), "Go to repo", "Go to repo").show()
        }

    }

    private fun PsiClass.isEntityClass(): Boolean {
        return this.annotations.any {
            it.qualifiedName == JAKARTA_PERSISTENCE_ENTITY ||
            it.qualifiedName == JAVAX_PERSISTENCE_ENTITY
        }
    }

    private fun findRepositoryForEntity(project: Project, entityQualifiedName: String): List<PsiClass> {
        val searchScope = GlobalSearchScope.allScope(project)
        val psiFacade = JavaPsiFacade.getInstance(project)

        val repoBases = listOf(
            CRUD,
            JPA
        )

        val repoCandidates = repoBases.flatMap { fqName ->
            val baseClass = psiFacade.findClass(fqName, searchScope) ?: return@flatMap emptyList()
            ClassInheritorsSearch.search(baseClass, searchScope, true).findAll()
        }

        return repoCandidates
            .filterIsInstance<PsiClass>()
            .filter { isRepositoryForEntity(it, entityQualifiedName) }
    }

    private fun isRepositoryForEntity(clazz: PsiClass, entityQualifiedName: String): Boolean {
        if (!clazz.isInterface) return false

        return clazz.extendsListTypes
            .filterIsInstance<PsiClassType>()
            .filter {
                val resolved = it.resolve()?.qualifiedName
                resolved == CRUD ||
                        resolved == JPA
            }
            .mapNotNull { psiClassType ->
                psiClassType.parameters.firstOrNull() as? PsiClassType
            }
            .mapNotNull { it.resolve() }
            .any { it.qualifiedName == entityQualifiedName }
    }
}
