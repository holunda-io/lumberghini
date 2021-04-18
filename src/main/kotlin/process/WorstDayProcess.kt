package io.holunda.funstuff.lumberghini.process

import io.holunda.camunda.bpm.data.CamundaBpmData.stringVariable
import io.holunda.funstuff.lumberghini.process.fn.BpmnModelInstanceConverter.createBpmnModelInstance
import io.holunda.funstuff.lumberghini.process.fn.BpmnModelInstanceConverter.processDefinitionKey
import io.holunda.funstuff.lumberghini.process.fn.BpmnModelInstanceConverter.tasks
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.task.WorstDayTasks
import mu.KLogging
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * The lumbergh process, consisting of one..many useless user tasks.
 *
 * Each process is unique for a given date and user.
 */
data class WorstDayProcess (
  /**
   * which day is your worst?
   */
  val day: LocalDate = LocalDate.now(),

  /**
   * What is your name, poor fellow?
   */
  val userName: String,

  /**
   * What are the tasks you have to fulfill?
   */
  val tasks: WorstDayTasks = WorstDayTasks.EMPTY,

  /**
   * The processDefinitionId this process gets, once it was deployed.
   */
  val processDefinitionId: String? = null
) {
  companion object : KLogging() {
    const val PREFIX = "processWorstDay"
    const val DESCRIPTION = "So, I was sitting in my cubicle today and I realized, " +
      "ever since I started working, every single day of my life has been worse than the day before it. " +
      "So, that means that every single day that you see me, that’s on the worst day of my life."

    object VARIABLES {
      val processInstanceId = stringVariable("processInstanceId")
      val userName = stringVariable("userName")
      val day = stringVariable("day")
    }

    object ELEMENTS {
      const val EVENT_START = "startEvent"
      const val EVENT_END = "endEvent"
    }

    /**
     * A simple "2020116" date formatter.
     */
    val datePattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * Parse a given [BpmnModelInstance] and create a [WorstDayProcess] from it.
     * If the process already is deployed, its processDefinitionId is taken into account.
     */
    fun readFromModelInstance(bpmn: BpmnModelInstance, definitionId: String? = null): WorstDayProcess {
      // parse userName and day from processDefinitionKey
      val (_, userName, day) = bpmn.processDefinitionKey().split("-", limit = 3)

      val tasks = bpmn.tasks()

      return WorstDayProcess(
        day = LocalDate.parse(day, datePattern),
        userName = userName,
        tasks = tasks,
        processDefinitionId = definitionId
      )
    }

    /**
     * Helper function to determine the processDefinitionKey based on  current user and current day.
     */
    fun processDefinitionKey(userName: String, day: LocalDate) = "$PREFIX-$userName-${day.format(datePattern)}"
  }

  init {
    require(tasks.none { !it.inUse }) { "tasks in a process must have count > 0, please use factory method instead of constructor." }
  }

  /**
   * This process version, based on number of UserTasks already added.
   */
  val version = tasks.size

  /**
   * Highest [TaskId#count] of task with given Id.
   * Used to create the taskId of a task to be added.
   */
  fun maxTaskCount(id: Int) = tasks.maxTaskCount(id)

  /**
   * This process' definition key, based on userName and day.
   */
  val processDefinitionKey = processDefinitionKey(userName, day)

  /**
   * This process' resource name (`processDefinitionKey.bpmn`)
   */
  val processResourceName = "$processDefinitionKey.bpmn"

  /**
   * This process' display name.
   */
  val processName = "Worst Day in the life of $userName ($day)"

  /**
   * Create the [BpmnModelInstance] based on the tasks of this process.
   */
  val bpmnModelInstance: BpmnModelInstance by lazy {
    createBpmnModelInstance(this)
  }

  /**
   * Takes the [bpmnModelInstance] and converts it to bpmn-xml.
   */
  val bpmnXml: String by lazy {
    Bpmn.convertToString(bpmnModelInstance)
  }

  /**
   * Creates a copy of this instances with the newTask added.
   */
  fun addTask(newTask: WorstDayTask): WorstDayProcess = copy(tasks = tasks.add(newTask))

}

