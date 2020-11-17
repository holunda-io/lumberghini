package io.holunda.funstuff.lumberghini.process

import mu.KLogging
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private fun BpmnModelInstance.processDefinitionKey() = this.getModelElementsByType(Process::class.java).first().id
private fun BpmnModelInstance.tasks(): List<WorstDayProcess.WorstDayTask> = this.getModelElementsByType(Task::class.java)
  .mapIndexed { index, task -> task.toWorstDayTask(index) }
  .sortedBy { it.index }
  .toList()

private fun Task.toWorstDayTask(index: Int = -1) = WorstDayProcess.WorstDayTask(
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
  val tasks: List<WorstDayTask>
) {
  companion object : KLogging() {
    private val datePattern = DateTimeFormatter.ofPattern("yyyyMMdd");

    operator fun invoke(day: LocalDate = LocalDate.now(), userName: String, task:WorstDayTask) = WorstDayProcess(
      day = day,
      userName = userName,
      tasks = listOf(task.withIndex(0))
    )

    // secondary constructor: read from BpmnModelInstance.
    operator fun invoke(bpmn: BpmnModelInstance): WorstDayProcess {
      val processDefinitionKey = bpmn.processDefinitionKey()
      val (_, name, date) = processDefinitionKey.split("-")

      val tasks = bpmn.tasks()

      return WorstDayProcess(
        day = LocalDate.parse(date, datePattern),
        userName = name,
        tasks = tasks
      )
    }
  }

  init {
    require(tasks.isNotEmpty()) { "the process needs at least one user task!" }
  }

  val dayFormat = day.toString().replace("-", "")
  val version = tasks.size
  val processDefinitionKey = "processWorstDay-$userName-$dayFormat"
  val processResourceName = "$processDefinitionKey.bpmn"
  val processName = "Worst Day in the life of $userName ($day)"

  val bpmnModelInstance: BpmnModelInstance by lazy {
    val builder = Bpmn.createExecutableProcess(processDefinitionKey)
      .name(processName)
      .camundaVersionTag("${version}")
      .startEvent()

    tasks.forEachIndexed { index, task ->
      builder
        .userTask(task.taskDefinitionKey)
        .name(task.name)
        .documentation(task.description)
    }

    builder.endEvent().done()
  }

  val bpmnXml : String by lazy {
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

  /**
   * A user task in the process, defined by id, name and description.
   */
  data class WorstDayTask(
    val id: String,
    val name: String,
    val description: String,
    val index: Int = 0
  ) {
    companion object {
      operator fun invoke(taskDefinitionKey: String, name: String, description: String) = WorstDayTask(
        id = taskDefinitionKey.substringBeforeLast("-"),
        name = name,
        description = description,
        index = taskDefinitionKey.substringAfterLast("-").toInt()
      )
    }

    init {
      require(index >= 0) { "a tasks index must be >=0" }
      require(index < 1000) { "a tasks index must be < 1000" }
    }

    fun withIndex(index: Int) = copy(index = index)

    private val indexFormat = "$index".padStart(3, '0')

    val taskDefinitionKey = "$id-$indexFormat"
  }
}
