package io.holunda.funstuff.lumberghini.process.fn

import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.junit.jupiter.api.Test

internal class BpmnModelInstanceConverterTest {

  @Test
  internal fun `create BPMN model from worstTasks`() {
    val bpmn : BpmnModelInstance = BpmnModelInstanceConverter.createBpmnModelInstance(WorstDayProcessFixtures.processWithTask1AndTask2)


    println(Bpmn.convertToString(bpmn))
  }


}
