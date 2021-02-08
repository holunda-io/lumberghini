package io.holunda.funstuff.lumberghini

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@ContextConfiguration(initializers = [ConfigDataApplicationContextInitializer::class])
@EnableConfigurationProperties(LumberghiniConfigurationProperties::class)
internal class LumberghiniConfigurationPropertiesTest {

  @Autowired
  lateinit var properties : LumberghiniConfigurationProperties

  @Test
  internal fun `generate id from task`() {
    val task = properties.tasks.get(0)

    assertThat(task.id).isEqualTo("task-please-give-me-my-stapler")
  }
}
