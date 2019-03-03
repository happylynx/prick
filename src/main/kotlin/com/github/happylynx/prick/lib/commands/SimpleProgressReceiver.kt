package com.github.happylynx.prick.lib.commands

import com.github.happylynx.prick.lib.verbosity.ProgressReceiver

class SimpleProgressReceiver(
        private val taskName: String,
        private val size: Float = 1.0f,
        private val parent: SimpleProgressReceiver? = null
) : ProgressReceiver {
    override fun message(message: String?, verbosity: ProgressReceiver.Verbosity?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun partDone(percentage: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun subTask(subTaskName: String?, weight: Float): ProgressReceiver {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}