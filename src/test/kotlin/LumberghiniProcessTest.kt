package io.holunda.funstuff.lumberghini

import com.tngtech.jgiven.Stage
import com.tngtech.jgiven.annotation.ExpectedScenarioState
import com.tngtech.jgiven.annotation.Hidden
import com.tngtech.jgiven.annotation.ProvidedScenarioState
import com.tngtech.jgiven.annotation.ScenarioState
import com.tngtech.jgiven.annotation.ScenarioState.*
import com.tngtech.jgiven.junit.DualScenarioTest
import io.holunda.funstuff.lumberghini.process.support.MigrationProcess
import io.holunda.funstuff.lumberghini.process.support.StarterProcess
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessTestContext
import io.holunda.funstuff.lumberghini.test.WorstDayProcessTestContext.Companion.manageDeployments
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.test.Deployment
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.job
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat as cAssertThat
import org.camunda.bpm.extension.mockito.CamundaMockito
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@Deployment(resources = [StarterProcess.BPMN, MigrationProcess.BPMN])
class LumberghiniProcessTest : DualScenarioTest<LumberghiniProcessTest.GivenWhenStage, LumberghiniProcessTest.ThenStage>() {

  @ProvidedScenarioState
  private val context = WorstDayProcessTestContext()

  @get: Rule
  val camunda = context.rule

  @Before
  fun setUp() {
    BpmnAwareTests.init(camunda.processEngine)
    CamundaMockito.registerInstance(context.starterProcess)
    CamundaMockito.registerInstance(context.migrationProcess)
    CamundaMockito.registerInstance(context.service)

    assertThat(camunda.runtimeService.createProcessInstanceQuery().list()).isEmpty()
  }

  @After
  fun tearDown() {
    camunda.manageDeployments(context.repository.getDeployments())
  }

  @Test
  fun `start process and migrate to second task`() {
    `when`()
      .`start process for user=$`(WorstDayProcessFixtures.userName)

    then()
      .`the starterInstance is ended`()
      .and()
      .`there are $ running instances`(1)
  }

  open class GivenWhenStage : Stage<GivenWhenStage>() {

    @ExpectedScenarioState
    lateinit var context: WorstDayProcessTestContext

    @ProvidedScenarioState(resolution = Resolution.TYPE)
    lateinit var starterInstance: ProcessInstance

    open fun `start process for user=$`(userName: UserName) = self().apply {
      starterInstance = context.starterProcess.start(WorstDayProcessFixtures.userName).also {
        cAssertThat(it).isActive.isWaitingAt(StarterProcess.Companion.ELEMENTS.SERVICE_DEPLOY)
      }
      `executeJob=$`(StarterProcess.Companion.ELEMENTS.SERVICE_DEPLOY)
      `executeJob=$`(StarterProcess.Companion.ELEMENTS.SERVICE_START)
    }

    @Hidden
    open fun `executeJob=$`(activityId: String) = self().apply { execute(job(activityId)) }
  }

  open class ThenStage : Stage<ThenStage>() {
    @ExpectedScenarioState
    lateinit var context: WorstDayProcessTestContext

    @ExpectedScenarioState
    lateinit var starterInstance: ProcessInstance

    open fun `there are $ running instances`(number: Int) = self().apply {
      assertThat(context.repository.findAll()).hasSize(number)
    }

    open fun `the starterInstance is ended`() = self().apply {
      cAssertThat(starterInstance).isEnded
    }
  }
}
