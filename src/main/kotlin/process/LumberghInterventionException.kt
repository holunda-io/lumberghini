package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions.DelegateExpression
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.delegate.ExecutionListener

/**
 * Exception thrown in end event listener to keep the process from completing and give us some time to
 * add a new task and deploy/migrate to the next process engine version.
 */
class LumberghInterventionException(message: String = MESSAGE) : ProcessEngineException(message) {
  companion object {

    const val MESSAGE = """
     ______  __ __   ____  ______  __  ___        ____     ___       ____  ____     ___   ____  ______
    |      ||  |  | /    ||      ||  ||   \      |    \   /  _]     /    ||    \   /  _] /    ||      |
    |      ||  |  ||  o  ||      ||_ ||    \     |  o  ) /  [_     |   __||  D  ) /  [_ |  o  ||      |
    |_|  |_||  _  ||     ||_|  |_|  \||  D  |    |     ||    _]    |  |  ||    / |    _]|     ||_|  |_|
      |  |  |  |  ||  _  |  |  |      |     |    |  O  ||   [_     |  |_ ||    \ |   [_ |  _  |  |  |
      |  |  |  |  ||  |  |  |  |      |     |    |     ||     |    |     ||  .  \|     ||  |  |  |  |
      |__|  |__|__||__|__|  |__|      |_____|    |_____||_____|    |___,_||__|\_||_____||__|__|  |__|

    Lumbergh intervention: seems like you cannot go home right now ...


    """

    @DelegateExpression
    fun throwExceptionListener() = ExecutionListener {
      throw LumberghInterventionException()
    }
  }
}
