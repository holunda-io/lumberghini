package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.DeploymentId
import io.holunda.funstuff.lumberghini.ProcessDefinitionId
import io.holunda.funstuff.lumberghini.TodaySupplier
import io.holunda.funstuff.lumberghini.UserName
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.PREFIX
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.repository.Deployment
import org.springframework.stereotype.Component

@Component
class WorstDayProcessDefinitionRepository(
  private val repositoryService: RepositoryService,
  private val todaySupplier: TodaySupplier
) {
  private val deployments = mutableListOf<Deployment>()

  fun loadByProcessDefinitionId(processDefinitionId: ProcessDefinitionId) = WorstDayProcess
    .readFromModelInstance(repositoryService.getBpmnModelInstance(processDefinitionId))
    .copy(processDefinitionId = processDefinitionId)

  fun loadByDeploymentId(deploymentId: DeploymentId) = repositoryService.createProcessDefinitionQuery()
    .deploymentId(deploymentId)
    .singleResult()
    .id.let { loadByProcessDefinitionId(it) }

  fun findByUserName(userName: UserName): WorstDayProcess? {
    val definitionId: String = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .processDefinitionKey(WorstDayProcess.processDefinitionKey(userName, day = todaySupplier()))
      .singleResult()
      ?.id
      ?: return null

    return loadByProcessDefinitionId(definitionId)
  }

  fun deploy(process: WorstDayProcess): Deployment = repositoryService.createDeployment()
    .addModelInstance(process.processResourceName, process.bpmnModelInstance)
    .deploy()
    .apply { deployments.add(this) }

  fun findAll() = repositoryService.createProcessDefinitionQuery()
    .active()
    .processDefinitionKeyLike("$PREFIX-")
    .list()
    .filterNot { it.key.endsWith("-starter") }
    .map { loadByProcessDefinitionId(it.id) }


  /**
   * Deployments are collected and can be accessed through this method. Mainly used for testing/housekeeping.
   *
   * @return all deployments made by this repository.
   */
  fun getDeployments() = deployments.toList()
}
