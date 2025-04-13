package com.arslan.swissknife.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import java.util.function.Consumer
import javax.swing.*

class ManageSettingsDialog(
    private val value: String = "",
    private val id: String = "",
    private val isNew: Boolean = false,
    private val isMultiLine: Boolean = true,
    private val deleteOperation : Consumer<String>
) : DialogWrapper(true) {

    private val textArea = JBTextArea(25, 100).apply {
        lineWrap = true
        wrapStyleWord = true
        text = value
    }

    private val inputField = JTextField(value)

    init {
        init()
        title = "Enter Value for $id"
    }

    override fun createCenterPanel(): JComponent {
        return JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            if (isMultiLine) add(JBScrollPane(textArea)) else add(inputField)
        }
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction, cancelAction) +
                if (isNew) emptyArray() else arrayOf(DeleteAction(id))
    }

    fun getInputText(): String = if (isMultiLine) textArea.text else inputField.text


    private inner class DeleteAction(private val id : String) : AbstractAction("Delete") {
        override fun actionPerformed(e: java.awt.event.ActionEvent?) {
            deleteOperation.accept(id)
            doCancelAction()
        }
    }
}
