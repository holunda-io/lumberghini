package io.holunda.funstuff.lumberghini.properties

import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

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
