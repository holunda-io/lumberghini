package io.holunda.funstuff.lumberghini.camunda

import io.holunda.funstuff.lumberghini.ProcessInstanceId
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder
import org.camunda.bpm.model.bpmn.instance.FlowNode

object CamundaExtensions {
  val CommandContext.runtimeService get() = this.processEngineConfiguration.runtimeService

  fun DelegateExecution.commandExecutor() = CommandExecutorAdapter(processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl)

  fun <T : Any> DelegateExecution.execute(cmd: Command<T>) = commandExecutor().execute(cmd)

  fun DelegateExecution.suspend() = processEngineServices.runtimeService.suspendProcessInstanceById(processInstanceId)

  fun CommandContext.queryProcessInstance(processInstanceId: ProcessInstanceId) = runtimeService.createProcessInstanceQuery()
    .processInstanceId(processInstanceId)
    .singleResult()

}
