package io.holunda.funstuff.lumberghini.camunda.login

import io.holunda.funstuff.lumberghini.camunda.CamundaExtensions
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import org.camunda.bpm.webapp.impl.security.auth.Authentications
import javax.servlet.http.HttpServletRequest

class SessionBasedAuthenticationProvider : CamundaExtensions.DefaultAuthenticationProvider {
  companion object {
    val FQN = SessionBasedAuthenticationProvider::class.java.canonicalName
  }

  override fun extractAuthenticatedUser(request: HttpServletRequest, processEngine: ProcessEngine): AuthenticationResult {
    val authentications = Authentications.getFromSession(request.session)

    val userId = if (authentications != null && authentications.hasAuthenticationForProcessEngine(processEngine.name)) {
      authentications.getAuthenticationForProcessEngine(processEngine.name).name
    } else {
      "admin"
    }

    return AuthenticationResult(userId, true)
  }
}
