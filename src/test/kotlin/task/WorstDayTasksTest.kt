package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.TaskId
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class WorstDayTasksTest {

  @Test
  internal fun `must not be empty`() {
    assertThatThrownBy { WorstDayTasks(emptyList()) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("worst day tasks must not be empty.")
  }

  @Test
  internal fun `list with task1`() {
    val list = WorstDayTasks(WorstDayProcessFixtures.task1)

    assertThat(list).hasSize(1)
    assertThat(list[0].taskId.id).isEqualTo(1)
    assertThat(list[0].taskId.count).isEqualTo(1)
  }

  @Test
  internal fun `max task count exiting`() {
    assertThat(WorstDayTasks(WorstDayProcessFixtures.task1).maxTaskCount(1)).isEqualTo(1)
  }

  @Test
  internal fun `max task count not exiting`() {
    assertThat(WorstDayTasks(WorstDayProcessFixtures.task1).maxTaskCount(17)).isEqualTo(0)
  }

  @Test
  internal fun `add task again`() {
    var tasks = WorstDayTasks(WorstDayProcessFixtures.task1)

    assertThat(tasks).hasSize(1)

    tasks = tasks.add(WorstDayProcessFixtures.task1)
    assertThat(tasks).hasSize(2)

    assertThat(tasks[0].taskId.id).isEqualTo(1)
    assertThat(tasks[0].taskId.count).isEqualTo(1)

    assertThat(tasks[1].taskId.id).isEqualTo(1)
    assertThat(tasks[1].taskId.count).isEqualTo(2)
  }

  @Test
  internal fun `find tasks with lowest count`() {
    fun task(id: Int, count: Int) = WorstDayTask(taskId = TaskId(id = id, count = count), name = "foo", description = "desc", context = "")

    val tasks = WorstDayTasks(
      task(1, 1),
      task(1, 2),
      task(1, 3),
      task(2, 1),
      task(2, 2),
      task(3, 1),
      task(4, 1),
    )

    assertThat(tasks.taskIdAndMaxCount[1]).isEqualTo(3)
    assertThat(tasks.taskIdAndMaxCount[2]).isEqualTo(2)
    assertThat(tasks.taskIdAndMaxCount[3]).isEqualTo(1)
    assertThat(tasks.taskIdAndMaxCount[4]).isEqualTo(1)

    assertThat(tasks.tasksWithLowestCount.map { it.taskId.id })
      .containsExactlyInAnyOrder(3, 4)
  }
}
