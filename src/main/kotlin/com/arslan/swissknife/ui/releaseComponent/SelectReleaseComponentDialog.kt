package com.arslan.swissknife.ui.releaseComponent

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.*
import javax.swing.*


class SelectReleaseComponentDialog(val project: Project?, val components: Map<String, String>) : DialogWrapper(project) {

    var targetComponent: String? = null

    init {
        title = "Select component"
        init()
    }


    override fun getDimensionServiceKey(): String? {
        return "SelectComponentDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row("Target branch"){
                comboBox(components.keys, null)
                    .align(Align.FILL)
                    .bindItem({ targetComponent}, {targetComponent = it })
            }
        }
    }

    override fun doOKAction() {
        super.doOKAction()
        val currentVersion = components.get(targetComponent)!!
        // get version properly from pom.xml file
        targetComponent?.let { InputNextVersionDialog(project, it, currentVersion).show() }
    }

}
