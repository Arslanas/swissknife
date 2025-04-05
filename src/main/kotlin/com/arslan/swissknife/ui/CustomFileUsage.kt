package com.arslan.swissknife.ui


import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.usages.TextChunk
import com.intellij.usages.Usage
import com.intellij.usages.UsagePresentation

class CustomFileUsage(private val file: VirtualFile, private val project : Project) : Usage {

    override fun getPresentation() = object : UsagePresentation {
        override fun getText() = arrayOf(TextChunk(TextAttributes(), file.path.removePrefix(project.basePath!! + "/") ))
        override fun getPlainText(): String {
            return file.presentableUrl
        }

        override fun getIcon() = null
        override fun getTooltipText() = file.presentableUrl
    }

    override fun isValid() = true
    override fun isReadOnly() = false
    override fun getLocation(): FileEditorLocation? {
        TODO("Not yet implemented")
    }

    override fun selectInEditor() {}

    override fun highlightInEditor() {}

    override fun canNavigate():Boolean {
        return true
    }

    override fun canNavigateToSource():Boolean {
        return true
    }

    override fun navigate(requestFocus: Boolean) {
        println("Navigating to $file")
        FileEditorManager.getInstance(project).openFile(file, requestFocus, true)
    }

}