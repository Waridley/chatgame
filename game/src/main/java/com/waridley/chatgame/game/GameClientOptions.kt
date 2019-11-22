package com.waridley.chatgame.game

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.OptionGroup

class GameClientOptions: CliktCommand() {
	
	override fun run() {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
}

open class GameBackendOptions(name: String): OptionGroup(name)