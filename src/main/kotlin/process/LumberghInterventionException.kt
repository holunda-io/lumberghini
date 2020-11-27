package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.DelegateExpression
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.delegate.ExecutionListener

class LumberghInterventionException : ProcessEngineException(LumberghInterventionException.MESSAGE) {
  companion object {

    val MESSAGE = """
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
