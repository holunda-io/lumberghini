package io.holunda.funstuff.lumberghini.camunda

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object CamundaExtensions {

  fun <T:Any> T?.toOptional() = Optional.ofNullable(this)

  /**
   * Marker annotation, use to mark methods that are used as expressions (delegates, listener, ...) in a BPMN process.
   * Allows ignoring `unused` warnings and helps identifying expression methods.
   */
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.SOURCE)
  annotation class DelegateExpression

  /**
   * Enhances [AuthenticationProvider] by overriding the required methods with default implementations, so the concrete instance
   * does not have to implement empty method bodies.
   */
  interface DefaultAuthenticationProvider : AuthenticationProvider {
    override fun extractAuthenticatedUser(request: HttpServletRequest, processEngine: ProcessEngine): AuthenticationResult

    override fun augmentResponseByAuthenticationChallenge(response: HttpServletResponse, processEngine: ProcessEngine) = Unit
  }
}
