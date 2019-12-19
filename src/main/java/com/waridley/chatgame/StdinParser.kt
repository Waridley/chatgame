package com.waridley.chatgame

import com.github.ajalt.clikt.core.*
import com.github.ajalt.clikt.output.TermUi.echo
import com.github.ajalt.clikt.output.defaultCliktConsole
import kotlin.system.exitProcess

class StdinParser {
	
	fun start() {
		println("Started CliParser")
		val console = defaultCliktConsole()
		while(true) {
			println("Enter a command:")
			console.promptForLine("", false)?.let {
				try {
					launchCmd.parse(it.split(" "))
				} catch (e: PrintHelpMessage) {
					echo(e.command.getFormattedHelp())
				} catch (e: PrintCompletionMessage) {
					val s = if (e.forceUnixLineEndings) "\n" else defaultCliktConsole().lineSeparator
					echo(e.message, lineSeparator = s)
				} catch (e: PrintMessage) {
					echo(e.message)
				} catch (e: UsageError) {
					echo(e.helpMessage(), err = true)
				} catch (e: CliktError) {
					echo(e.message, err = true)
				} catch (e: Abort) {
					echo("Aborted!", err = true)
				}
			}
		}
	}
}

class Exit: CliktCommand(name = "exit") {
	override fun run() {
		exitProcess(0)
	}
	
}