package io.holunda.funstuff.lumberghini.task

import org.springframework.stereotype.Component

@Component
class WorstDayNextTaskStrategy(
  private val repository: WorstDayTaskRepository,
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

    TODO("Not yet implemented")
  }
}
