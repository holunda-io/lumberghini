package io.holunda.funstuff.lumberghini.camunda

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.context.Context
import org.camunda.bpm.engine.impl.interceptor.Command

/**
 * Adapter that hides the execution of a camunda [Command], works with
 * [org.camunda.bpm.engine.impl.interceptor.CommandContext] when called inside
 * a camunda [org.camunda.bpm.engine.delegate.JavaDelegate] or Listener
 * but uses [ProcessEngineConfigurationImpl.getCommandExecutorTxRequired]
 * to get a new context when called elsewhere.
 */
class CommandExecutorAdapter(private val configuration: ProcessEngineConfigurationImpl) {
  fun <T> execute(command: Command<T>): T = if (Context.getCommandContext() == null) {
    configuration.commandExecutorTxRequired.execute(command)
  } else {
    command.execute(Context.getCommandContext())
  }
}
