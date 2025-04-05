package com.arslan.swissknife.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import javax.swing.*


class CreateMrDialog(project: Project?) : DialogWrapper(project) {

    var title = ""
    private val options = listOf("Option 1", "Option 2", "Option 3")

    init {
        title = "Create Mr Dialog"
        init()
    }


    override fun getDimensionServiceKey(): String? {
        return "CustomInputDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Title"){
                textField()
                    .resizableColumn()
                    .align(Align.FILL)
                    .focused()
                    .bindText({ title}, {title = it})
            }
        }
    }

}
