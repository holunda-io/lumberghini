package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.processDefinitionKey
import io.holunda.funstuff.lumberghini.properties.TaskId
import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import java.time.LocalDate

object WorstDayProcessFixtures {

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
  }

  const val userName = "peter"

  val day = LocalDate.parse("2020-11-16")
  val daySupplier = { LocalDate.parse("2020-11-16") }

  val processDefinitionKey = processDefinitionKey(userName, day)

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

  fun processWithTasks(vararg tasks: WorstDayTask) = WorstDayProcess(
    day = day,
    userName = userName,
    tasks = tasks.toList()
  )
}
