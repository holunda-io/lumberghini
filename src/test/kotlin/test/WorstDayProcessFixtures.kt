package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.processDefinitionKey
import io.holunda.funstuff.lumberghini.properties.TaskDataConfiguration
import io.holunda.funstuff.lumberghini.strategy.FindNextTaskStrategy
import io.holunda.funstuff.lumberghini.task.TaskId
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.task.WorstDayTasks
import java.time.LocalDate
import java.util.concurrent.atomic.AtomicInteger

object WorstDayProcessFixtures {

  /**
   * A default fallback strategy that just keeps adding numerated tasks. Might be moved to test-scope.
   */
  fun countingNextTaskStrategy() = object : FindNextTaskStrategy {
    private val count = AtomicInteger(1)

    override fun first(): WorstDayTask = task(count.getAndIncrement())

    override fun next(previousTasks: WorstDayTasks): WorstDayTask = task(count.getAndIncrement())

    private fun task(index: Int) = WorstDayTask(taskId = TaskId(id = index), name = "Task $index", description = "this is task number $index")
  }

  object TaskDataConfigurations {

    val milton1 = TaskDataConfiguration(
      name = "This is my task",
      description = "Another interesting Job you must do",
      colleague = TaskDataConfiguration.Colleague.Milton
    )

    val bill1 = TaskDataConfiguration(
      name = "Where are the TPS reports",
      description = "Come in on Saturday",
      colleague = TaskDataConfiguration.Colleague.Bill
    )

    val milton2 = TaskDataConfiguration(
      name = "I believe you have my stapler.",
      description = "I need my stapler",
      colleague = TaskDataConfiguration.Colleague.Milton
    )

    val bill2 = TaskDataConfiguration(
      name = "Did you get my memo?",
      description = "Well, Did you?",
      colleague = TaskDataConfiguration.Colleague.Bill
    )
  }

  const val userId = "peter_gibbons"
  const val userFirstName = "Peter"
  const val userLastName = "Gibbons"

  val day = LocalDate.parse("2020-11-16")
  val daySupplier = { LocalDate.parse("2020-11-16") }

  val processDefinitionKey = processDefinitionKey(userId, day)

  val task1 = WorstDayTask(
    taskId = TaskId(id = 1),
    name = "Task 1",
    description = "the task one"
  )
  val task2 = WorstDayTask(
    taskId = TaskId(id = 2),
    name = "Task 2",
    description = "the task two"
  )

  val processWithTask1 = processWithTasks(task1)

  val processWithTask1AndTask2 = processWithTasks(task1, task2)

  fun processWithTasks(vararg tasks: WorstDayTask): WorstDayProcess = tasks.fold(
    WorstDayProcess(
      day = day,
      userId = userId
    )
  ) { p, t -> p.addTask(t) }

  fun WorstDayTask.setCount(count: Int) = copy(taskId = this.taskId.copy(count = count))

}
