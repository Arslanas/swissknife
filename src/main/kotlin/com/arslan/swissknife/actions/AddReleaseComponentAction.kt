package com.arslan.swissknife.actions

import com.arslan.swissknife.ui.releaseComponent.ConfirmReleaseComponentDialog
import com.arslan.swissknife.ui.releaseComponent.InputNextVersionDialog
import com.arslan.swissknife.ui.releaseComponent.SelectReleaseComponentDialog
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FilenameIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.xml.XmlFile
import com.intellij.psi.xml.XmlTag

class AddReleaseComponentAction : AnAction() {

    override fun update(e: AnActionEvent) {
        val editor = e.getRequiredData(CommonDataKeys.EDITOR)
        e.project ?: return

        // check if the file is yml and if not then do not show the action otherwise show
//        e.presentation.isEnabledAndVisible = editor.selectionModel.hasSelection()
    }

    /*
        1. Get the map of all pom components
        2. for each add current version
        3. Confirm
        4. Update
     */

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: run {
            Messages.showErrorDialog("No project found", "Error")
            return
        }

        val pomComponents = getPomArtifacts(project)

        val versionMap = pomComponents.mapValues { (_, dto) -> dto.currentVersion }

        val selectDialog = SelectReleaseComponentDialog(project, versionMap)

        if (!selectDialog.showAndGet()) return

        val component = selectDialog.targetComponent ?: return
        val currentVersion = selectDialog.components[component] ?: return

        val inputDialog = InputNextVersionDialog(project, component, currentVersion)

        if (!inputDialog.showAndGet()) return

        val nextVersion = inputDialog.nextVersion

        val confirmDialog = ConfirmReleaseComponentDialog(
            project,
            component,
            currentVersion,
            nextVersion
        )

        if (!confirmDialog.showAndGet()) return

        val pomData = pomComponents[component]  ?: return
        val file = pomData.virtualFile


        val currentFile: VirtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return

        addYamlComponent(project, currentFile, pomData, nextVersion)

        updatePomVersion(project, file, nextVersion)

        // Update of Core component is complex as not all components need update of core version.
        // If Core component selected then need to choose which dependent components need to update core version
        // Also need to provide refresh of update of core version as components could be added later

        // Update of database component : read latest folder version, create new folder with new changelog file, update yml
    }

    data class PomData(
        val artifactId: String,
        val currentVersion: String,
        val virtualFile: VirtualFile
    )

    fun getPomArtifacts(project: Project): Map<String, PomData> {
        val artifacts = mutableMapOf<String, PomData>()
        val psiManager = PsiManager.getInstance(project)

        findAllPomFiles(project).forEach({
            val psiFile = psiManager.findFile(it)
            if (psiFile is XmlFile) {
                val rootTag: XmlTag = psiFile.rootTag ?: return@forEach

                val artifactId = rootTag.findFirstSubTag("artifactId")?.value?.text
                var version = rootTag.findFirstSubTag("version")?.value?.text

                if (version == null) {
                    val parentTag = rootTag.findFirstSubTag("parent")
                    version = parentTag?.findFirstSubTag("version")?.value?.text
                }

                if (artifactId != null && version != null) {
                    artifacts[artifactId] = PomData(artifactId, version, it)
                }
            }
        })

        return artifacts
    }

    fun findAllPomFiles(project: Project): List<VirtualFile> {
        val psiFiles = FilenameIndex.getVirtualFilesByName(
            "pom.xml",
            GlobalSearchScope.projectScope(project)
        )
        return psiFiles.toList()
    }

    fun updatePomVersion(project: Project, pomFile: VirtualFile, newVersion: String) {
        val psiFile = PsiManager.getInstance(project).findFile(pomFile) as? XmlFile ?: return
        val rootTag: XmlTag = psiFile.rootTag ?: return

        val versionTag = rootTag.findFirstSubTag("version")

        val finalVersionTag = versionTag!!
        WriteCommandAction.runWriteCommandAction(project) {
            finalVersionTag.value.text = newVersion
        }
    }

    fun addYamlComponent(project: Project, ymlFile: VirtualFile, pomData: PomData, newVersion: String) {
        val psiFile = PsiManager.getInstance(project).findFile(ymlFile) ?: return
        val document = PsiDocumentManager.getInstance(project).getDocument(psiFile) ?: return

        val artifactId = pomData.artifactId
        val currentVersion = pomData.currentVersion.replace("-SNAPSHOT", "")
        val nextVersion = newVersion.replace("-SNAPSHOT", "")
        val deploymentDirectory = if (artifactId == "env") "release" else artifactId

        val newEntry = """
            
        - artifact_id: ${artifactId}
          version:
            current: ${nextVersion}
            previous: ${currentVersion}
          deployment_directory: ${deploymentDirectory}
          
    """.trimIndent()

        WriteCommandAction.runWriteCommandAction(project) {
            val text = document.text

            // Find the "components:" section and append inside it
            val index = text.indexOf("components:")
            if (index >= 0) {
                val insertPos = text.indexOf('\n', index) + 1
                document.insertString(insertPos, newEntry.prependIndent("    ") + "\n")
            }
        }
    }


}