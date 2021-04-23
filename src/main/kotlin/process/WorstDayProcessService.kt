package io.holunda.funstuff.lumberghini.process

import io.holunda.camunda.bpm.data.CamundaBpmData
import io.holunda.funstuff.lumberghini.UserName
import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.DelegateExpression
import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.toOptional
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.VARIABLES
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.datePattern
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.startMigrationProcess
import io.holunda.funstuff.lumberghini.strategy.FindNextTaskStrategy
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.engine.delegate.TaskListener
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
    with(it) {

      startMigrationProcess(processInstanceId, processDefinitionId)
    }
  }

  @DelegateExpression
  fun onTaskCreate() = TaskListener { task ->
    val userId = VARIABLES.userName.from(task).get()
    val user = task.processEngineServices.identityService.createUserQuery().userId(userId).singleResult().toOptional()

    user.map { it.firstName }.ifPresent {
      task.name = task.name.replace("{firstName}", it)
    }
  }

  @DelegateExpression
  fun throwLumberghInterventionListener() = LumberghInterventionException.throwExceptionListener()

  fun start(userId: String): ProcessInstance {
    val process = findDeployedProcess(userId) ?: deploy(create(userId))
    val date = process.day.format(datePattern)
    return runtimeService.findSingleInstance(process) ?: runtimeService.startProcessInstanceByKey(
      process.processDefinitionKey,
      CamundaBpmData.builder()
        .set(VARIABLES.userName, process.userId)
        .set(VARIABLES.day, date)
        .build()
    )
  }

  fun create(userId: String) = WorstDayProcess(
    day = todaySupplier(),
    userId = userId
  ).addTask(findNextTaskStrategy.first())

  fun createNext(process: WorstDayProcess): WorstDayProcess = findNextTaskStrategy.nextVersion(process)

  fun createAndDeploy(userName: UserName) = repository.deploy(
    create(userName)
  )

  fun loadProcess(userId: String) = findDeployedProcess(userId)
    ?: throw IllegalArgumentException("no process deployed for user=$userId, day=${todaySupplier()}")

  fun findDeployedProcess(userId: String): WorstDayProcess? = repository.findByUserId(userId)

  fun deploy(process: WorstDayProcess): WorstDayProcess {
    val deployment: Deployment = repository.deploy(process)

    return repository.loadByDeploymentId(deployment.id)
  }

  fun deployNextVersion(process: WorstDayProcess) = deploy(findNextTaskStrategy.nextVersion(process))
}
