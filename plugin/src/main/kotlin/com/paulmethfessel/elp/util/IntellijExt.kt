package com.paulmethfessel.elp.util

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.util.parentOfTypes
import com.intellij.refactoring.suggested.startOffset
import com.jetbrains.cidr.lang.psi.OCStruct
import javax.swing.Icon
import kotlin.reflect.KClass

val openProject get() = ProjectManager.getInstance().openProjects.firstOrNull()

val VirtualFile.recursiveChildren get(): List<VirtualFile> = children.flatMap { it.recursiveChildren + it }

fun actionGroup(builder: ActionGroupBuilder.() -> Unit): DefaultActionGroup {
    val b = ActionGroupBuilder()
    builder(b)
    return b.create()
}

class ActionGroupBuilder {
    private val actions = mutableListOf<AnAction>()

    fun action(text: String, description: String, icon: Icon, onActionPerformed: (e: AnActionEvent) -> Unit) {
        actions += object: AnAction(text, description, icon) {
            override fun actionPerformed(e: AnActionEvent) {
                onActionPerformed(e)
            }
        }
    }

    fun create() = DefaultActionGroup(actions.toList())
}

fun Project.error(content: String) {
    NotificationGroupManager
        .getInstance()
        .getNotificationGroup("Embedded Live Programming Notification Group")
        .createNotification(content, NotificationType.ERROR)
        .notify(this)
}

fun Document.getPsiFile(project: Project) = PsiDocumentManager.getInstance(project).getPsiFile(this)
fun VirtualFile.getPsiFile(project: Project) = PsiManager.getInstance(project).findFile(this)
val PsiFile.document get() = PsiDocumentManager.getInstance(project).getDocument(this)
val VirtualFile.document get() = FileDocumentManager.getInstance().getDocument(this)

val PsiFile.struct get() = childOfType<OCStruct>()
val PsiFile.structs get() = childrenOfType<OCStruct>()

val PsiElement.navigable get() = OpenFileDescriptor(project, containingFile.virtualFile, navigationElement.startOffset)

inline fun <reified T: PsiElement> PsiElement.childrenOfType(): List<T> = PsiTreeUtil.findChildrenOfType(this, T::class.java).toList()
inline fun <reified T: PsiElement> PsiElement.childOfType(): T? = PsiTreeUtil.findChildOfType(this, T::class.java)

inline fun <reified T: PsiElement> PsiElement.childAtRangeOfType(range: TextRange) = childAtRangeOfType(range, T::class)

fun <T: PsiElement> PsiElement.childAtRangeOfType(range: TextRange, type: KClass<T>): T? {
    var element = findElementAt(range.startOffset) ?: return null
    while (element.textRange != range || !type.isInstance(element)) {
        if (element.textRange !in range) return null
        element = element.parentOfTypes(type) ?: return null
    }

    return element as? T
}
