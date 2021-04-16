package io.holunda.funstuff.lumberghini.task

import org.valiktor.functions.hasSize
import org.valiktor.validate

/**
 * Task data read from `config/tasks.yml`
 */
data class TaskDataConfiguration(
  val name: String,
  val description: String,
  val colleague: Colleague
) {

  init {
    validate(this) {
      validate(TaskDataConfiguration::name).hasSize(3,100)
    }
  }

  /**
   * Your nice colleagues.
   */
  enum class Colleague {
    Bill,
    Milton,
    Michael,
    Dom
  }
}
