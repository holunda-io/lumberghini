package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.LumberghiniConfigurationProperties
import org.springframework.stereotype.Component

@Component
class WorstDayNextTaskStrategy(
  val properties: LumberghiniConfigurationProperties
) : FindNextTaskStrategy {


  override fun next(previousTasks: List<WorstDayTask>): WorstDayTask {
    TODO("Not yet implemented")
  }
}
