package io.holunda.funstuff.lumberghini.camunda.login

import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import mu.KLogging
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationService
import org.camunda.bpm.webapp.impl.security.auth.Authentications
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpSession
import javax.validation.Valid
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern

@Controller
@RequestMapping("/public")
class UserCreationAndLoginServlet(
  private val identityService: IdentityService,
  private val filterService: FilterService,
  private val worstDayProcessService: WorstDayProcessService
) {
  companion object : KLogging()

  private val authenticationService = AuthenticationService()

  @PostMapping(value = ["/create-user"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
  fun createAndLogin(
    @ModelAttribute
    @Valid
    newUserModel: NewUserModel,
    session: HttpSession
  ): ModelAndView {
    val userInfo = NewUserInfo(newUserModel)
    logger.info { "==========    logging in new user: $userInfo" }

    return try {
      if (identityService.createUserQuery().userId(userInfo.id).count() == 0L) {
        logger.info { "User created: '${userInfo.id}'" }
        val user = identityService.newUser(userInfo.id).apply {
          firstName = userInfo.firstName
          lastName = userInfo.lastName
        }

        identityService.saveUser(user)
      }
      logger.info { "Cleared authentication" }

      Authentications.clearCurrent()
      Authentications().apply {
        addAuthentication(authenticationService.createAuthenticate("default", userInfo.id))
        Authentications.updateSession(session, this)
      }

      worstDayProcessService.start(userInfo.id)

      val filterId = filterService.createTaskFilterQuery().filterName("My Tasks").singleResult().id
      val taskListPath =
        "/app/tasklist/default/#/?searchQuery=%5B%5D&filter=$filterId&sorting=%5B%7B%22sortBy%22:%22created%22,%22sortOrder%22:%22desc%22%7D%5D"

      logger.info("User '${userInfo.id}' is now logged in.")
      // redirect to welcome
      ModelAndView("redirect:$taskListPath")
    } catch (e: Exception) {
      logger.error("Error logging you in", e)
      ModelAndView("/index.html")
    }
  }

  data class NewUserInfo(val id: String, val firstName: String, val lastName: String) {
    companion object {
      operator fun invoke(model: NewUserModel): NewUserInfo {
        val parts: Pair<List<String>, String> = requireNotNull(model.userNameInput).trim()
          .split("""\s+""".toRegex())
          .map { it.toLowerCase().capitalize() }
          .let { if (it.size == 1) it + "" else it }
          .reversed()
          .let { Pair(it.drop(1).reversed(), it.take(1).first()) }

        return NewUserInfo(
          id = parts.let { it.first + it.second }.joinToString(separator = "") { it.toLowerCase() },
          firstName = parts.first.joinToString(separator = " "),
          lastName = parts.second
        )
      }
    }
  }

  /**
   * Spring request model. Just used for POST method call, transformed to
   * [NewUserInfo] for user creation.
   */
  data class NewUserModel(
    @get:NotEmpty(message = "Must not be empty")
    @get:NotNull(message = "Must not be null")
    @get:Pattern(regexp = """[a-zA-Z\s]+""", message = "Must contain letters and whitespace only")
    var userNameInput: String? = null
  )
}
