package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.processDefinitionKey
import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import java.time.LocalDate

object WorstDayProcessFixtures {

  object TaskDataConfigurations {

    val milton1 = TaskDataConfiguration(
      name = "This is my task",
      description = "Another intersting Job you must do",
      colleague = TaskDataConfiguration.Colleague.Milton
    )

  }

  const val userName = "peter"

  val day = LocalDate.parse("2020-11-16")
  val daySupplier = { LocalDate.parse("2020-11-16") }

  val processDefinitionKey = processDefinitionKey(userName, day)

  val task1 = WorstDayTask(
    id = "task1",
    name = "Task 1",
    description = "the task one"
  )
  val task2 = WorstDayTask(
    id = "task2",
    name = "Task 2",
    description = "the task two"
  )

  val processWithTask1 = WorstDayProcess(
    day = day,
    userName = userName,
    task = task1
  )

}

