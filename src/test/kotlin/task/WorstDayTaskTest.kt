package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WorstDayTaskTest {

  @Test
  internal fun `create from taskDataConfiguration`() {
    val task = WorstDayTask.from(listOf(WorstDayProcessFixtures.TaskDataConfigurations.milton1))[0]

    assertThat(task.taskDefinitionKey).isEqualTo("task-001-00")
    assertThat(task.name).isEqualTo("This is my task")
    assertThat(task.context).isEqualTo("Milton")
    assertThat(task.description).isEqualTo("Another interesting Job you must do")
    assertThat(task.taskId).isEqualTo(TaskId(id=1))
  }

  @Test
  internal fun `create from taskDataConfiguration and increase count`() {
    val task = WorstDayTask.from(listOf(WorstDayProcessFixtures.TaskDataConfigurations.milton1))[0].increaseCount()

    assertThat(task.taskDefinitionKey).isEqualTo("task-001-01")
    assertThat(task.name).isEqualTo("This is my task")
    assertThat(task.context).isEqualTo("Milton")
    assertThat(task.description).isEqualTo("Another interesting Job you must do")
    assertThat(task.count).isEqualTo(1)
  }


}
