package com.arslan.swissknife.ui

import com.arslan.swissknife.state.CapgSettings
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import javax.swing.*

class ManageSettingsDialog(
    private val query: String = "",
    private val queryId: String = "",
    private val isNew: Boolean = false,
) : DialogWrapper(true) {

    private val textArea = JBTextArea(25, 100).apply {
        lineWrap = true
        wrapStyleWord = true
        text = query
    }

    init {
        init()
        title = "Enter SQL Query for $queryId"
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(JBScrollPane(textArea))
        }
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, cancelAction) +
                if (isNew) emptyArray() else arrayOf(DeleteAction(queryId))
    }

    fun getInputText(): String = textArea.text


    private inner class DeleteAction(private val id : String) : AbstractAction("Delete") {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            val settings = service<CapgSettings>()
            settings.deleteQuery(id)
            doCancelAction()
        }
    }
}
