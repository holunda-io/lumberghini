package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.TaskDataConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WorstDayNextTaskRandomStrategyTest {

  private val first: (List<WorstDayTask>) -> WorstDayTask = { it.first() }

  @Test
  internal fun `select first - would be random but here it is the first entry`() {
    val (repository, strategy) = init(
      TaskDataConfigurations.milton1,
      TaskDataConfigurations.bill1,
      TaskDataConfigurations.milton2,
      TaskDataConfigurations.bill2,
    )

    assertThat(strategy.first().name).isEqualTo(TaskDataConfigurations.milton1.name)
  }

  @Test
  internal fun `follow up task is different from task 1`() {
    val (repository, strategy) = init(
      TaskDataConfigurations.milton1,
      TaskDataConfigurations.bill1
    )
    val previous = WorstDayTasks(repository.findById(1))

    assertThat(strategy.next(previous).taskId.id).isEqualTo(2)
  }

  @Test
  internal fun `single task is repeated immediately`() {
    val (repository, strategy) = init(
      TaskDataConfigurations.milton1
    )
    val previous = WorstDayTasks(repository.findById(1))

    assertThat(strategy.next(previous)).isEqualTo(previous[0])
  }



  private fun init(vararg data: TaskDataConfiguration): Pair<WorstDayTaskRepository, WorstDayNextTaskRandomStrategy> {
    val properties = LumberghiniConfigurationProperties(
      tasks = listOf(*data)
    )

    val repository = WorstDayTaskRepository(properties)

    val strategy = WorstDayNextTaskRandomStrategy(repository, first)

    return repository to strategy
  }
}
