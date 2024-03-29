package com.paulmethfessel.elp.editor

import com.paulmethfessel.elp.actions.showCreateOrOpenExampleDialog
import com.paulmethfessel.elp.services.classService
import com.paulmethfessel.elp.ui.ELPIcons
import com.paulmethfessel.elp.util.struct
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProvider
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import com.jetbrains.cidr.lang.parser.OCKeywordElementType

/**
 * Shows "create example" line marker
 */
class ExampleLineMarkerProvider: LineMarkerProvider {
    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (!(element.elementType is OCKeywordElementType && (element.text == "class" || element.text == "struct"))) return null
        val struct = element.containingFile.struct ?: return null
        val project = element.project
        val clazz = project.classService.findClass(struct.containingFile.virtualFile) ?: return null

        return LineMarkerInfo(
            element,
            element.textRange,
            ELPIcons.createExample,
            { "Create Example for class ${struct.name}" },
            { _, _ -> clazz.showCreateOrOpenExampleDialog() },
            GutterIconRenderer.Alignment.LEFT,
            { "Create Example" }
        )
    }
}
