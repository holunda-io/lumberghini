package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration.Colleague
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.valiktor.ConstraintViolationException

internal class TaskDataConfigurationTest {

  @Test
  internal fun `convert to worstDayTask`() {
    assertThat(WorstDayProcessFixtures.TaskDataConfigurations.milton1.id)
      .isEqualTo("task-this-is-my-task")
  }

  @Test
  internal fun `id has to be between 3 and 100`() {
    assertThatThrownBy {
      TaskDataConfiguration(
        name = "ab",
        description = "",
        colleague = Colleague.Milton
      )
    }.isInstanceOf(ConstraintViolationException::class.java)
  }
}
