package io.holunda.funstuff.lumberghini.strategy

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.task.WorstDayTasks

/**
 * Strategy Pattern that determines which next tasks gets added to the current process.
 */
interface FindNextTaskStrategy {

  /**
   * Find the first task to execute.
   */
  fun first() : WorstDayTask

  /**
   * Find the next task to choose. The list of already existing tasks can be passed to avoid repetition.
   * If this information is considered for the choice of the next task is up to the concrete implementation.
   *
   * @param previousTasks - the tasks already existing in a process.
   * @return the next task to be added to the process
   */
  fun next(previousTasks: WorstDayTasks): WorstDayTask

  /**
   * Uses [FindNextTaskStrategy#next] to determine the next task to add, modifies the process and returns it for deployment/migration.
   *
   * @param process the current process
   * @return modified process with task [FindNextTaskStrategy#next] added.
   */
  fun nextVersion(process: WorstDayProcess): WorstDayProcess = process.addTask(next(process.tasks))
}
