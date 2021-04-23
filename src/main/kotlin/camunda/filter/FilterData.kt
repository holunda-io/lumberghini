package io.holunda.funstuff.lumberghini.camunda.filter


data class FilterData(
  val name: String,
  val description: String,
  val color: String? = null,
  val priority: Int = 0,
  val refresh: Boolean = false,
  val showUndefinedVariable: Boolean = false,
  val taskAssigneeExpression: String? = null
) {

  val query by lazy {
    mutableMapOf<String, String>().apply {
      taskAssigneeExpression?.also { this["taskAssigneeExpression"] to it }
    }.toMap()
  }

  val properties by lazy {
    mutableMapOf<String, Any>().apply {
      this["description"] = description
      this["priority"] = priority
      this["refresh"] = refresh
      this["showUndefinedVariable"] = showUndefinedVariable

      color?.also { this["color"] = color }
    }.toMap()
  }
}
