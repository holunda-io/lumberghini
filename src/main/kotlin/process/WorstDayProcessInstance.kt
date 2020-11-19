package io.holunda.funstuff.lumberghini.process

import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.runtime.ProcessInstance

data class WorstDayProcessInstance(
  val processInstanceId: String,
  val processDefinitionId: String,
  val processInstance: ProcessInstance? = null,
  private val runtimeService: RuntimeService,
  private val repository: WorstDayProcessRepository
) {

  constructor(processInstance: ProcessInstance, runtimeService: RuntimeService, repository: WorstDayProcessRepository) : this(
    processInstance.processInstanceId,
    processInstance.processDefinitionId,
    processInstance,
    runtimeService,
    repository
  )

  constructor(execution: DelegateExecution, repository: WorstDayProcessRepository) : this(
    execution.processInstanceId,
    execution.processDefinitionId,
    null,
    execution.processEngineServices.runtimeService,
    repository
  )

  val process: WorstDayProcess by lazy {
    repository.loadByProcessDefinitionId(processDefinitionId)
  }

  fun migrateNextVersion(deployNextVersion: (WorstDayProcess) -> WorstDayProcess): WorstDayProcessInstance {
    val currentProcess = process
    val nextProcess = deployNextVersion.invoke(currentProcess)

    val migrationPlan = runtimeService.createMigrationPlan(currentProcess.processDefinitionId, nextProcess.processDefinitionId)
      .mapEqualActivities()
      .mapActivities("endEvent", nextProcess.tasks.last().taskDefinitionKey)
      .build()

    runtimeService.newMigration(runtimeService.createMigrationPlan(
      currentProcess.processDefinitionId,
      nextProcess.processDefinitionId
    ).mapEqualActivities()
      .build())
      .processInstanceIds(processInstanceId)
      .execute()

    return refresh()
  }

  fun refresh(): WorstDayProcessInstance = copy(processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult())
}
