package io.holunda.funstuff.lumberghini.process

import io.holunda.camunda.bpm.data.CamundaBpmData
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.repository.Deployment
import org.camunda.bpm.engine.repository.ProcessDefinition
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.model.bpmn.Bpmn
import org.springframework.stereotype.Component
import java.util.*

@Component
class PerpetuumMobileProcess(
  private val runtimeService: RuntimeService,
  private val repositoryService: RepositoryService
) {
  companion object {
    private fun processDefinitionKey(user:String) = "worst-day-for-$user"

    object VARIABLES {
      val USER_NAME = CamundaBpmData.stringVariable("userName")
    }
  }

  val deployments = mutableListOf<Deployment>()


  private fun findOrDeploy(user:String): ProcessDefinition {
    val process: ProcessDefinition? = repositoryService.createProcessDefinitionQuery()
      .active()
      .processDefinitionKey(processDefinitionKey(user))
      .singleResult()

    if (process == null)  {
      deployments.add(deploy(user))
      return findOrDeploy(user)
    }
    return process!!
  }

  fun start(user:String) : PerpetuumMobileProcessInstance{
    val process = findOrDeploy(user)
    val businessKey = UUID.randomUUID().toString()

    return runtimeService.startProcessInstanceByKey(process.key, businessKey, CamundaBpmData.builder()
      .set(VARIABLES.USER_NAME, user)
      .build()).let { wrap(it) }
  }

  fun deploy(user:String): Deployment {
    val key = processDefinitionKey(user)
    val bpmn = Bpmn.createExecutableProcess(key)
      .startEvent()
      .userTask("task-a").name("Task A")
      .endEvent()
      .done()

    return repositoryService.createDeployment().addModelInstance("$key.bpmn", bpmn).deploy()
  }

  fun wrap(processInstance: ProcessInstance) = PerpetuumMobileProcessInstance(processInstance)
}
