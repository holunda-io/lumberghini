package io.holunda.funstuff.lumberghini.process

import io.holunda.funstuff.lumberghini.test.TestProcessEngineConfiguration
import org.junit.Rule
import org.junit.Test

@Deprecated("remove this, moved to WorstDayProcessInstanceTest")
class PerpetuumMobileProcessInstanceTest {

  @get:Rule
  val camunda = TestProcessEngineConfiguration().rule()


  @Test
  fun name() {

  }
}
