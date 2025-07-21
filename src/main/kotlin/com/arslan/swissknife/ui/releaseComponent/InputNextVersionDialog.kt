package com.arslan.swissknife.ui.releaseComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class InputNextVersionDialog(val project: Project?, val component: String, val currentVersion : String) : DialogWrapper(project) {

    var nextVersion: String = currentVersion

    init {
        title = "Input next version"
        init()
    }


    override fun getDimensionServiceKey(): String? {
        return "InputNextVersionDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Current version"){
                label(currentVersion).bold()
            }
            row("Next version"){
                textField()
                    .resizableColumn()
                    .align(Align.FILL)
                    .focused()
                    .bindText({ nextVersion}, {nextVersion = it})
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        ConfirmReleaseComponentDialog(project, component, currentVersion, nextVersion).show()
    }

}
