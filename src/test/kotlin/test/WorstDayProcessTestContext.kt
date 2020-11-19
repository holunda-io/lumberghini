package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.WorstDayProcessRepository
import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import io.holunda.funstuff.lumberghini.task.FindNextTaskStrategy
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.MockExpressionManager

class WorstDayProcessTestContext(
  private val configuration: StandaloneInMemProcessEngineConfiguration.() -> StandaloneInMemProcessEngineConfiguration = { this }
) : ProcessEngineServices {

  val processEngineConfiguration: StandaloneInMemProcessEngineConfiguration by lazy {
    StandaloneInMemProcessEngineConfiguration().apply {
      expressionManager = MockExpressionManager()
      isJobExecutorActivate = false
      historyLevel = HistoryLevel.HISTORY_LEVEL_FULL
      databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
      isDbMetricsReporterActivate = false
      isTelemetryReporterActivate = false
      isInitializeTelemetry = false
    }.configuration()
  }

  val processEngine: ProcessEngine by lazy { processEngineConfiguration.buildProcessEngine() }

  val rule: ProcessEngineRule by lazy { ProcessEngineRule(processEngine) }

  val repository: WorstDayProcessRepository by lazy {
    WorstDayProcessRepository(
      repositoryService = repositoryService,
      todaySupplier = WorstDayProcessFixtures.daySupplier
    )
  }

  val service = WorstDayProcessService(
    runtimeService = runtimeService,
    findNextTaskStrategy = FindNextTaskStrategy.countingNextTaskStrategy(),
    todaySupplier = WorstDayProcessFixtures.daySupplier,
    repository = repository
  )

  override fun getRuntimeService() = processEngine.runtimeService
  override fun getRepositoryService() = processEngine.repositoryService
  override fun getFormService() = processEngine.formService
  override fun getTaskService() = processEngine.taskService
  override fun getHistoryService() = processEngine.historyService
  override fun getIdentityService() = processEngine.identityService
  override fun getManagementService() = processEngine.managementService
  override fun getAuthorizationService() = processEngine.authorizationService
  override fun getCaseService() = processEngine.caseService
  override fun getFilterService() = processEngine.filterService
  override fun getExternalTaskService() = processEngine.externalTaskService
  override fun getDecisionService() = processEngine.decisionService
}
