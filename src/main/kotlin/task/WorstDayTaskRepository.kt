package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import org.springframework.stereotype.Component
import java.lang.IllegalArgumentException

/**
 * Read only Repository that holds the [WorstDayTask]s configured via
 * [LumberghiniConfigurationProperties#tasks].
 */
@Component
class WorstDayTaskRepository(
  configurationProperties: LumberghiniConfigurationProperties
) {

  private val store: Map<Int, WorstDayTask> = configurationProperties.tasks
    .mapIndexed { index, taskData ->
      (index + 1).let {
        it to WorstDayTask(it, taskData)
      }
    }.toMap()

  /**
   * Return [WorstDayTask] by id.
   */
  fun findById(id:Int) = store[id]?: throw IllegalArgumentException("task with id=$id does not exist")

  /**
   * Return all [WorstDayTask]s.
   */
  fun findAll() : WorstDayTasks = store.values.sortedBy { it.taskId.id }.let { WorstDayTasks(it) }

  /**
   * Returns number of configured tasks.
   */
  fun size() = store.size
}
