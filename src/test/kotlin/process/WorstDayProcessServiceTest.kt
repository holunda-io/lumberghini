package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.UserName
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.ELEMENTS
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.PREFIX
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess
import io.holunda.funstuff.lumberghini.process.support.StarterProcess
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessTestContext
import io.holunda.funstuff.lumberghini.test.WorstDayProcessTestContext.Companion.manageDeployments
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.spring.annotations.StartProcess
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.*
import org.camunda.bpm.engine.variable.Variables
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as cAssertThat

@Deployment(resources = [StarterProcess.BPMN, MigrationProcess.BPMN])
class WorstDayProcessServiceTest {
  private val context = WorstDayProcessTestContext()

  @get:Rule
  val camunda = context.rule

  private val repository = context.repository
  private val service = context.service

  @Before
  fun setUp() {
    init(camunda.processEngine)
    // make sure no processes are deployed
    assertThat(repository.findAll()).isEmpty()
    CamundaMockito.registerInstance(service)
    CamundaMockito.registerInstance(context.migrationProcess)
  }

  @After
  fun tearDown() {
    camunda.manageDeployments(repository.getDeployments())
  }

  @Test
  fun `can deploy`() {
    service.deploy(service.create(WorstDayProcessFixtures.userName))
  }

  @Test
  fun `create, deploy and start process for user`() {
    val processInstance = startProcess(WorstDayProcessFixtures.userName)

    cAssertThat(processInstance).isActive.isWaitingAt("task-001-01")
  }

  @Test
  fun `deploy created process`() {
    assertThat(repository.getDeployments()).isEmpty()

    var process = service.create(WorstDayProcessFixtures.userName)

    process = service.deploy(process)
    assertThat(process.processDefinitionId).isNotNull()
    assertThat(process.processDefinitionId).startsWith(process.processDefinitionKey)
    assertThat(process.tasks.first().name).isEqualTo("Task 1")

    assertThat(repository.getDeployments()).hasSize(1)
  }

  @Test
  fun `create process for peter`() {
    val process = service.create(WorstDayProcessFixtures.userName)
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().taskId.id).isEqualTo(1)
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter-20201116.bpmn")
    assertThat(process.processDefinitionId).isNull()
  }

  @Test
  fun `use starter process to generate and start worstDayProcess`() {
    assertThat(repository.findAll()).isEmpty()

    val starterInstance = camunda.runtimeService.startProcessInstanceByKey("$PREFIX-starter", Variables.createVariables()
      .putValue("userName", WorstDayProcessFixtures.userName)
    )
    cAssertThat(starterInstance).isWaitingAt("serviceTask_deploy")
    execute(job("serviceTask_deploy"))

    assertThat(repository.findByUserName(WorstDayProcessFixtures.userName)).isNotNull

    cAssertThat(starterInstance).isWaitingAt("serviceTask_start")
    execute(job("serviceTask_start"))
    cAssertThat(starterInstance).isEnded

    val processInstance: ProcessInstance = startProcess(WorstDayProcessFixtures.userName)
    assertThat(processInstance).isNotNull
    cAssertThat(processInstance).isWaitingAt("task-001-01")
  }

  private fun startProcess(userName:UserName): ProcessInstance {
    val instance: ProcessInstance = service.start(userName)
    cAssertThat(instance).isWaitingAt(ELEMENTS.EVENT_START)
    execute(job(ELEMENTS.EVENT_START))
    return instance
  }
}
