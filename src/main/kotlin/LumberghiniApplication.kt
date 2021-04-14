package io.holunda.funstuff.lumberghini

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.funstuff.lumberghini.JacksonDataFormatConfigurator.Companion.configure
import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import io.holunda.funstuff.lumberghini.task.FindNextTaskStrategy
import mu.KLogging
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import java.time.LocalDate


/**
 * Runs the lumberghini application.
 */
fun main(args: Array<String>) = runApplication<LumberghiniApplication>(*args)
  .let { Unit }

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
  fun objectMapper() = jacksonObjectMapper().configure()

  @Bean
  fun todaySupplier(): TodaySupplier = { LocalDate.now() }

  override fun run(vararg args: String) {
    logger.info { "started with: $properties" }
  }
}

typealias TodaySupplier = () -> LocalDate
typealias UserName = String
typealias ProcessDefinitionId = String
typealias ProcessInstanceId = String
typealias DeploymentId = String
typealias TaskDefinitionKey = String

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class DelegateExpression

/**
 * Configured via SPI in `src/main/resources/META-INF/services/org.camunda.spin.spi.DataFormatConfigurator`.
 */
open class JacksonDataFormatConfigurator : DataFormatConfigurator<JacksonJsonDataFormat> {
  companion object {

    /**
     * Configure the Jackson [ObjectMapper] for use with kotlin/java8.
     */
    fun ObjectMapper.configure() {
      registerModule(KotlinModule())
      registerModule(JavaTimeModule())
    }
  }

  override fun configure(dataFormat: JacksonJsonDataFormat) = dataFormat.objectMapper.configure()
  override fun getDataFormatClass(): Class<JacksonJsonDataFormat> = JacksonJsonDataFormat::class.java
}
