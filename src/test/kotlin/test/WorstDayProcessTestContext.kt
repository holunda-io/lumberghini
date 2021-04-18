package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.WorstDayProcessDefinitionRepository
import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess
import io.holunda.funstuff.lumberghini.process.support.StarterProcess
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.countingNextTaskStrategy
import org.camunda.bpm.engine.*
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.MockExpressionManager
import org.camunda.spin.plugin.impl.SpinProcessEnginePlugin

class WorstDayProcessTestContext(
  private val configuration: StandaloneInMemProcessEngineConfiguration.() -> Unit = { }
) : ProcessEngineServices {
  companion object {
    fun ProcessEngineRule.manageDeployments(deployments: List<Deployment>) = deployments.forEach { this.manageDeployment(it) }
  }

  val processEngineConfiguration: StandaloneInMemProcessEngineConfiguration by lazy {
    StandaloneInMemProcessEngineConfiguration().apply {
      expressionManager = MockExpressionManager()
      isJobExecutorActivate = false
      historyLevel = HistoryLevel.HISTORY_LEVEL_FULL
      databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
      isDbMetricsReporterActivate = false
      isTelemetryReporterActivate = false
      isInitializeTelemetry = false
      processEnginePlugins.add(SpinProcessEnginePlugin())
      this.configuration()
    }
  }

  val processEngine: ProcessEngine by lazy { processEngineConfiguration.buildProcessEngine() }

  val rule: ProcessEngineRule by lazy { ProcessEngineRule(processEngine) }

  val repository: WorstDayProcessDefinitionRepository by lazy {
    WorstDayProcessDefinitionRepository(
      repositoryService = repositoryService,
      todaySupplier = WorstDayProcessFixtures.daySupplier
    )
  }

  val service = WorstDayProcessService(
    runtimeService = runtimeService,
    identityService = identityService,
    findNextTaskStrategy = countingNextTaskStrategy(),
    todaySupplier = WorstDayProcessFixtures.daySupplier,
    repository = repository
  )

  val migrationProcess = MigrationProcess(
    runtimeService = runtimeService,
    repository = repository,
    service = service
  )

  val starterProcess = StarterProcess(
    runtimeService = runtimeService
  )

  fun createUser(userId: String, firstName: String, lastName: String) = identityService.newUser(userId).apply {
    this.firstName = firstName
    this.lastName = lastName
    this.password = userId
  }


  override fun getRuntimeService(): RuntimeService = processEngine.runtimeService
  override fun getRepositoryService(): RepositoryService = processEngine.repositoryService
  override fun getFormService(): FormService = processEngine.formService
  override fun getTaskService(): TaskService = processEngine.taskService
  override fun getHistoryService(): HistoryService = processEngine.historyService
  override fun getIdentityService(): IdentityService = processEngine.identityService
  override fun getManagementService(): ManagementService = processEngine.managementService
  override fun getAuthorizationService(): AuthorizationService = processEngine.authorizationService
  override fun getCaseService(): CaseService = processEngine.caseService
  override fun getFilterService(): FilterService = processEngine.filterService
  override fun getExternalTaskService(): ExternalTaskService = processEngine.externalTaskService
  override fun getDecisionService(): DecisionService = processEngine.decisionService
}
