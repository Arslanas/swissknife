package com.arslan.swissknife.intentions

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.usageView.UsageInfo
import com.intellij.usages.*

class ShowSaveIntention : IntentionAction {
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

    override fun invoke(project: Project, editor: Editor?, file: PsiFile?){
        val offset = editor!!.caretModel.offset
        val element = file!!.findElementAt(offset) ?: return
        val psiClass = PsiTreeUtil.getParentOfType(element, PsiClass::class.java) ?: return


        val references = ReferencesSearch.search(psiClass, GlobalSearchScope.projectScope(project))

        val usageInfos = mutableListOf<UsageInfo>()

        for (ref in references) {
            val element = ref.element


            // Look for field/variable declarations
            val parent = element.parent

            if (parent is PsiImportStatement) continue

            val variable = PsiTreeUtil.getParentOfType(element, PsiVariable::class.java)

            if (variable != null) {
                val variableUsages = ReferencesSearch.search(variable, GlobalSearchScope.projectScope(project))
                for (usage in variableUsages) {
                    val usageElement = usage.element
                    val usageParent = usageElement.parent

                    if (usageParent.parent is PsiMethodCallExpression) {
                        val methodCall = usageParent.parent as PsiMethodCallExpression
                        val methodName = methodCall.methodExpression.referenceName
                        if (methodName == "save" || methodName == "saveAll") {
                            usageInfos.add(UsageInfo(methodCall))
                        }
                    }
                }
            } else {
                println("Other usage: ${element.text}")
            }
        }

        if (usageInfos.isEmpty()) {
            Messages.showInfoMessage(project, "No usages of save/saveAll found.", "ShowSave")
        } else {
            UsageViewManager.getInstance(project).showUsages(
                UsageTarget.EMPTY_ARRAY,
                usageInfos.map { UsageInfo2UsageAdapter(it) }.toTypedArray(),
                UsageViewPresentation().apply { tabText = "Save/SaveAll Usages : ${element.text}" }
            )
        }
    }

    override fun startInWriteAction(): Boolean = false
}
