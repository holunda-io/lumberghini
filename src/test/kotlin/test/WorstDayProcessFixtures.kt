package io.holunda.funstuff.lumberghini.test

import io.holunda.funstuff.lumberghini.process.FindNextTaskStrategy
import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import org.assertj.core.api.Assertions.assertThat
import org.camunda.bpm.engine.test.ProcessEngineRule
import process.WorstDayTask
import java.time.LocalDate

object WorstDayProcessFixtures {

  val userName = "peter"

  val day = LocalDate.parse("2020-11-16")
  val daySupplier = { LocalDate.parse("2020-11-16") }

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

  object Camunda {

    fun ProcessEngineRule.noProcessesDeployed() = assertThat(this.repositoryService.createProcessDefinitionQuery().list()).isEmpty()

  }
}

