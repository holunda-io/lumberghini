package io.holunda.funstuff.lumberghini.properties

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

/**
 * Reads `resources/config/tasks.yml` to [TaskDataConfiguration].
 */
@ConfigurationProperties(prefix = "lumberghini")
@ConstructorBinding
data class LumberghiniConfigurationProperties(
  val tasks: List<TaskDataConfiguration>
) {
  init {
    require(tasks.isNotEmpty() && (1..100).contains(tasks.size)) { "you must configure a list " +
      "of taskDataConfigurations, at least 3 and at most 100 elements long" }
  }
}
