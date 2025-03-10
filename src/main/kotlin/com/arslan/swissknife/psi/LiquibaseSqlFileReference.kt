package com.arslan.swissknife.psi

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.xml.XmlAttributeValue
import com.intellij.psi.xml.XmlElement
import com.intellij.util.IncorrectOperationException

class LiquibaseSqlFileReference(element: PsiElement) : PsiReferenceBase<XmlAttributeValue>(element as XmlAttributeValue, true) {

    override fun resolve(): PsiElement? {
        val sqlPath = myElement.value.replace("capricorn/CapricornGlobal/Database", "Database/sql")
        val project = myElement.project
        val baseDir = myElement.containingFile.virtualFile.parent.parent.parent.parent
        val sqlVirtualFile = baseDir.findFileByRelativePath(sqlPath)
        return sqlVirtualFile?.let {
            PsiManager.getInstance(project).findFile(it)
        }
    }
}
