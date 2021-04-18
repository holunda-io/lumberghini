package io.holunda.funstuff.lumberghini.task

import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration.Colleague
import io.holunda.funstuff.lumberghini.test.WorstDayProcessFixtures.TaskDataConfigurations
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test


internal class WorstDayTaskRepositoryTest {

  private val properties = LumberghiniConfigurationProperties(
    tasks = listOf(
      TaskDataConfigurations.milton1,
      TaskDataConfigurations.bill1
    )
  )

  private val repository = WorstDayTaskRepository(properties)

  @Test
  internal fun `has size 2`() {
    assertThat(repository.size()).isEqualTo(2)
  }

  @Test
  internal fun `find by id = 1`() {
    with(repository.findById(1)) {
      assertThat(taskId).isEqualTo(TaskId(id = 1))
      assertThat(name).isEqualTo("This is my task")
      assertThat(description).isEqualTo("Another interesting Job you must do")
      assertThat(context).isEqualTo(Colleague.Milton.name)
    }
  }

  @Test
  internal fun `fails if id is unknown`() {
    assertThatThrownBy { repository.findById(17) }
      .isInstanceOf(IllegalArgumentException::class.java)
      .hasMessage("task with id=17 does not exist")
  }

  @Test
  internal fun `find all contains tasks from properties`() {
    val all = repository.findAll()
    assertThat(all).hasSize(2)
    assertThat(all.none { it.inUse }).isTrue


    with(all[0]) {
      assertThat(taskId.id).isEqualTo(1)
      assertThat(name).isEqualTo("This is my task")
      assertThat(description).isEqualTo("Another interesting Job you must do")
      assertThat(context).isEqualTo(Colleague.Milton.name)
    }
    with(all[1]) {
      assertThat(taskId.id).isEqualTo(2)
      assertThat(name).isEqualTo("Where are the TPS reports")
      assertThat(description).isEqualTo("Come in on Saturday")
      assertThat(context).isEqualTo(Colleague.Bill.name)
    }

  }
}
