package io.holunda.funstuff.lumberghini.task

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

class TaskIdTest {

  @Test
  internal fun `create task id from index`() {
    assertThat(TaskId(id = 1).taskDefinitionKey)
      .isEqualTo("task-001-00")
  }

  @Test
  internal fun `create task id full`() {
    assertThat(TaskId(prefix = "foo", id = 91, count = 7).taskDefinitionKey)
      .isEqualTo("foo-091-07")
  }

  @Test
  internal fun `copy with count`() {
    val taskId = TaskId(id = 1).withCount(17)
    assertThat(taskId.taskDefinitionKey).isEqualTo("task-001-17")
  }

  @Test
  internal fun `parse from string`() {
    val taskId = TaskId.from("hello-067-23")

    assertThat(taskId.prefix).isEqualTo("hello")
    assertThat(taskId.id).isEqualTo(67)
    assertThat(taskId.count).isEqualTo(23)
  }

  @Test
  internal fun `just return when count is unchanged`() {
    val taskId = TaskId.from("hello-067-23")
    assertThat(taskId.count).isEqualTo(23)

    val newTaskId = taskId.withCount(23)
    assertThat(newTaskId).isSameAs(taskId)
  }

  @Test
  internal fun `with count must be higher`() {
    val taskId = TaskId.from("hello-067-23")
    assertThatThrownBy { taskId.withCount(22) }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  internal fun `set new count`() {
    assertThat(TaskId.from("hello-067-23").withCount(25).count)
      .isEqualTo(25)
  }
}
