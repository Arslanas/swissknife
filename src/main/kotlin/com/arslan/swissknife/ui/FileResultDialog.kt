package com.arslan.swissknife.ui

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import javax.swing.Action
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.UIManager

class FileResultDialog(
    private val project: Project,
    private val files: List<VirtualFile>,
    private val keyword: String,
    private val dimensionKey: String = "FileResultDialog",
) : DialogWrapper(project, false, IdeModalityType.MODELESS) {

    private lateinit var fileList: JBList<VirtualFile>

    init {
        title = "Search Results : $keyword"
        isResizable = true
        init() // initializes the dialog
    }

    override fun getDimensionServiceKey(): String {
        return dimensionKey
    }

    override fun createCenterPanel(): JComponent {
        fileList = JBList(files).apply {
            cellRenderer = FileNameRenderer(project)
            addListSelectionListener {
                if (!isSelectionEmpty && selectedValue != null) {
                    FileEditorManager.getInstance(project).openFile(selectedValue, true)
                }
            }
        }

        return JBScrollPane(fileList)
    }

    override fun createActions(): Array<Action> {
        return arrayOf(okAction)
    }

    override fun getPreferredFocusedComponent(): JComponent {
        return fileList
    }
}

class FileNameRenderer(private val project: Project) : SimpleListCellRenderer<VirtualFile>() {
    override fun customize(
        list: JList<out VirtualFile>,
        value: VirtualFile?,
        index: Int,
        selected: Boolean,
        hasFocus: Boolean
    ) {
        if (value != null) {
            text = value.path.removePrefix((project.basePath + "/"))

            background = if (!selected) UIManager.getColor("List.background") else list.selectionBackground
            foreground = if (!selected) UIManager.getColor("List.foreground") else list.selectionForeground
        }
    }
}