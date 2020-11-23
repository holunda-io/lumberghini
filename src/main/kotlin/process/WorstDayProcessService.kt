package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.UserName
import io.holunda.funstuff.lumberghini.task.FindNextTaskStrategy
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class WorstDayProcessService(
  private val runtimeService: RuntimeService,
  private val findNextTaskStrategy: FindNextTaskStrategy,
  private val todaySupplier: () -> LocalDate = { LocalDate.now() },
  private val repository: WorstDayProcessDefinitionRepository
) {

  fun deployNextVersionListener() = ExecutionListener {
    val nextProcess = findNextTaskStrategy.nextVersion(repository.loadByProcessDefinitionId(it.processDefinitionId))
    val deploymentId = repository.deploy(nextProcess).id
    it.setVariable("nextVersionDeploymentId", deploymentId)
  }

  fun migrateNextVersionListener(): ExecutionListener = ExecutionListener {
    val currentProcess = repository.loadByProcessDefinitionId(it.processDefinitionId)
    val nextProcess = repository.loadByDeploymentId(it.getVariable("nextVersionDeploymentId") as String)

    val migrationPlan = runtimeService.createMigrationPlan(currentProcess.processDefinitionId, nextProcess.processDefinitionId)
      .mapEqualActivities()
      //.mapActivities("endEvent", nextProcess.tasks.last().taskDefinitionKey)
      .build()

    runtimeService.newMigration(runtimeService.createMigrationPlan(
      currentProcess.processDefinitionId,
      nextProcess.processDefinitionId
    ).mapEqualActivities()
      .mapActivities("intermediate", currentProcess.tasks.last().taskDefinitionKey)
      .build())
      .processInstanceIds(it.processInstanceId)
      .execute()
  }


  fun start(userName: String): WorstDayProcessInstance {
    val process = findDeployedProcess(userName) ?: deploy(create(userName))
    val processInstance = runtimeService.findSingleInstance(process) ?: runtimeService.startProcessInstanceByKey(process.processDefinitionKey)
    return wrap(processInstance)
  }

  fun create(userName: UserName) = WorstDayProcess(
    day = todaySupplier(),
    userName = userName,
    task = findNextTaskStrategy.next()
  )

  fun createAndDeploy(userName: UserName) = repository.deploy(
    create(userName)
  )

  fun loadProcess(userName: UserName) = findDeployedProcess(userName)
    ?: throw IllegalArgumentException("no process deployed for user=$userName, day=${todaySupplier()}")

  fun findDeployedProcess(userName: UserName): WorstDayProcess? = repository.findByUserName(userName)

  fun deploy(process: WorstDayProcess): WorstDayProcess {
    val deployment: Deployment = repository.deploy(process)

    return repository.loadByDeploymentId(deployment.id)
  }

  fun deployNextVersion(process: WorstDayProcess) = deploy(findNextTaskStrategy.nextVersion(process))


  fun wrap(processInstance: ProcessInstance) = WorstDayProcessInstance(processInstance, runtimeService, repository)

  private fun DelegateExecution.toWorstDayProcessInstance() = WorstDayProcessInstance(this, repository)
}

internal fun RuntimeService.findSingleInstance(process: WorstDayProcess): ProcessInstance? = this.createProcessInstanceQuery()
  .processDefinitionId(process.processDefinitionId)
  .singleResult()
