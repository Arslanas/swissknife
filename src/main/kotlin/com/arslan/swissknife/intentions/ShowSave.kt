package com.arslan.swissknife.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.psi.search.searches.MethodReferencesSearch
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.util.RefactoringChangeUtil.getQualifierClass
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*
import com.intellij.util.Query

class ShowSave : IntentionAction {
    override fun getText(): String = "CAPG: show save/saveAll operations"

    override fun getFamilyName(): String = "My Plugin Intentions"

    override fun isAvailable(project: Project, editor: Editor?, file: PsiFile?): Boolean {
        if (editor == null || file == null) return false

        val offset = editor.caretModel.offset
        val element = file.findElementAt(offset) ?: return false
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return false

        val isJPAInterface = psiClass.interfaces.any {
            it.qualifiedName?.contains("CrudRepository") == true
        }

        return isJPAInterface
    }

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?) {
        val offset = editor!!.caretModel.offset
        val element = file!!.findElementAt(offset) ?: return
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return

        val methods = psiClass.allMethods.filter {
            it.name == "save" || it.name == "saveAll"
        }

        if (methods.isEmpty()) {
            Messages.showInfoMessage(project, "No save/saveAll methods found in this interface.", "ShowSave")
            return
        }

        // Find usages of save/saveAll methods
        val usageInfos = mutableListOf<UsageInfo>()
        val searchScope = GlobalSearchScope.projectScope(project)

        for (method in methods) {
            val query = MethodReferencesSearch.search(method, searchScope, true)
            query.forEach { ref ->
                if (psiClass.qualifiedName == getQualifiedClass(ref)) usageInfos.add(UsageInfo(ref.element))
            }
        }

        if (usageInfos.isEmpty()) {
            Messages.showInfoMessage(project, "No usages of save/saveAll found.", "ShowSave")
        } else {
            UsageViewManager.getInstance(project).showUsages(
                UsageTarget.EMPTY_ARRAY,
                usageInfos.map { UsageInfo2UsageAdapter(it) }.toTypedArray(),
                UsageViewPresentation().apply { tabText = "Save/SaveAll Usages" }
            )
        }
    }


    private fun lookByClassFirst(project: Project, editor: Editor?, file: PsiFile?){
        val offset = editor!!.caretModel.offset
        val element = file!!.findElementAt(offset) ?: return
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return


        val references = ReferencesSearch.search(psiClass, GlobalSearchScope.projectScope(project))


        for (ref in references) {
            val element = ref.element


            // Look for field/variable declarations
            val parent = element.parent

            if (parent is PsiImportStatement) continue

            val variable = PsiTreeUtil.getParentOfType(element, PsiVariable::class.java)

            if (variable != null) {
                val variableUsages = ReferencesSearch.search(variable, GlobalSearchScope.projectScope(project))
                for (usage in variableUsages) {
                    val element = usage.element
                    val parent = element.parent

                    if (parent is PsiReferenceExpression &&
                        parent.parent is PsiMethodCallExpression
                    ) {
                        val methodCall = parent.parent as PsiMethodCallExpression
                        val methodName = methodCall.methodExpression.referenceName
                        println("Method call on variable: $methodName → ${methodCall.text}")

                        // TODO : collect and show usages
                    }
                }
            } else {
                println("Other usage: ${element.text}")
            }
        }
    }

    private fun getQualifiedClass(query : PsiReference) : String?{
        val qualifier  = (query.element.parent as PsiMethodCallExpression).methodExpression.qualifierExpression
        val qualifierType = when (qualifier) {
            is PsiReferenceExpression -> {
                val resolved = qualifier.resolve()
                (resolved as? PsiVariable)?.type
            }

            is PsiMethodCallExpression -> {
                qualifier.type
            }

            else -> null
        }

        val qualifierClass = (qualifierType as? PsiClassType)?.resolve()

        return qualifierClass?.qualifiedName
    }

    override fun startInWriteAction(): Boolean = false
}
