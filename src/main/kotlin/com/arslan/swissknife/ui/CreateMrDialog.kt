package com.arslan.swissknife.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class CreateMrDialog(project: Project?, val options: List<String>) : DialogWrapper(project) {

    var targetBranch = ""
    var mrTitle = ""

    init {
        title = "Create MR"
        init()
    }


    override fun getDimensionServiceKey(): String? {
        return "CustomInputDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Target branch"){
                comboBox(options, null)
                    .align(Align.FILL)
                    .bindItem({ targetBranch}, {targetBranch = it ?: "" })
            }
            row("Title"){
                textField()
                    .resizableColumn()
                    .align(Align.FILL)
                    .focused()
                    .bindText({ mrTitle}, {mrTitle = it})
            }
        }
    }

}
