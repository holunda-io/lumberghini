package io.holunda.funstuff.lumberghini.process

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import java.time.LocalDate

class WorstDayProcessTest {

  private val userName = "peter"
  private val day = LocalDate.parse("2020-11-16")
  private val task1 = WorstDayProcess.WorstDayTask(id = "task1", name = "Task 1", description = "the task one")
  private val task2 = WorstDayProcess.WorstDayTask(id = "task2", name = "Task 2", description = "the task two")

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
  fun `add second task`() {
    var process = WorstDayProcess(day = day, userName = userName, tasks = listOf(task2))
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(1)
    assertThat(process.tasks.first().index).isEqualTo(0)

    // add a task
    process = process.addTask(task1)
    assertThat(process.version).isEqualTo(2)

    assertThat(process.tasks).containsExactly(
      task2.withIndex(0),
      task1.withIndex(1)
    )

    // read modified process
    process = WorstDayProcess(process.bpmnModelInstance)
    assertThat(process.processDefinitionKey).isEqualTo("processWorstDay-peter-20201116")
    assertThat(process.version).isEqualTo(2)
    assertThat(process.tasks).containsExactly(
      task2.withIndex(0),
      task1.withIndex(1)
    )
  }

  @Test
  fun `read from bpmn`() {
    val bpmn = WorstDayProcess(
      day = day,
      userName = userName,
      tasks = listOf(task1)).bpmnModelInstance


    val next = WorstDayProcess(bpmn)

    assertThat(next.day).isEqualTo(day)
    assertThat(next.dayFormat).isEqualTo("20201116")
    assertThat(next.userName).isEqualTo("peter")
    assertThat(next.tasks).hasSize(1)

    val task = next.tasks.first()
    assertThat(task.description).isEqualTo(task1.description)
    assertThat(task.name).isEqualTo(task1.name)
    assertThat(task.index).isEqualTo(0)
    assertThat(task.id).isEqualTo(task1.id)
    assertThat(task.taskDefinitionKey).isEqualTo("task1-000")


  }

  @Test
  fun `task index`() {
    assertThat(task1.id).isEqualTo("task1")
    assertThat(task1.name).isEqualTo("Task 1")
    assertThat(task1.description).isEqualTo("the task one")
    assertThat(task1.index).isEqualTo(0)
    assertThat(task1.taskDefinitionKey).isEqualTo("task1-000")
  }

  @Test
  fun `task declaration`() {
    val t1 = WorstDayProcess.WorstDayTask("task-1", "T1", "ttt111")
      .withIndex(10)

    assertThat(t1.taskDefinitionKey).isEqualTo("task-1-010")

    assertThat(WorstDayProcess.WorstDayTask(
      taskDefinitionKey = t1.taskDefinitionKey,
      name = t1.name,
      description = t1.description)
    ).isEqualTo(t1)
  }
}
