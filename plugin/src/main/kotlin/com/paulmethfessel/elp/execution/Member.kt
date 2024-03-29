package com.paulmethfessel.elp.execution

import com.paulmethfessel.elp.util.navigable
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.jetbrains.cidr.lang.psi.OCDeclaration
import com.jetbrains.cidr.lang.psi.OCFunctionDeclaration
import com.jetbrains.cidr.lang.psi.OCStruct
import com.jetbrains.cidr.lang.types.OCStructType

sealed class Member(
    val element: OCDeclaration
) {
    val isStatic = element.isStatic
    val file = element.containingFile.name
    abstract val name: String
    abstract val navigable: OpenFileDescriptor?

    class Function(
        function: OCFunctionDeclaration
    ) : Member(function) {
        val parameters = function.parameters?.map { "${it.type.name} ${it.name}" } ?: listOf()
        override val name = function.name ?: "undefined"
        override val navigable = function.nameIdentifier?.navigable
        val type = function.type.name

        val parameterString = "(${parameters.joinToString(", ")})"

        override fun equalsIgnoreFile(other: Member) =
            super.equalsIgnoreFile(other) && other is Function && other.parameters == parameters

        override fun equals(other: Any?) =
            super.equals(other) && other is Function && other.parameters == parameters

        override fun hashCode() = super.hashCode() * 31 + parameters.hashCode()

        override fun toString() = "$staticStr$name$parameterString"
    }

    class Field(
        field: OCDeclaration
    ) : Member(field) {
        val type = field.type.name
        val typeElement = field.type
        override val name = field.declarators.firstOrNull()?.name ?: "undefined"
        override val navigable = field.declarators.firstOrNull()?.nameIdentifier?.navigable
        val value = field.declarators.firstOrNull()?.initializer?.text
        val initializer = field.declarators.firstOrNull()?.initializer

        override fun equalsIgnoreFile(other: Member) =
            super.equalsIgnoreFile(other) && other is Field && other.type == type

        override fun equals(other: Any?) =
            super.equals(other) && other is Field && other.type == type

        override fun hashCode() = super.hashCode() * 31 + type.hashCode()

        override fun toString() = "$staticStr$type $name"
    }

    open infix fun equalsIgnoreFile(other: Member) =
        other.name == name && other.isStatic == isStatic

    override fun equals(other: Any?) =
        other is Member && other.file == file && other.name == name && other.isStatic == isStatic

    override fun hashCode() = (file.hashCode() * 31 + (name.hashCode())) * 31 + isStatic.hashCode()

    protected val staticStr = if (isStatic) "static " else ""
}

val OCStruct.memberFunctions get() = children.filterIsInstance<OCFunctionDeclaration>().map { it.asMember() }
val OCStruct.memberFields
    get() = children
        .filterIsInstance<OCDeclaration>()
        .filter { it !is OCFunctionDeclaration && it.type !is OCStructType }
        .map { it.asMember() }
val OCStruct.allMembers get() = memberFunctions + memberFields

fun OCFunctionDeclaration.asMember() = Member.Function(this)
fun OCDeclaration.asMember() = Member.Field(this)

val OCStruct.loop get() = memberFunctions.find { it.name == "liveLoop" && it.parameters.isEmpty() && !it.isStatic }
val OCStruct.setup get() = memberFunctions.find { it.name == "setup" && it.parameters.isEmpty() && !it.isStatic }
