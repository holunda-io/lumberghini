package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.properties.TaskId
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.processWithTasks
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.camunda.bpm.model.bpmn.Bpmn
import org.junit.Test

class WorstDayProcessTest {

  @Test
  fun `fail with empty task list`() {
    assertThatThrownBy { WorstDayProcess(day = WorstDayProcessFixtures.day, userName = WorstDayProcessFixtures.userName, tasks = emptyList()) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("the process needs at least one user task!")
  }

  @Test
  fun `max task count=0 if task not exists`() {
    val process = WorstDayProcessFixtures.processWithTask1
    assertThat(process.maxTaskCount(1)).isEqualTo(1)

    assertThat(process.maxTaskCount(2)).isEqualTo(0)
  }

  @Test
  fun `create with single task`() {
    val process = WorstDayProcess(
      day = WorstDayProcessFixtures.day,
      userName = WorstDayProcessFixtures.userName,
      task = WorstDayProcessFixtures.task1.withCount(10)
    )

    Bpmn.validateModel(process.bpmnModelInstance)
    assertThat(process.tasks).hasSize(1)
    assertThat(process.tasks.first().count).isEqualTo(10)
    assertThat(process.tasks.first().taskDefinitionKey).isEqualTo("task-001-10")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter-20201116.bpmn")
    assertThat(process.processName).isEqualTo("Worst Day in the life of peter (2020-11-16)")

  }

  @Test
  fun `add second task`() {
    var process = processWithTasks(WorstDayProcessFixtures.task2)

    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().taskId.id).isEqualTo(2)
    assertThat(process.tasks.first().count).isEqualTo(1)

    // add a task
    assertThat(WorstDayProcessFixtures.task1.taskId.id).isEqualTo(1)
    assertThat(WorstDayProcessFixtures.task1.taskId.count).isEqualTo(1)

    process = process.addTask(WorstDayProcessFixtures.task1)

    assertThat(process.maxTaskCount(1)).isEqualTo(1)

    Bpmn.validateModel(process.bpmnModelInstance)
    assertThat(process.version).isEqualTo(2)

    assertThat(process.tasks).containsExactly(
      WorstDayProcessFixtures.task2.copy(taskId = WorstDayProcessFixtures.task2.taskId.copy(count = 1)),
      WorstDayProcessFixtures.task1.copy(taskId = WorstDayProcessFixtures.task1.taskId.copy(count = 1)),
    )

    // read modified process
    process = WorstDayProcess.readFromModelInstance(process.bpmnModelInstance)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(2)
    assertThat(process.tasks).containsExactlyInAnyOrder(
      WorstDayProcessFixtures.task2.withCount(1),
      WorstDayProcessFixtures.task1.withCount(1)
    )

    println(process.bpmnXml)
  }

  @Test
  fun `read from bpmn`() {
    val bpmn = WorstDayProcessFixtures.processWithTask1.bpmnModelInstance

    val next = WorstDayProcess.readFromModelInstance(bpmn)

    assertThat(next.day).isEqualTo(WorstDayProcessFixtures.day)
    assertThat(next.userName).isEqualTo(WorstDayProcessFixtures.userName)
    assertThat(next.tasks).hasSize(1)

    with(next.tasks.first()) {
      assertThat(description).isEqualTo(WorstDayProcessFixtures.task1.description)
      assertThat(name).isEqualTo(WorstDayProcessFixtures.task1.name)
      assertThat(count).isEqualTo(1)
      assertThat(taskId).isEqualTo(WorstDayProcessFixtures.task1.taskId)
      assertThat(taskDefinitionKey).isEqualTo("task-001-01")
    }
  }

  @Test
  fun `task index`() = with(WorstDayProcessFixtures.task1) {
    assertThat(taskDefinitionKey).isEqualTo("task-001-01")
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
