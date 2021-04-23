package io.holunda.funstuff.lumberghini.camunda.filter

//10847f1c-a45b-11eb-98d2-7e77227b1520	1	Task	My Tasks	null	{"taskAssigneeExpression":"${ currentUser() }"}	{"description":"all I have to do","priority":0,"color":"#ef0606","refresh":false,"showUndefinedVariable":false}

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
