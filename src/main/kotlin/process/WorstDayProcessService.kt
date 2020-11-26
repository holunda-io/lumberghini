package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.DelegateExpression
import io.holunda.funstuff.lumberghini.UserName
import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.execute
import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.suspend
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.startMigrationProcess
import io.holunda.funstuff.lumberghini.task.FindNextTaskStrategy
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component(WorstDayProcessService.NAME)
class WorstDayProcessService(
  private val runtimeService: RuntimeService,
  private val findNextTaskStrategy: FindNextTaskStrategy,
  private val todaySupplier: () -> LocalDate = { LocalDate.now() },
  private val repository: WorstDayProcessDefinitionRepository
) {
  companion object : KLogging() {
    const val NAME = "worstDayProcessService"

    private fun RuntimeService.findSingleInstance(process: WorstDayProcess): ProcessInstance? = this.createProcessInstanceQuery()
      .processDefinitionId(process.processDefinitionId)
      .singleResult()
  }

  @DelegateExpression
  fun startMigrationListener() = ExecutionListener {
    it.startMigrationProcess(it.processInstanceId, it.processDefinitionId)
  }

  @DelegateExpression
  fun createIncidentListener() = ExecutionListener {
    throw IllegalStateException("this is not supposed to finish!")
  }

  fun start(userName: String): ProcessInstance {
    val process = findDeployedProcess(userName) ?: deploy(create(userName))
    return runtimeService.findSingleInstance(process) ?: runtimeService.startProcessInstanceByKey(process.processDefinitionKey)
  }

  fun create(userName: UserName) = WorstDayProcess(
    day = todaySupplier(),
    userName = userName,
    task = findNextTaskStrategy.next()
  )

  fun createNext(process: WorstDayProcess): WorstDayProcess = findNextTaskStrategy.nextVersion(process)

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
}
