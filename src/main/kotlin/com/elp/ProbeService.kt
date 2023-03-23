package com.elp

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.util.Disposer

@Service
class ProbeService: Disposable {
    var probes = mutableListOf<ProbePresentation>()

    val probeUpdater = ProbeUpdateController().start()
    val runner = Runner().start()

    init {
        Disposer.register(this, runner)
        Disposer.register(this, probeUpdater)
    }

    override fun dispose() = Unit
}

val probeService get() = service<ProbeService>()
