package com.arslan.swissknife.psi

import com.intellij.patterns.XmlPatterns
import com.intellij.psi.*
import com.intellij.util.ProcessingContext

class LiquibasePathReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            XmlPatterns
                .xmlAttributeValue("path")
                .withParent(XmlPatterns.xmlAttribute().withParent(XmlPatterns.xmlTag().withName("sqlFile"))),
            object : PsiReferenceProvider() {
                override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
                    return arrayOf(LiquibaseSqlFileReference(element))
                }
            }
        )
    }
}
