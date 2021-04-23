package io.holunda.funstuff.lumberghini.camunda.filter

import org.camunda.bpm.engine.ProcessEngineServices
import org.camunda.bpm.engine.filter.Filter
import org.camunda.bpm.engine.impl.persistence.entity.FilterEntity

class FilterDataService(
  private val processEngineServices: ProcessEngineServices
) {
  private val filterService = processEngineServices.filterService
  private val taskService = processEngineServices.taskService

  fun create(data: FilterData): Filter = (filterService.newTaskFilter(data.name) as FilterEntity).apply {
    this.properties = data.properties
    data.taskAssigneeExpression?.also {
      this.setQuery(taskService.createTaskQuery().taskAssigneeExpression(it))
    }
  }.let { filterService.saveFilter(it) }
}
