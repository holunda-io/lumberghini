package io.holunda.funstuff.lumberghini.test

import org.camunda.bpm.engine.ProcessEngineConfiguration
import org.camunda.bpm.engine.impl.ProcessEngineImpl
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration
import org.camunda.bpm.engine.impl.history.HistoryLevel
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.test.ProcessEngineRule
import org.camunda.bpm.engine.test.mock.MockExpressionManager

class TestProcessEngineConfiguration {

  private val processEngineConfiguration: StandaloneInMemProcessEngineConfiguration = StandaloneInMemProcessEngineConfiguration()

  init {
      with(processEngineConfiguration) {
        expressionManager = MockExpressionManager()
        isJobExecutorActivate = false
        historyLevel = HistoryLevel.HISTORY_LEVEL_FULL
        databaseSchemaUpdate = ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE
        isDbMetricsReporterActivate = false
      }
  }

  fun build() = processEngineConfiguration.buildProcessEngine() as ProcessEngineImpl

  fun rule() = ProcessEngineRule(build())

}

fun ProcessEngineRule.manageDeployments(deployments:List<Deployment>)  = deployments.forEach { this.manageDeployment(it) }
