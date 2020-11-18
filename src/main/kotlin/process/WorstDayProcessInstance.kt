package io.holunda.funstuff.lumberghini.process

import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance

data class WorstDayProcessInstance(
  val processInstance: ProcessInstance,
  private val runtimeService: RuntimeService,
  private val repositoryService: RepositoryService
) {

  val processInstanceId: String = processInstance.id
  val processDefinitionId: String = processInstance.processDefinitionId

  val process: WorstDayProcess by lazy {
    repositoryService.loadWorstDayProcess(processDefinitionId)
  }



  fun refresh(): WorstDayProcessInstance = copy(processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult())
}
