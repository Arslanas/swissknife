package com.arslan.swissknife.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.JCheckBox
import com.intellij.icons.AllIcons


class CustomInputDialog(project: Project?) : DialogWrapper(project) {

    private val inputField = JTextField()
    private val regexp = JCheckBox("Regexp").apply {
        icon = AllIcons.Actions.Regex
    }
    private val caseSensitive = JCheckBox("Case sensitive").apply {
        icon = AllIcons.Actions.MatchCase
    }

    init {
        title = "Search param dialog"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row("Keyword:") {
                cell(inputField)
                    .resizableColumn()
                    .align(Align.FILL)
            }
            row {
                cell(regexp)
            }
            row {
                cell(caseSensitive)
            }
        }
    }

    fun getInputText(): String = inputField.text
    fun isRegexp(): Boolean = regexp.isSelected
    fun isCaseSensitive(): Boolean = caseSensitive.isSelected
}
