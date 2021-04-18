package io.holunda.funstuff.lumberghini.task

/**
 * List of [WorstDayTask]. Like a regular List, but enhanced with
 * app specific functions.
 */
data class WorstDayTasks(
  val list: List<WorstDayTask>
) : List<WorstDayTask> by list {
  companion object {
    val EMPTY = WorstDayTasks(emptyList())
  }

  constructor(vararg tasks: WorstDayTask) : this(tasks.toList())

  val taskCount by lazy { asSequence().map { it.taskId.id }.map { it to maxTaskCount(it) }.toMap() }
  val lowestCount by lazy { taskCount.values.minByOrNull { it } ?: 1 }

  val taskIdAndMaxCount by lazy {
    asSequence().map { it.taskId.id }.map { it to maxTaskCount(it) }.toMap()
  }

  val tasksWithHighestCount by lazy { filter { it.taskId.count == taskIdAndMaxCount[it.taskId.id] } }
  val tasksWithLowestCount by lazy {
    tasksWithHighestCount.filter { it.taskId.count == lowestCount }
  }

  /**
   * Highest [TaskId#count] of task with given Id.
   * Used to create the taskId of a task to be added.
   */
  fun maxTaskCount(id: Int) = filter { it.taskId.id == id }
    .map { it.taskId.count }
    .maxByOrNull { it }
    ?: 0


  fun add(task: WorstDayTask): WorstDayTasks = plus(task)

  operator fun plus(task: WorstDayTask): WorstDayTasks = copy(
    list = (list + task.withCount(maxTaskCount(task.taskId.id) + 1))
  )
}
