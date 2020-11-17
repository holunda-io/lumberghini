package process

import io.holunda.funstuff.lumberghini.process.PerpetuumMobileProcess
import io.holunda.funstuff.lumberghini.test.TestProcessEngineConfiguration
import org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.instance.Task
import org.junit.Before
import org.junit.Rule
import org.junit.Test


class PerpetuumMobileProcessTest {

  @get:Rule
  val camunda = TestProcessEngineConfiguration().rule()

  val process by lazy {
    PerpetuumMobileProcess(camunda.runtimeService, camunda.repositoryService)
  }

  @Before
  fun setUp() {
    BpmnAwareTests.init(camunda.processEngine)
  }

  @Test
  fun `start for user`() {
    val pi = process.start("jan")

    BpmnAwareTests.assertThat(pi.processInstance).isWaitingAt("task-a")


    val mi = pi.getModelInstance()

    println(Bpmn.convertToString(mi))

    val t1 = mi.getModelElementsByType(Task::class.java).first()

    println("task: $t1")
  }
}

