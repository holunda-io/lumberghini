package io.holunda.funstuff.lumberghini

import io.holunda.funstuff.lumberghini.task.FindNextTaskStrategy
import io.holunda.funstuff.lumberghini.task.TaskDataConfiguration
import mu.KLogging
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.configuration.CamundaProcessEngineConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.LocalDate

@SpringBootApplication
@EnableProcessApplication
@EnableConfigurationProperties(LumberghiniConfigurationProperties::class)
class LumberghiniApplication : CommandLineRunner {
  companion object : KLogging()

  @Autowired
  lateinit var properties: LumberghiniConfigurationProperties

  @Bean
  fun findNextTaskStrategy() = FindNextTaskStrategy.countingNextTaskStrategy()

  @Bean
  fun todaySupplier(): TodaySupplier = { LocalDate.now() }

  @Bean
  fun disableTelemetry() = object : CamundaProcessEngineConfiguration {
    override fun preInit(processEngineConfiguration: ProcessEngineConfigurationImpl) = with(processEngineConfiguration) {
      isInitializeTelemetry = false
      isTelemetryReporterActivate = false
    }

    override fun toString(): String = "Disable telemetry"
  }

  override fun run(vararg args: String) {
    logger.info { "started with: $properties" }
  }
}

@ConfigurationProperties(prefix = "application.lumberghini")
@ConstructorBinding
data class LumberghiniConfigurationProperties(
  val tasks: List<TaskDataConfiguration>
)

fun main(args: Array<String>) = runApplication<LumberghiniApplication>(*args)
  .let { Unit }

typealias TodaySupplier = () -> LocalDate
typealias UserName = String
typealias ProcessDefinitionId = String
typealias ProcessInstanceId = String
typealias DeploymentId = String

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DelegateExpression
