package com.arslan.swissknife.inspections

import com.intellij.codeInspection.InspectionSuppressor
import com.intellij.codeInspection.SuppressQuickFix
import com.intellij.psi.PsiElement
import com.intellij.psi.xml.XmlAttribute

class LiquibaseInspectionSuppressor : InspectionSuppressor {
    
    override fun isSuppressedFor(element: PsiElement, toolId: String): Boolean {
        if (toolId != "XmlPathReference") return false
        val attribute = element.parent as? XmlAttribute
        if (attribute?.name != "path") return false
        if (attribute.parent.name != "sqlFile") {
            return false;
        }
        return true
    }

    override fun getSuppressActions(element: PsiElement?, toolId: String): Array<SuppressQuickFix> {
        return SuppressQuickFix.EMPTY_ARRAY // No manual quick-fixes, just automatic suppression
    }
}
