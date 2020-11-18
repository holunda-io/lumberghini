package io.holunda.funstuff.lumberghini

import io.holunda.funstuff.lumberghini.process.FindNextTaskStrategy
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
@EnableProcessApplication
class LumberghiniApplication {

  @Bean
  fun findNextTaskStrategy() = FindNextTaskStrategy.countingNextTaskStrategy()
}

fun main(args: Array<String>) = runApplication<LumberghiniApplication>(*args)
		.let { Unit }
