package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.test.TestProcessEngineConfiguration
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.Camunda.noProcessesDeployed
import io.holunda.funstuff.lumberghini.test.manageDeployments
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as cAssertThat

class WorstDayProcessServiceTest {

  @get:Rule
  val camunda = TestProcessEngineConfiguration().rule()

  val service: WorstDayProcessService by lazy {
    WorstDayProcessService(
      repositoryService = camunda.repositoryService,
      runtimeService = camunda.runtimeService,
      findNextTaskStrategy = FindNextTaskStrategy.countingNextTaskStrategy(),
      todaySupplier = WorstDayProcessFixtures.daySupplier
    )
  }

  @Before
  fun setUp() {
    BpmnAwareTests.init(camunda.processEngine)
    // make sure no processes are deployed
    camunda.noProcessesDeployed()
  }

  @After
  fun tearDown() {
    camunda.manageDeployments(service.getDeployments())
  }

  @Test
  fun `create, deploy and start process for user`() {
    val processInstance = service.start(WorstDayProcessFixtures.userName)

    cAssertThat(processInstance.processInstance).isActive().isWaitingAt("task1-000")

    complete(task())
    cAssertThat(processInstance.processInstance).isEnded
  }

  @Test
  fun `deploy next version and migrate`() {
    val processInstance = service.start(WorstDayProcessFixtures.userName)
    cAssertThat(processInstance.processInstance).isActive().isWaitingAt("task1-000")
    assertThat(camunda.repositoryService.createProcessDefinitionQuery().list()).hasSize(1)

    val processVersion1 = camunda.repositoryService.loadWorstDayProcess(processInstance.processDefinitionId)

    val processVersion2 = service.deployNextVersion(processVersion1)

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

    cAssertThat(processInstance.processInstance).isActive().isWaitingAt("task1-000")

    complete(task())
    cAssertThat(processInstance.processInstance).isActive().isWaitingAt("task2-001")
  }

  @Test
  fun `deploy created process`() {
    assertThat(service.getDeployments()).isEmpty()

    var process = service.create(WorstDayProcessFixtures.userName)

    process = service.deploy(process)
    assertThat(process.processDefinitionId).isNotNull()
    assertThat(process.processDefinitionId).startsWith(process.processDefinitionKey)
    assertThat(process.tasks.first().name).isEqualTo("Task 1")

    assertThat(service.getDeployments()).hasSize(1)
  }

  @Test
  fun `create process for peter`() {
    val process = service.create(WorstDayProcessFixtures.userName)
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().id).isEqualTo("task1")
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter-20201116.bpmn")
    assertThat(process.processDefinitionId).isNull()
  }
}
