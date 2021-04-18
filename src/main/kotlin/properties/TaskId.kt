package io.holunda.funstuff.lumberghini.properties

import io.holunda.funstuff.lumberghini.TaskDefinitionKey

/**
 * Wrapper class that creates a [TaskDefinitionKey] using the id (index of task in [TaskDataConfiguration]
 * and the count (usage of the same task configuration in this process so far).
 *
 * The result is a taskDefinitionKey which is unique for the current process instance.
 */
data class TaskId(val prefix: String = "task", val id: Int, val count: Int = 0) {
  companion object {
    fun from(taskId: String) = taskId.split("-", limit = 3).let {
      TaskId(it[0], it[1].toInt(), it[2].toInt())
    }

    private fun Int.padStart(length: Int = 3, char: Char = '0') = toString().padStart(length, char)
  }

  /**
   * Derives a definition key based on id and count, so it is unique in a process even if the task repeats.
   */
  val taskDefinitionKey: TaskDefinitionKey = "$prefix-${id.padStart()}-${count.padStart(length = 2)}"

  fun withCount(count: Int): TaskId = if (count == this.count) {
    this
  } else {
    require(count > this.count) { "new count=$count has to be higher than last=${this.count}" }
    copy(count = count)
  }
}
