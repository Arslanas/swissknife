package com.arslan.swissknife.actions

import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import javax.swing.JComponent

class SettingsAction : AnAction(){

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run{
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val service = service<CapgSettings>()
        val commonSettingsMap = service.getCommonSettingsMap()

        val dialog = ManageSettingsDialog(project, commonSettingsMap.map { SettingDTO(it.key, it.value) }.toList())

        dialog.show()
    }

    data class SettingDTO(val key: String,  var value: String)

    class ManageSettingsDialog(project : Project, val settings : List<SettingDTO>) : DialogWrapper(project) {

        init {
            title = "Manage Settings"
            init()
        }


        override fun createCenterPanel(): JComponent {
            return panel{
                settings.map {
                    row{
                        label(it.key).bold()
                        textField()
                            .text(it.value)
                            .align(Align.FILL)
                            .bindText({it.value}, {newValue  -> it.value = newValue})
                    }
                }
            }
        }


        override fun doOKAction() {
            super.doOKAction()

            // Save updated values to CapgSettings
            val updatedMap = settings.associate { it.key to it.value }.toMutableMap()
            val service = service<CapgSettings>()
            service.updateSettings(updatedMap)
        }

    }
}