package io.holunda.funstuff.lumberghini.rest

import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/worst-day-process")
class WorstDayProcessController(
  private val service: WorstDayProcessService
) {



}
