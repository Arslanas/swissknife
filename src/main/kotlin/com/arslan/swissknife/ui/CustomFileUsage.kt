package com.arslan.swissknife.ui


import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.usages.TextChunk
import com.intellij.usages.Usage
import com.intellij.usages.UsagePresentation

class CustomFileUsage(private val file: VirtualFile) : Usage {

    override fun getPresentation() = object : UsagePresentation {
        override fun getText() = arrayOf(TextChunk(TextAttributes(), file.presentableUrl))  // Show only file name
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

    override fun navigate(requestFocus: Boolean) { /* Open file on click */ }

}