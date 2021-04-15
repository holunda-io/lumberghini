package io.holunda.funstuff.lumberghini.process.fn

import io.holunda.funstuff.lumberghini.process.fn.BpmnModelInstanceConverter.processDefinitionKey
import io.holunda.funstuff.lumberghini.process.fn.BpmnModelInstanceConverter.tasks
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.setCount
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.junit.jupiter.api.Test

internal class BpmnModelInstanceConverterTest {

  @Test
  internal fun `create BPMN model from worstTasks`() {
    val bpmn: BpmnModelInstance = BpmnModelInstanceConverter.createBpmnModelInstance(
      WorstDayProcessFixtures.processWithTask1AndTask2
        .addTask(WorstDayProcessFixtures.task1)
    )

    assertThat(bpmn.processDefinitionKey()).isEqualTo(WorstDayProcessFixtures.processDefinitionKey)

    val tasks = bpmn.tasks()

    assertThat(tasks).containsExactlyInAnyOrder(
      WorstDayProcessFixtures.task1.setCount(1),
      WorstDayProcessFixtures.task1.setCount(2),
      WorstDayProcessFixtures.task2.setCount(1),
    )
  }
}
