package io.holunda.funstuff.lumberghini.process

import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class WorstDayProcessService(
  private val repositoryService: RepositoryService,
  private val runtimeService: RuntimeService,
  private val findNextTaskStrategy: FindNextTaskStrategy,
  private val todaySupplier: () -> LocalDate = { LocalDate.now() },
  private val deployments: MutableList<Deployment> = mutableListOf()
) {

  fun start(userName: String): WorstDayProcessInstance {
    val process = findDeployedProcess(userName) ?: deploy(create(userName))

    val processInstance =  runtimeService.findSingleInstance(process) ?: runtimeService.startProcessInstanceById(process.processDefinitionId)
    return wrap(processInstance)
  }

  fun create(userName: String) = WorstDayProcess(
    day = todaySupplier(),
    userName = userName,
    task = findNextTaskStrategy.next()
  )

  fun loadProcess(userName: String) = findDeployedProcess(userName)
    ?: throw IllegalArgumentException("no process deployed for user=$userName, day=${todaySupplier()}")

  fun findDeployedProcess(userName: String): WorstDayProcess? {
    val definitionId: String = repositoryService
      .createProcessDefinitionQuery()
      .latestVersion()
      .processDefinitionKey(WorstDayProcess.processDefinitionKey(userName, day = todaySupplier()))
      .singleResult()
      ?.id
      ?: return null

    return repositoryService.loadWorstDayProcess(definitionId)
  }

  fun deploy(process: WorstDayProcess): WorstDayProcess {
    val deployment: Deployment = repositoryService.createDeployment()
      .addModelInstance(process.processResourceName, process.bpmnModelInstance)
      .deploy()
    deployments.add(deployment)

    return repositoryService.loadWorstDayProcessByDeployment(deploymentId = deployment.id)
  }

  fun deployNextVersion(process: WorstDayProcess) = deploy(findNextTaskStrategy.nextVersion(process))

  fun getDeployments() = deployments.toList()

  fun wrap(processInstance:ProcessInstance) = WorstDayProcessInstance(processInstance, runtimeService, repositoryService)
}

internal fun RepositoryService.loadWorstDayProcessByDeployment(deploymentId: String) = this.createProcessDefinitionQuery().deploymentId(deploymentId).singleResult().id.let { loadWorstDayProcess(it) }
internal fun RepositoryService.loadWorstDayProcess(processDefinitionId: String) = WorstDayProcess.readFromModelInstance(this.getBpmnModelInstance(processDefinitionId)).copy(processDefinitionId = processDefinitionId)
internal fun RuntimeService.findSingleInstance(process:WorstDayProcess) : ProcessInstance? = this.createProcessInstanceQuery().processDefinitionId(process.processDefinitionId).singleResult()
