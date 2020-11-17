package io.holunda.funstuff.lumberghini.process

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.LocalDate


class WorstDayProcessTest {

  private val userName = "peter"
  private val day = LocalDate.parse("2020-11-16")
  private val task1 = WorstDayProcess.WorstDayTask(taskDefinitionKey = "task-0", name = "Task 1", description = "the task one")
  private val task2 = WorstDayProcess.WorstDayTask(taskDefinitionKey = "task-1", name = "Task 2", description = "the task two")

  @Test
  fun `fail with empty task list`() {
    assertThatThrownBy { WorstDayProcess(day = day, userName = userName, tasks = emptyList()) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("the process needs at least one user task!")
  }

  @Test
  fun `create with single task`() {
    val process = WorstDayProcess(day = day, userName = userName, tasks = listOf(task1))

    assertThat(process.tasks).hasSize(1)
    assertThat(process.version).isEqualTo(1)
    assertThat(process.dayFormat).isEqualTo("20201116")
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
  }

  @Test
  fun `read from bpmn`() {
    val bpmn = WorstDayProcess(
      day = day,
      userName = userName,
      tasks = listOf(task1)).bpmnModelInstance

    val next = WorstDayProcess(bpmn)

    assertThat(next.tasks).hasSize(1)
    assertThat(next.tasks.get(0)).isEqualTo(task1)

    assertThat(next.day).isEqualTo(day)
    assertThat(next.dayFormat).isEqualTo("20201116")
    assertThat(next.userName).isEqualTo("peter")
  }

  @Test
  fun `task index`() {
    assertThat(task1.index).isEqualTo(0)
    assertThat(task1.taskDefinitionKey).isEqualTo("task-000")
    assertThat(task1.name).isEqualTo("Task 1")
    assertThat(task1.description).isEqualTo("the task one")
  }


  @Test
  fun `task declaration`() {
    val t1 = WorstDayProcess.WorstDayTask("task-1", "T1", "ttt111")
      .withIndex(0)

    assertThat(t1.taskDefinitionKey).isEqualTo("task-1-000")

    assertThat(WorstDayProcess.WorstDayTask(taskDefinitionKey = t1.taskDefinitionKey, name = t1.name, description = t1.description))
      .isEqualTo(t1)
  }
}
