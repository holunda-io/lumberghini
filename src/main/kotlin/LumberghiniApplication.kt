package io.holunda.funstuff.lumberghini

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableProcessApplication
class LumberghiniApplication

fun main(args: Array<String>) = runApplication<LumberghiniApplication>(*args)
		.let { Unit }
