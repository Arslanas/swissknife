package com.arslan.swissknife.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import com.intellij.icons.AllIcons
import javax.swing.*


class CustomInputDialog(project: Project?) : DialogWrapper(project) {

    private val inputField = JTextField("")
    private val regexp = JCheckBox(AllIcons.Actions.Regex, false).apply {
        toolTipText = "Regexp"
        icon
    }
    private val caseSensitive = JCheckBox(AllIcons.Actions.MatchCase, false).apply {
        toolTipText = "Case sensitive"
    }
    val iconMatchCase = JLabel(AllIcons.Actions.MatchCase)
    val iconRegex = JLabel(AllIcons.Actions.Regex)
    val iconSearch = JLabel("", AllIcons.Actions.Search, SwingConstants.LEFT)

    init {
        title = "Search param dialog"
        init()
    }

    override fun createCenterPanel(): JComponent {
        return panel {
            row(iconSearch) {
                cell(inputField)
                    .resizableColumn()
                    .align(Align.FILL)
            }
            row() {
                cell(iconRegex)
                cell(regexp)
                cell(iconMatchCase)
                cell(caseSensitive)
            }
        }
    }

    fun getInputText(): String = inputField.text
    fun isRegexp(): Boolean = regexp.isSelected
    fun isCaseSensitive(): Boolean = caseSensitive.isSelected
}
