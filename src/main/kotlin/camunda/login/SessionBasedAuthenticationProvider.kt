package io.holunda.funstuff.lumberghini.camunda.login

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter.AUTHENTICATION_PROVIDER_PARAM
import org.camunda.bpm.webapp.impl.security.auth.Authentications
import org.camunda.bpm.webapp.impl.security.auth.ContainerBasedAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SessionBasedAuthenticationProvider : AuthenticationProvider {
  companion object {
    val FQN = SessionBasedAuthenticationProvider::class.java.canonicalName
  }

  override fun extractAuthenticatedUser(request: HttpServletRequest, engine: ProcessEngine): AuthenticationResult {
    val authentications = Authentications.getFromSession(request.session)

    val userId = if (authentications != null && authentications.hasAuthenticationForProcessEngine(engine.name)) {
      authentications.getAuthenticationForProcessEngine(engine.name).name
    } else {
      "admin"
    }
    return AuthenticationResult(userId, true)
  }

  override fun augmentResponseByAuthenticationChallenge(response: HttpServletResponse?, engine: ProcessEngine?) {
    // noop
  }

  @Configuration
  class CamundaWebAppsSecurityConfiguration {

    @Bean
    fun containerBasedAuthenticationFilterRegistrationBean(): FilterRegistrationBean<ContainerBasedAuthenticationFilter> =
      FilterRegistrationBean<ContainerBasedAuthenticationFilter>().apply {
        filter = ContainerBasedAuthenticationFilter()
        initParameters = mapOf(AUTHENTICATION_PROVIDER_PARAM to FQN)
        addUrlPatterns("/app/*", "/api/*", "/lib/*")
        setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST))
      }
  }
}
