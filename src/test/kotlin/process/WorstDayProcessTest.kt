package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import process.WorstDayTask

class WorstDayProcessTest {

  @Test
  fun `fail with empty task list`() {
    assertThatThrownBy { WorstDayProcess(day = WorstDayProcessFixtures.day, userName = WorstDayProcessFixtures.userName, tasks = emptyList()) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("the process needs at least one user task!")
  }

  @Test
  fun `create with single task`() {
    val process = WorstDayProcess(
      day = WorstDayProcessFixtures.day,
      userName = WorstDayProcessFixtures.userName,
      task = WorstDayProcessFixtures.task1.withIndex(100)
    )

    assertThat(process.tasks).hasSize(1)
    assertThat(process.tasks.first().index).isEqualTo(0)
    assertThat(process.tasks.first().taskDefinitionKey).isEqualTo("task1-000")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.processResourceName).isEqualTo("processWorstDay-peter-20201116.bpmn")
    assertThat(process.processName).isEqualTo("Worst Day in the life of peter (2020-11-16)")

  }

  @Test
  fun `add second task`() {
    var process = WorstDayProcess(day = WorstDayProcessFixtures.day, userName = WorstDayProcessFixtures.userName, task = WorstDayProcessFixtures.task2)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().index).isEqualTo(0)

    // add a task
    process = process.addTask(WorstDayProcessFixtures.task1)

    assertThat(process.version).isEqualTo(2)

    assertThat(process.tasks).containsExactly(
      WorstDayProcessFixtures.task2.withIndex(0),
      WorstDayProcessFixtures.task1.withIndex(1)
    )

    // read modified process
    process = WorstDayProcess.readFromModelInstance(process.bpmnModelInstance)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(2)
    assertThat(process.tasks).containsExactly(
      WorstDayProcessFixtures.task2.withIndex(0),
      WorstDayProcessFixtures.task1.withIndex(1)
    )
  }

  @Test
  fun `read from bpmn`() {
    val bpmn = WorstDayProcessFixtures.processWithTask1.bpmnModelInstance

    val next = WorstDayProcess.readFromModelInstance(bpmn)

    assertThat(next.day).isEqualTo(WorstDayProcessFixtures.day)
    assertThat(next.userName).isEqualTo(WorstDayProcessFixtures.userName)
    assertThat(next.tasks).hasSize(1)

    val task = next.tasks.first()
    assertThat(task.description).isEqualTo(WorstDayProcessFixtures.task1.description)
    assertThat(task.name).isEqualTo(WorstDayProcessFixtures.task1.name)
    assertThat(task.index).isEqualTo(0)
    assertThat(task.id).isEqualTo(WorstDayProcessFixtures.task1.id)
    assertThat(task.taskDefinitionKey).isEqualTo("task1-000")
  }

  @Test
  fun `task index`() = with(WorstDayProcessFixtures.task1) {
    assertThat(id).isEqualTo("task1")
    assertThat(name).isEqualTo("Task 1")
    assertThat(description).isEqualTo("the task one")
    assertThat(index).isEqualTo(0)
    assertThat(taskDefinitionKey).isEqualTo("task1-000")
  }.let { Unit }

  @Test
  fun `task declaration`() {
    val t1 = WorstDayTask("task-1", "T1", "ttt111")
      .withIndex(10)

    assertThat(t1.taskDefinitionKey).isEqualTo("task-1-010")

    assertThat(WorstDayTask(
      taskDefinitionKey = t1.taskDefinitionKey,
      name = t1.name,
      description = t1.description)
    ).isEqualTo(t1)
  }
}
