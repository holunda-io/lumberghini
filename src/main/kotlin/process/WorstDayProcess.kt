package io.holunda.funstuff.lumberghini.process

import mu.KLogging
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val BpmnModelInstance.processDefinitionKey: String get() = this.getModelElementsByType(Process::class.java).first().id
private val BpmnModelInstance.tasks: List<WorstDayProcess.WorstDayTask>
  get() = this.getModelElementsByType(Task::class.java).map { it.toWorstDayTask() }.sortedBy { it.index }.toList()

private fun Task.toWorstDayTask() = WorstDayProcess.WorstDayTask(
  taskDefinitionKey = this.id,
  name = this.name,
  description = this.documentations.first().rawTextContent
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

    // secondary constructor: read from BpmnModelInstance.
    operator fun invoke(bpmn: BpmnModelInstance): WorstDayProcess {
      val processDefinitionKey = bpmn.processDefinitionKey
      val (_, name, date) = processDefinitionKey.split("-")

      val tasks = bpmn.getModelElementsByType(Task::class.java).sortedBy { it.id }.map {
        WorstDayTask(
          taskDefinitionKey = it.id,
          name = it.name,
          description = it.documentations.first().rawTextContent
        )
      }.toList()

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
  val processName = "Worst Day in the life of $userName"

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

  val bpmnXml by lazy {
    Bpmn.convertToString(bpmnModelInstance)
  }


  /**
   * A user task in the process, defined by id, name and description.
   */
  data class WorstDayTask(
    val id: String,
    val name: String,
    val description: String,
    val index: Int = -1
  ) {
    companion object {
      operator fun invoke(taskDefinitionKey: String, name: String, description: String) = WorstDayTask(
        id = taskDefinitionKey.substringBeforeLast("-"),
        name = name,
        description = description,
        index = taskDefinitionKey.substringAfterLast("-").toInt()
      )
    }

    fun withIndex(index: Int) = copy(index = index)

    private val indexFormat = "$index".padStart(3, '0')

    val taskDefinitionKey = "$id-$indexFormat"
  }
}
