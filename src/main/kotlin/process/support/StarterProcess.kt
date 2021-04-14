package io.holunda.funstuff.lumberghini.process.support

import io.holunda.camunda.bpm.data.CamundaBpmData
import io.holunda.camunda.bpm.data.CamundaBpmData.stringVariable
import io.holunda.camunda.bpm.data.factory.VariableFactory
import io.holunda.funstuff.lumberghini.UserName
import mu.KLogging
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.springframework.stereotype.Component

/**
 * This bean represents the starter process `processWorstDay-starter.bpmn`. This is the only process that can
 * directly be started via the task list.
 *
 * It is responsible for generating and starting a personalized process instance of the worst day process.
 */
@Component(StarterProcess.NAME)
class StarterProcess(
  private val runtimeService: RuntimeService
) {
  companion object : KLogging() {
    const val NAME = "starterProcess"
    const val KEY = "processWorstDay-starter"
    const val BPMN = "bpmn/$KEY.bpmn"

    object ELEMENTS {
      const val SERVICE_DEPLOY = "serviceTask_deploy"
      const val SERVICE_START = "serviceTask_start"
    }

    object VARIABLES {
      val USER_NAME: VariableFactory<UserName> = stringVariable("userName")
    }
  }

  fun start(userName: UserName): ProcessInstance = runtimeService.startProcessInstanceByKey(KEY, CamundaBpmData.builder()
    .set(VARIABLES.USER_NAME, userName)
    .build()
  )
}
