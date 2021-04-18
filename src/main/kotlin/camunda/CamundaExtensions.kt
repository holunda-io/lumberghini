package io.holunda.funstuff.lumberghini.camunda

import io.holunda.funstuff.lumberghini.ProcessInstanceId
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.impl.interceptor.Command
import org.camunda.bpm.engine.impl.interceptor.CommandContext
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

object CamundaExtensions {

  /**
   * Marker annotation, use to mark methods that are used as expressions (delegates, listener, ...) in a BPMN process.
   * Allows ignoring `unused` warnings and helps identifying expression methods.
   */
  @Target(AnnotationTarget.FUNCTION)
  @Retention(AnnotationRetention.SOURCE)
  annotation class DelegateExpression

  val CommandContext.runtimeService get() = this.processEngineConfiguration.runtimeService

  fun DelegateExecution.commandExecutor() = CommandExecutorAdapter(processEngine.processEngineConfiguration as ProcessEngineConfigurationImpl)

  fun <T : Any> DelegateExecution.execute(cmd: Command<T>) = commandExecutor().execute(cmd)

  fun DelegateExecution.suspend() = processEngineServices.runtimeService.suspendProcessInstanceById(processInstanceId)

  fun CommandContext.queryProcessInstance(processInstanceId: ProcessInstanceId) = runtimeService.createProcessInstanceQuery()
    .processInstanceId(processInstanceId)
    .singleResult()

  /**
   * Enhances [AuthenticationProvider] by overriding the required methods with default implementations, so the concrete instance
   * does not have to implement empty method bodies.
   */
  interface DefaultAuthenticationProvider : AuthenticationProvider {
    override fun extractAuthenticatedUser(request: HttpServletRequest, processEngine: ProcessEngine): AuthenticationResult

    override fun augmentResponseByAuthenticationChallenge(response: HttpServletResponse, processEngine: ProcessEngine) = Unit
  }
}
