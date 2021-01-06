package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.task.WorstDayTask
import mu.KLogging
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.Task
import org.camunda.bpm.model.bpmn.instance.UserTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter


private fun BpmnModelInstance.processDefinitionKey() = this.getModelElementsByType(Process::class.java).first().id
private fun BpmnModelInstance.tasks(): List<WorstDayTask> = this.getModelElementsByType(Task::class.java)
  .mapIndexed { index, task -> task.toWorstDayTask(index) }
  .sortedBy { it.index }
  .toList()

private fun Task.toWorstDayTask(index: Int = -1) = WorstDayTask(
  id = this.id.substringBeforeLast("-"),
  name = this.name,
  description = this.documentations.first().rawTextContent,
  index = index
)

/**
 * The lumbergh process, consisting of one..many useless user tasks.
 *
 * Each process is unique for a given date and user.
 */
data class WorstDayProcess(
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
  val tasks: List<WorstDayTask>,

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

    /**
     * A simple "2020116" date formatter.
     */
    private val datePattern = DateTimeFormatter.ofPattern("yyyyMMdd")

    /**
     * Parse a given [BpmnModelInstance] and create a [WorstDayProcess] from it.
     * If the process already is deployed, its processDefinitionId is taken into account.
     */
    fun readFromModelInstance(bpmn: BpmnModelInstance, definitionId: String? = null): WorstDayProcess {
      // parse userName and day from processDefinitionKey
      val (_, userName, day) = bpmn.processDefinitionKey().split("-")

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

  /**
   * Creates a new process instance containing only one UserTask.
   */
  constructor(day: LocalDate, userName: String, task: WorstDayTask) : this(
    day = day,
    userName = userName,
    tasks = listOf(task.withIndex(0))
  )

  init {
    require(tasks.isNotEmpty()) { "the process needs at least one user task!" }
  }

  /**
   * This process version, based on numer of UserTasks already added.
   */
  val version = tasks.size

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
   * Index of the newTask is set to the position in the list,
   * the process version is equal to the list size.
   */
  fun addTask(newTask: WorstDayTask): WorstDayProcess = copy(
    tasks = (tasks + newTask)
      .mapIndexed { index, task -> task.withIndex(index) }
      .toList()
  )
}


fun createBpmnModelInstance(process: WorstDayProcess): BpmnModelInstance = with(process) {
  require(tasks.isNotEmpty())

  // this first creates a [BpmnModelInstance] containing only the startEvent, then loops through all the tasks, and finally adds the endEvent.
  // return
  Bpmn.createExecutableProcess(processDefinitionKey)
    .name(processName)
    .documentation(WorstDayProcess.DESCRIPTION)
    .camundaStartableInTasklist(false)
    .camundaVersionTag("${version}")
    .startEvent("startEvent").name("Started in good mood")
    .done()
    .apply {
      var lastElementId = "startEvent"
      tasks.forEach { task ->
        getModelElementById<FlowNode>(lastElementId).builder()
          .userTask(task.taskDefinitionKey)
          .name(task.name)
          .documentation(task.description)
        // update the lastUserTask id for the next iteration
        lastElementId = task.taskDefinitionKey
      }

      getModelElementById<UserTask>(lastElementId).builder()
        .camundaAsyncAfter()
        .camundaExecutionListenerDelegateExpression(ExecutionListener.EVENTNAME_END, "#{worstDayProcessService.startMigrationListener()}")
        .endEvent("endEvent").name("Reached Beer O'clock")
        .camundaAsyncBefore()
        .camundaExecutionListenerDelegateExpression(ExecutionListener.EVENTNAME_START, "#{worstDayProcessService.throwLumberghInterventionListener()}")
        .camundaFailedJobRetryTimeCycle("R1/PT1M")
        .done()
    }
}
