package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import java.util.concurrent.atomic.AtomicInteger

/**
 * Strategy Pattern that determines which next tasks gets added to the current process.
 */
interface FindNextTaskStrategy {
  companion object {
    /**
     * A default fallback strategy that just keeps adding numerated tasks. Might be moved to test-scope.
     */
    fun countingNextTaskStrategy() = object : FindNextTaskStrategy {
      private val count = AtomicInteger(1)
      override fun next(previousTasks: List<WorstDayTask>): WorstDayTask {
        val index = count.getAndIncrement()
        return WorstDayTask(id = "task$index", name = "Task $index", description = "this is task number $index")
      }
    }
  }

  /**
   * Find the next task to choose. The list of already existing tasks can be passed to avoid repetition.
   * If this information is considered for the choice of the next task is up to the concrete implementation.
   *
   * @param previousTasks - the tasks already existing in a process.
   * @return the next task to be added to the process
   */
  fun next(previousTasks: List<WorstDayTask> = emptyList()) : WorstDayTask

  /**
   * Uses [FindNextTaskStrategy#next] to determine the next task to add, modifies the process and returns it for deployment/migration.
   *
   * @param process the current process
   * @return modified process with task [FindNextTaskStrategy#next] added.
   */
  fun nextVersion(process: WorstDayProcess) : WorstDayProcess = process.addTask(next(process.tasks))
}
