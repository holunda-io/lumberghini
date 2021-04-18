package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.TaskId
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

internal class WorstDayTasksTest {

  @Test
  internal fun `list with task1`() {
    val list = WorstDayTasks.EMPTY.add(WorstDayProcessFixtures.task1)

    assertThat(list).hasSize(1)
    assertThat(list[0].taskId.id).isEqualTo(1)
    assertThat(list[0].taskId.count).isEqualTo(1)
  }

  @Test
  internal fun `max task count exiting`() {
    assertThat(WorstDayTasks.EMPTY.add(WorstDayProcessFixtures.task1).maxTaskCount(1)).isEqualTo(1)
  }

  @Test
  internal fun `max task count not exiting`() {
    assertThat(WorstDayTasks(WorstDayProcessFixtures.task1).maxTaskCount(17)).isEqualTo(0)
  }

  @Test
  internal fun `add task again`() {
    var tasks = WorstDayTasks.EMPTY.add(WorstDayProcessFixtures.task1)

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
    val tasks = tasks (
      1 to 1,
      1 to 2,
      1 to 3,
      2 to 1,
      2 to 2,
      3 to 1,
      4 to 1
    )

    assertThat(tasks.taskIdAndMaxCount[1]).isEqualTo(3)
    assertThat(tasks.taskIdAndMaxCount[2]).isEqualTo(2)
    assertThat(tasks.taskIdAndMaxCount[3]).isEqualTo(1)
    assertThat(tasks.taskIdAndMaxCount[4]).isEqualTo(1)

    assertThat(tasks.tasksWithLowestCount.map { it.taskId.id })
      .containsExactlyInAnyOrder(3, 4)
  }

  @Test
  internal fun `find latest tasks (highest count)`() {
    val tasks = tasks (
      1 to 1,
      1 to 2,
      1 to 3,
      2 to 1,
      2 to 2,
      3 to 1,
      4 to 1
    )

    val tasksWithHighestCount = tasks.tasksWithHighestCount

    assertThat(tasksWithHighestCount.map { it.taskId.id to it.taskId.count }).containsExactly(
      1 to 3,
      2 to 2,
      3 to 1,
      4 to 1
    )
  }

  private fun tasks(vararg idAndCount : Pair<Int,Int>) = WorstDayTasks(idAndCount.map { task(it.first, it.second) })
  private fun task(id: Int, count: Int) = WorstDayTask(taskId = TaskId(id = id, count = count), name = "foo", description = "desc", context = "")
}
