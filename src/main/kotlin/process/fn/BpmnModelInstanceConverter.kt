package io.holunda.funstuff.lumberghini.process.fn

import io.holunda.funstuff.lumberghini.ProcessDefinitionId
import io.holunda.funstuff.lumberghini.process.WorstDayProcess
import io.holunda.funstuff.lumberghini.process.WorstDayProcess.Companion.ELEMENTS.EVENT_START
import io.holunda.funstuff.lumberghini.properties.TaskId
import io.holunda.funstuff.lumberghini.task.WorstDayTask
import io.holunda.funstuff.lumberghini.task.WorstDayTasks
import org.camunda.bpm.engine.delegate.ExecutionListener
import org.camunda.bpm.model.bpmn.Bpmn
import org.camunda.bpm.model.bpmn.BpmnModelInstance
import org.camunda.bpm.model.bpmn.instance.FlowNode
import org.camunda.bpm.model.bpmn.instance.Process
import org.camunda.bpm.model.bpmn.instance.Task
import org.camunda.bpm.model.bpmn.instance.UserTask

object BpmnModelInstanceConverter {

  fun BpmnModelInstance.processDefinitionKey(): ProcessDefinitionId = this.getModelElementsByType(Process::class.java).first().id

  /**
   * Analyse the given [BpmnModelInstance] and extract the existing [WorstDayTasks].
   */
  fun BpmnModelInstance.tasks(): WorstDayTasks = this.getModelElementsByType(Task::class.java)
    .map { it.toWorstDayTask() }
    .let { WorstDayTasks(it) }

  private fun Task.toWorstDayTask() = WorstDayTask(
    taskId = TaskId.from(this.id),
    name = this.name,
    description = this.documentations.first().rawTextContent
  )

  fun createBpmnModelInstance(process: WorstDayProcess): BpmnModelInstance = with(process) {
    require(tasks.isNotEmpty())

    // this first creates a [BpmnModelInstance] containing only the startEvent, then loops through all the tasks, and finally adds the endEvent.
    Bpmn.createExecutableProcess(processDefinitionKey)
      .name(processName)
      .documentation(WorstDayProcess.DESCRIPTION)
      .camundaStartableInTasklist(false)
      .camundaVersionTag("${version}")
      .startEvent(EVENT_START).name("Started in good mood").camundaAsyncBefore()
      .documentation("documentation")
      .camundaExecutionListenerExpression(
        ExecutionListener.EVENTNAME_END,
        """#{execution.setVariable("${WorstDayProcess.Companion.VARIABLES.processInstanceId.name}",execution.processInstanceId)}"""
      )
      .done()
      .apply {
        var lastElementId = EVENT_START
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
          .endEvent(WorstDayProcess.Companion.ELEMENTS.EVENT_END).name("Reached Beer O'clock")
          .camundaAsyncBefore()
          .camundaExecutionListenerDelegateExpression(ExecutionListener.EVENTNAME_START, "#{worstDayProcessService.throwLumberghInterventionListener()}")
          .camundaFailedJobRetryTimeCycle("R1/PT1M")
          .done()
      }
  }
}
