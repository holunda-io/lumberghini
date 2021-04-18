package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.countingNextTaskStrategy
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FindNextTaskStrategyTest {

  private val strategy = countingNextTaskStrategy()

  private val process = WorstDayProcess(
    day = WorstDayProcessFixtures.day,
    userId = WorstDayProcessFixtures.userId
  ).addTask(strategy.first())

  @Test
  fun `next process iteration`() {
    assertThat(process.version).isEqualTo(1)
    val next = strategy.nextVersion(process)

    assertThat(next.version).isEqualTo(2)
    assertThat(next.tasks.get(1).taskId.id).isEqualTo(2)
  }
}
