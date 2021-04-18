package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FindNextTaskStrategyTest {

  private val strategy = FindNextTaskStrategy.countingNextTaskStrategy()

  private val process = WorstDayProcess(
    day = WorstDayProcessFixtures.day,
    userName = WorstDayProcessFixtures.userName
  ).addTask(strategy.first())

  @Test
  fun `next process iteration`() {
    assertThat(process.version).isEqualTo(1)
    val next = strategy.nextVersion(process)

    assertThat(next.version).isEqualTo(2)
    assertThat(next.tasks.get(1).taskId.id).isEqualTo(2)
  }
}
