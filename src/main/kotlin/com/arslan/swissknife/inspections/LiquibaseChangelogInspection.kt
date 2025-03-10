package com.arslan.swissknife.inspections

import com.intellij.codeInspection.*
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.XmlElementVisitor
import com.intellij.psi.xml.XmlAttribute
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

class LiquibaseChangelogInspection : LocalInspectionTool() {

    override fun getDisplayName(): String = "Liquibase changelog path validator"
    override fun getShortName(): String = "LiquibaseChangelog"
    override fun isEnabledByDefault(): Boolean = true
    override fun getStaticDescription(): String = "Check sql path "

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : XmlElementVisitor() {
            override fun visitXmlFile(file: XmlFile) {
                if (!file.name.endsWith("changelog.xml")) return
                super.visitXmlFile(file)
            }

            override fun visitXmlTag(tag: XmlTag) {
                if (tag.name == "sqlFile") {
                    val pathAttr = tag.getAttribute("path", null) ?: return
                    validateSqlPath(holder, pathAttr, tag)
                }
            }
        }
    }

    private fun validateSqlPath(holder: ProblemsHolder, pathAttr: XmlAttribute, tag: XmlTag) {


        val valueElement = pathAttr.valueElement
        if (valueElement == null) {
            holder.registerProblem(
                pathAttr,
                "Should have path specified",
                ProblemHighlightType.ERROR
            )
            return
        }
        val pathValue = pathAttr.value
        val fixedPath = pathValue!!.replace("capricorn/CapricornGlobal/Database", "Database/sql")
        val sqlFile = tag.containingFile.virtualFile.parent.parent.parent.parent.findFileByRelativePath(fixedPath)

        if (sqlFile == null || !sqlFile.exists()) {
            holder.registerProblem(
                valueElement,
                "SQL file path is incorrect",
                ProblemHighlightType.ERROR
            )
        }
    }
}
