package com.arslan.swissknife.git

import com.arslan.swissknife.util.SwissknifeUtil
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.vcs.CheckinProjectPanel
import com.intellij.openapi.vcs.changes.CommitContext
import com.intellij.openapi.vcs.changes.CommitExecutor
import com.intellij.openapi.vcs.checkin.CheckinHandler
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory
import com.intellij.util.PairConsumer
import git4idea.repo.GitRepositoryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GitCommitMessageHandlerFactory : CheckinHandlerFactory() {
    override fun createHandler(panel: CheckinProjectPanel, commitContext: CommitContext): CheckinHandler {
        return object : CheckinHandler() {
            init {
                val repositoryManager = GitRepositoryManager.getInstance(panel.project)

                val repository = repositoryManager.repositories.firstOrNull()
                val currentBranch = repository?.currentBranchName

                currentBranch?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        val lastCommitMessage = SwissknifeUtil.getLastCommitMessage(panel.project, repository)
                        WriteCommandAction.runWriteCommandAction(panel.project, {
                            if (lastCommitMessage.contains(it)) {
                                panel.setCommitMessage(lastCommitMessage)
                            } else {
                                panel.setCommitMessage("$it : ")
                            }
                        })

                    }
                }
            }

            override fun beforeCheckin(executor: CommitExecutor?, additionalDataConsumer: PairConsumer<Any, Any>?): ReturnResult? {
                // Proceed with commit
                return ReturnResult.COMMIT
            }
        }
    }
}
