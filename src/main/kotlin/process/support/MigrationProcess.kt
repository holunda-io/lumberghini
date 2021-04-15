package io.holunda.funstuff.lumberghini.process.support

import io.holunda.camunda.bpm.data.CamundaBpmData
import io.holunda.camunda.bpm.data.CamundaBpmData.stringVariable
import io.holunda.camunda.bpm.data.factory.VariableFactory
import io.holunda.funstuff.lumberghini.DeploymentId
import io.holunda.funstuff.lumberghini.ProcessDefinitionId
import io.holunda.funstuff.lumberghini.ProcessInstanceId
import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.DelegateExpression
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.ELEMENTS.EVENT_END
import io.holunda.funstuff.lumberghini.process.WorstDayProcessDefinitionRepository
import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.VARIABLES.DEPLOYMENT_ID
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.VARIABLES.LAST_USER_TASK_KEY
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.VARIABLES.SOURCE_PROCESS_DEFINITION_ID
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess.Companion.VARIABLES.TARGET_PROCESS_DEFINITION_ID
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Component

/**
 * This process is responsible for generating and deploying the next processDefinition
 * and migrate the currently suspended processInstance to the new version.
 */
@Component(MigrationProcess.NAME)
class MigrationProcess(
  private val runtimeService: RuntimeService,
  private val repository: WorstDayProcessDefinitionRepository,
  private val service: WorstDayProcessService
) {
  companion object : KLogging() {
    const val NAME = "migrationProcess"
    private const val KEY = "processWorstDay-migration"
    const val BPMN = "bpmn/$KEY.bpmn"
    private const val MESSAGE_START = "message_start_migration"

    /**
     * This starts the migration process.
     */
    fun DelegateExecution.startMigrationProcess(processInstanceId: ProcessInstanceId, sourceProcessDefinitionId: ProcessDefinitionId): ProcessInstance = runtimeService.startProcessInstanceByMessage(
      MESSAGE_START,
      processInstanceId,
      CamundaBpmData.builder()
        .set(SOURCE_PROCESS_DEFINITION_ID, sourceProcessDefinitionId)
        .build()
    )

    private object VARIABLES {
      val SOURCE_PROCESS_DEFINITION_ID: VariableFactory<ProcessDefinitionId> = stringVariable("sourceProcessDefinitionId")
      val TARGET_PROCESS_DEFINITION_ID: VariableFactory<ProcessDefinitionId> = stringVariable("targetProcessDefinitionId")
      val LAST_USER_TASK_KEY: VariableFactory<ProcessDefinitionId> = stringVariable("lastUserTaskKey")
      val DEPLOYMENT_ID: VariableFactory<DeploymentId> = stringVariable("deploymentId")
    }

    private val DelegateExecution.variableProcessInstanceId
      get() = processBusinessKey

    private val DelegateExecution.variableSourceProcessDefinitionId
      get() = SOURCE_PROCESS_DEFINITION_ID.from(this).get()

    private var DelegateExecution.variableTargetProcessDefinitionId
      get() = TARGET_PROCESS_DEFINITION_ID.from(this).get()
      set(value) = TARGET_PROCESS_DEFINITION_ID.on(this).set(value)

    private var DelegateExecution.variableDeploymentId
      get() = DEPLOYMENT_ID.from(this).get()
      set(value) = DEPLOYMENT_ID.on(this).set(value)

    private var DelegateExecution.variableLastUserTaskKey
      get() = LAST_USER_TASK_KEY.from(this).get()
      set(value) = LAST_USER_TASK_KEY.on(this).set(value)

    private val DelegateExecution.runtimeService get() = processEngineServices.runtimeService
  }

  /**
   * Adds a new User task to the current process and deployes the new instance.
   */
  @DelegateExpression
  fun deployNextProcessVersionDelegate() = JavaDelegate {
    val currentProcess = repository.loadByProcessDefinitionId(it.variableSourceProcessDefinitionId)
    val nextProcess = service.createNext(currentProcess)

    // the taskDefinitionKey we migrate the token to
    it.variableLastUserTaskKey = nextProcess.tasks.last().taskDefinitionKey

    val deployment = repository.deploy(nextProcess)
    // unfortunately we can not get the targetProcessDefinitionKey right here, because the deployment can not be loaded
    // from within the delegate
    it.variableDeploymentId = deployment.id
  }

  /**
   * Uses a [MigrationPlan] to migrate the current process instance to the previously deployed version.
   */
  @DelegateExpression
  fun migrateProcessInstanceDelegate() = JavaDelegate {
    // we have to load the process here to get the targetDefinitionId because it is not available
    // in the previous deployment step
    val targetProcessDefinitionId = repository.loadByDeploymentId(it.variableDeploymentId).processDefinitionId
    it.variableTargetProcessDefinitionId = targetProcessDefinitionId

    logger.info { "variables= ${it.variables}" }

    val plan = runtimeService.createMigrationPlan(it.variableSourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build()

    runtimeService.newMigration(plan).processInstanceIds(it.variableProcessInstanceId).execute()
  }

  /**
   * Use process instance modification to move the token from the end event to the new last user task.
   */
  @DelegateExpression
  fun moveTokenToUserTaskDelegate() = JavaDelegate {
    runtimeService.createProcessInstanceModification(it.variableProcessInstanceId)
      .startBeforeActivity(it.variableLastUserTaskKey)
      .cancelAllForActivity(EVENT_END)
      .execute()
  }
}
