package io.holunda.funstuff.lumberghini

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.holunda.funstuff.lumberghini.JacksonDataFormatConfigurator.Companion.configure
import io.holunda.funstuff.lumberghini.camunda.filter.FilterData
import io.holunda.funstuff.lumberghini.camunda.filter.FilterDataService
import io.holunda.funstuff.lumberghini.camunda.login.SessionBasedAuthenticationProvider
import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import mu.KLogging
import org.camunda.bpm.engine.impl.ProcessEngineImpl
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication
import org.camunda.bpm.spring.boot.starter.util.SpringBootProcessEnginePlugin
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter
import org.camunda.spin.impl.json.jackson.format.JacksonJsonDataFormat
import org.camunda.spin.spi.DataFormatConfigurator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import java.time.LocalDate
import java.util.*
import javax.servlet.DispatcherType

/**
 * Runs the [LumberghiniApplication].
 */
fun main(args: Array<String>) = runApplication<LumberghiniApplication>(*args).let { }

/**
 * The main spring boot application and core spring [org.springframework.context.annotation.Configuration].
 */
@SpringBootApplication
@EnableProcessApplication
@EnableConfigurationProperties(LumberghiniConfigurationProperties::class)
class LumberghiniApplication {
  companion object : KLogging()

  /**
   * AuthenticationFilter that uses activates the [SessionBasedAuthenticationProvider] to auto login users to the cockpit.
   */
  @Bean
  fun containerBasedAuthenticationFilterRegistrationBean(): FilterRegistrationBean<ContainerBasedAuthenticationFilter> =
    FilterRegistrationBean<ContainerBasedAuthenticationFilter>().apply {
      filter = ContainerBasedAuthenticationFilter()
      initParameters = mapOf(ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM to SessionBasedAuthenticationProvider.FQN)
      addUrlPatterns("/app/*", "/api/*", "/lib/*")
      setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST))
    }

  @Bean
  fun createMyTasksFilterPlugin() : SpringBootProcessEnginePlugin = object : SpringBootProcessEnginePlugin() {
    override fun postProcessEngineBuild(processEngine: ProcessEngineImpl) {
      val filterDataService = FilterDataService(processEngine)
      val myTasksFilter = filterDataService.create(
        FilterData(
          name = "My Tasks",
          description = "all I have to do today",
          priority = 100,
          color = "#c31d1d",
          taskAssigneeExpression = "\${ currentUser() }",
          refresh = true
        )
      )
      logger.info { "Created Task Filter: $myTasksFilter" }
    }
  }

  @Bean
  fun objectMapper() = jacksonObjectMapper().configure()

  /**
   * Returns current date. Use supplier to simplify unit testing.
   */
  @Bean
  fun todaySupplier(): TodaySupplier = { LocalDate.now() }
}

/**
 * Supplier that returns a [LocalDate].
 */
typealias TodaySupplier = () -> LocalDate

/**
 * Camunda user name (String).
 */
typealias UserName = String

/**
 * Camunda: id of a [org.camunda.bpm.engine.repository.ProcessDefinition] (String).
 */
typealias ProcessDefinitionId = String
/**
 * Camunda: id of a [org.camunda.bpm.engine.runtime.ProcessInstance] (String).
 */
typealias ProcessInstanceId = String

/**
 * Camunda: id of a [org.camunda.bpm.engine.repository.Deployment] (String).
 */
typealias DeploymentId = String

/**
 * Camunda: key of a [org.camunda.bpm.engine.task.Task] (String).
 */
typealias TaskDefinitionKey = String


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
