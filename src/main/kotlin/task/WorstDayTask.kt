package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.LumberghiniConfigurationProperties

/**
 * A user task in the process, defined by id, name and description.
 */
data class WorstDayTask(
  val id: String,
  val name: String,
  val context: String? = null,
  val description: String,
  val index: Int = 0
) {
  companion object {
    operator fun invoke(taskDefinitionKey: String, name: String, description: String) = WorstDayTask(
            id = taskDefinitionKey.substringBeforeLast("-"),
            name = name,
            description = description,
            index = taskDefinitionKey.substringAfterLast("-").toInt()
    )

    operator fun invoke(taskData: TaskDataConfiguration) = WorstDayTask(
      id = taskData.id,
      name = taskData.name,
      description = taskData.description,
      context = taskData.colleague.name
    )
  }

  init {
    require(index >= 0) { "a tasks index must be >=0" }
    require(index < 1000) { "a tasks index must be < 1000" }
  }

  fun withIndex(index: Int) = copy(index = index)

  private val indexFormat = "$index".padStart(3, '0')

  val taskDefinitionKey = "$id-$indexFormat"
}
