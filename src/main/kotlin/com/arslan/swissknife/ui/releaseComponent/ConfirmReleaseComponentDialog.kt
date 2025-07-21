package com.arslan.swissknife.ui.releaseComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class ConfirmReleaseComponentDialog(val project: Project?, val component: String, val currentVersion : String, val nextVersion: String) : DialogWrapper(project) {


    init {
        init()
    }

    override fun getDimensionServiceKey(): String? {
        return "SelectComponentDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Component"){
                label(component).bold()
            }
            row("Current version"){
                label(currentVersion).bold()
            }
            row("New version"){
                label(nextVersion).bold()
            }
        }
    }
}
