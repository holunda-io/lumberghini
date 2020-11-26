package io.holunda.funstuff.lumberghini.process

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
    val processInstance = service.start(WorstDayProcessFixtures.userName)

    cAssertThat(processInstance).isActive.isWaitingAt("task1-000")
  }

  @Test
  fun `deploy next version and migrate`() {
    val processInstance = service.start(WorstDayProcessFixtures.userName)
    cAssertThat(processInstance)
      .isActive
      .isWaitingAt("task1-000")
    assertThat(camunda.repositoryService.createProcessDefinitionQuery().list()).hasSize(1)

    val processVersion1 = repository.loadByProcessDefinitionId(processInstance.processDefinitionId)

    val processVersion2 = service.deployNextVersion(processVersion1)

    println(processVersion2.bpmnXml)

    assertThat(camunda.repositoryService.createProcessDefinitionQuery().list()).hasSize(2)

    camunda.runtimeService.newMigration(camunda.runtimeService.createMigrationPlan(
      processVersion1.processDefinitionId,
      processVersion2.processDefinitionId
    ).mapEqualActivities()
      .build())
      .processInstanceIds(processInstance.processInstanceId)
      .execute()

    assertThat(camunda.runtimeService
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.processInstanceId)
      .singleResult().processDefinitionId)
      .isEqualTo(processVersion2.processDefinitionId)

    cAssertThat(processInstance).isActive.isWaitingAt("task1-000")

    complete(task())
    cAssertThat(processInstance).isActive.isWaitingAt("task2-001")
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
    assertThat(process.tasks.first().id).isEqualTo("task1")
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter-20201116.bpmn")
    assertThat(process.processDefinitionId).isNull()
  }

  @Test
  @Deployment(resources = ["bpmn/processWorstDay-starter.bpmn"])
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

    val processInstance: ProcessInstance = camunda.runtimeService.createProcessInstanceQuery().processDefinitionKey(WorstDayProcessFixtures.processDefinitionKey).singleResult()
    assertThat(processInstance).isNotNull
    cAssertThat(processInstance).isWaitingAt("task1-000")
  }
}
