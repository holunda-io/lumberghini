package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.task.TaskId
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.processWithTasks
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.setCount
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.Test

class WorstDayProcessTest {


  @Test
  fun `max task count=0 if task not exists`() {
    val process = WorstDayProcessFixtures.processWithTask1
    assertThat(process.maxTaskCount(1)).isEqualTo(1)

    assertThat(process.maxTaskCount(2)).isEqualTo(0)
  }

  @Test
  fun `create with single task`() {
    val process = processWithTasks(WorstDayProcessFixtures.task1)

    Bpmn.validateModel(process.bpmnModelInstance)
    assertThat(process.tasks).hasSize(1)
    assertThat(process.tasks.first().count).isEqualTo(1)
    assertThat(process.tasks.first().taskDefinitionKey).isEqualTo("task-001-01")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter_gibbons-20201116")
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter_gibbons-20201116.bpmn")
    assertThat(process.processName).isEqualTo("Worst Day in the life of peter_gibbons (2020-11-16)")

  }

  @Test
  fun `add second task`() {
    var process = processWithTasks(WorstDayProcessFixtures.task2)

    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter_gibbons-20201116")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().taskId).isEqualTo(TaskId(id = 2, count = 1))

    // add a task
    assertThat(WorstDayProcessFixtures.task1.taskId).isEqualTo(TaskId(id = 1, count = 0))

    process = process.addTask(WorstDayProcessFixtures.task1)

    assertThat(process.maxTaskCount(1)).isEqualTo(1)

    Bpmn.validateModel(process.bpmnModelInstance)
    assertThat(process.version).isEqualTo(2)

    assertThat(process.tasks).containsExactly(
      WorstDayProcessFixtures.task2.setCount(1),
      WorstDayProcessFixtures.task1.setCount(1),
    )

    // read modified process
    process = WorstDayProcess.readFromModelInstance(process.bpmnModelInstance)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter_gibbons-20201116")
    assertThat(process.version).isEqualTo(2)
    assertThat(process.tasks).containsExactlyInAnyOrder(
      WorstDayProcessFixtures.task2.setCount(1),
      WorstDayProcessFixtures.task1.setCount(1)
    )

    //println(process.bpmnXml)
  }

  @Test
  fun `read from bpmn`() {
    val bpmn = WorstDayProcessFixtures.processWithTask1.bpmnModelInstance

    val next = WorstDayProcess.readFromModelInstance(bpmn)

    assertThat(next.day).isEqualTo(WorstDayProcessFixtures.day)
    assertThat(next.userId).isEqualTo(WorstDayProcessFixtures.userId)
    assertThat(next.tasks).hasSize(1)

    with(next.tasks.first()) {
      assertThat(description).isEqualTo(WorstDayProcessFixtures.task1.description)
      assertThat(name).isEqualTo(WorstDayProcessFixtures.task1.name)
      assertThat(count).isEqualTo(1)
      assertThat(taskId).isEqualTo(TaskId(id = 1, count = 1))
      assertThat(taskDefinitionKey).isEqualTo("task-001-01")
    }
  }

  @Test
  fun `task index`() = with(WorstDayProcessFixtures.task1) {
    assertThat(taskDefinitionKey).isEqualTo("task-001-00")
    assertThat(inUse).isFalse
    assertThat(name).isEqualTo("Task 1")
    assertThat(description).isEqualTo("the task one")
  }.let { }

  @Test
  fun `task declaration`() = with(
    WorstDayTask(taskId = TaskId(id = 1), name = "T1", context = "ttt111", description = "task 1")
      .withCount(10)
  ) {

    assertThat(taskDefinitionKey).isEqualTo("task-001-10")
    assertThat(name).isEqualTo("T1")
    assertThat(description).isEqualTo("task 1")

  }.let { }
}
