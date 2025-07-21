package com.arslan.swissknife.ui.releaseComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class ConfirmReleaseComponentDialog(val project: Project?, val component: String, val currentVersion : String, val nextVersion: String) : DialogWrapper(project) {

    var content: String = ""

    init {
        content = """
            Component : ${component}
            Next version : ${nextVersion}
            Prev version : ${currentVersion}
        """.trimIndent()
        init()
    }


    override fun getDimensionServiceKey(): String? {
        return "SelectComponentDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Content"){
                label(content).bold()
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        // perform Update
    }

}
