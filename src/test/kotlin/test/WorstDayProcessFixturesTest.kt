package io.holunda.funstuff.lumberghini.test

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class WorstDayProcessFixturesTest {

  @Test
  internal fun name() {
    assertThat(WorstDayProcessFixtures.task1.taskId.id).isEqualTo(1)
  }
}
