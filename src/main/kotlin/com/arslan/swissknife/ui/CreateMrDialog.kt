package com.arslan.swissknife.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import javax.swing.*


class CreateMrDialog(project: Project?) : DialogWrapper(project) {

    private val inputField = JTextField("")
    private val regexp = JCheckBox(AllIcons.Actions.Regex,  false)

    private val caseSensitive = JCheckBox(AllIcons.Actions.MatchCase,  false)

    init {
        title = "Search param dialog"
        init()
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return inputField
    }

    override fun getDimensionServiceKey(): String? {
        return "CustomInputDialog"
    }

    override fun createCenterPanel(): JComponent {
        return panel() {
            row(JLabel("", AllIcons.Actions.Search, SwingConstants.LEFT)) {
                cell(inputField)
                    .resizableColumn()
                    .align(Align.FILL)
            }
            row(JLabel("")) {
                cell(JLabel("", AllIcons.Actions.Regex, SwingConstants.LEFT))
                cell(regexp)
                cell(JLabel("", AllIcons.Actions.MatchCase, SwingConstants.LEFT))
                cell(caseSensitive)
            }
        }
    }

    fun getInputText(): String = inputField.text
    fun isRegexp(): Boolean = regexp.isSelected
    fun isCaseSensitive(): Boolean = caseSensitive.isSelected
}
