package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.TaskId


/**
 * A user task in the process, defined by id, name and description.
 */
data class WorstDayTask(
  val taskId: TaskId,
  val name: String,
  val context: String? = null,
  val description: String
) {
  companion object {
    fun from(list: List<TaskDataConfiguration>): List<WorstDayTask> = list.mapIndexed { id, taskData -> WorstDayTask(id + 1, taskData) }

    operator fun invoke(id: Int, taskData: TaskDataConfiguration) = WorstDayTask(
      taskId = TaskId(id = id, count = 1),
      name = taskData.name,
      description = taskData.description,
      context = taskData.colleague.name
    )
  }

//  init {
//    require(index >= 0) { "a tasks index must be >=0" }
//    require(index < 1000) { "a tasks index must be < 1000" }
//  }

//  fun withIndex(index: Int) = copy(index = index)

//  private val indexFormat = "$index".padStart(3, '0')


  val taskDefinitionKey = taskId.taskDefinitionKey
  val count = taskId.count

  fun withCount(count: Int) = copy(taskId = taskId.withCount(count))
  fun increaseCount() = copy(taskId = taskId.withCount((taskId.count ?: 1) + 1))
}
