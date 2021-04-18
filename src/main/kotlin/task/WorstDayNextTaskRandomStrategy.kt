package io.holunda.funstuff.lumberghini.task

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
  private val random : (List<WorstDayTask>) -> WorstDayTask = { repository.findAll().random() }
) : FindNextTaskStrategy {

  /**
   * The first task is elected randomly.
   */
  override fun first(): WorstDayTask = random(repository.findAll())

  /**
   * Follow up tasks are selected based on previous tasks.
   */
  override fun next(previousTasks: WorstDayTasks): WorstDayTask {
    if (repository.size() == 1) {
      return repository.findById(previousTasks[0].taskId.id)
    }

    TODO() // return random()
  }
}
