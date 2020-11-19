package io.holunda.funstuff.lumberghini.task

/**
 * A user task in the process, defined by id, name and description.
 */
data class WorstDayTask(
  val id: String,
  val name: String,
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
  }

  init {
    require(index >= 0) { "a tasks index must be >=0" }
    require(index < 1000) { "a tasks index must be < 1000" }
  }

  fun withIndex(index: Int) = copy(index = index)

  private val indexFormat = "$index".padStart(3, '0')

  val taskDefinitionKey = "$id-$indexFormat"
}
