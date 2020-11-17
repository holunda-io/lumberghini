package io.holunda.funstuff.lumberghini.process

import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance


class PerpetuumMobileProcessInstance(
  private val runtimeService: RuntimeService,
  private val repositoryService: RepositoryService,
  val processInstance: ProcessInstance
) {


  fun getModelInstance() = repositoryService.getBpmnModelInstance(processInstance.processDefinitionId)

}
