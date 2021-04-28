package io.holunda.funstuff.lumberghini.strategy

import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.task.WorstDayTaskRepository
import io.holunda.funstuff.lumberghini.task.WorstDayTasks
import org.springframework.stereotype.Component

@Component
class WorstDayNextTaskRandomStrategy(
  /**
   * Stores all available tasks.
   */
  private val repository: WorstDayTaskRepository,

  /*+
   * picks a tasks from given list randomly ... this can be overwritten for unit testing
   */
  private val random: (List<WorstDayTask>) -> WorstDayTask = { repository.findAll().random() }
) : FindNextTaskStrategy {

  /**
   * The first task is selected randomly.
   */
  override fun first(): WorstDayTask = repository.findAll().let(random)

  /**
   * Follow up tasks are selected based on previous tasks.
   */
  override fun next(previousTasks: WorstDayTasks): WorstDayTask = WorstDayTasks(repository.findAll()
    .toMutableList().apply { addAll(previousTasks) })
    .tasksWithLowestCount
    .let(random)

}
