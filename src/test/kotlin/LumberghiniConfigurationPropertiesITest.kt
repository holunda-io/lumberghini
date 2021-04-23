package io.holunda.funstuff.lumberghini

import io.holunda.funstuff.lumberghini.properties.LumberghiniConfigurationProperties
import io.holunda.funstuff.lumberghini.properties.TaskDataConfiguration
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class]) // required to read the application.yml
@EnableConfigurationProperties(LumberghiniConfigurationProperties::class)
internal class LumberghiniConfigurationPropertiesITest {

  @Autowired
  lateinit var properties: LumberghiniConfigurationProperties

  @Test
  internal fun `generate id from task`() {
    val task = properties.tasks.get(0)

    assertThat(task.name).isEqualTo("Please give me my stapler")
    assertThat(task.description).isEqualTo("I need my stapler.")
    assertThat(task.colleague).isEqualTo(TaskDataConfiguration.Colleague.Milton)
  }
}
