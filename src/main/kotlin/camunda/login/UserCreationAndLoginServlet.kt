package io.holunda.funstuff.lumberghini.camunda.login

import io.holunda.funstuff.lumberghini.process.WorstDayProcessService
import mu.KLogging
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.webapp.impl.security.auth.AuthenticationService
import org.camunda.bpm.webapp.impl.security.auth.Authentications
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
  private val worstDayProcessService: WorstDayProcessService
) {
  companion object : KLogging()

  private val authenticationService = AuthenticationService()

  @PostMapping(value = ["/create-user"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
  fun createAndLogin(@ModelAttribute newUserModel: NewUserModel, session: HttpSession): ModelAndView {
    val userId = newUserModel.getUserId().trim { it <= ' ' }

    return try {
      if (identityService.createUserQuery().userId(userId).count() == 0L) {
        logger.info { "User created: '$userId'" }
        val user = identityService.newUser(userId).apply {
          firstName = newUserModel.getFirstName()
          lastName = newUserModel.getLastName()
        }

        identityService.saveUser(user)
      }
      logger.info { "Cleared authentication" }

      Authentications.clearCurrent()
      Authentications().apply {
        addAuthentication(authenticationService.createAuthenticate("default", userId))
        Authentications.updateSession(session, this)
      }

      worstDayProcessService.start(userId)

      logger.info("User '$userId' is now logged in.")
      // redirect to welcome
      ModelAndView("redirect:/app/tasklist/default/")
    } catch (e: Exception) {
      logger.error("Error logging you in", e)
      ModelAndView("/index.html")
    }
  }

  /**
   * Spring request model.
   */
  data class NewUserModel(
    @get:NotEmpty(message = "Must not be empty")
    @get:NotNull(message = "Must not be null")
    @get:Pattern(regexp = """[a-zA-Z\s]+""", message = "Must contain letters and whitespace only")
    var userNameInput: String? = null
  ) {
    fun getUserId() = split().let { it.first + it.second }.joinToString(separator = "") { it.toLowerCase() }
    fun getFirstName() = split().first.joinToString(separator = " ")
    fun getLastName() = split().second
    //var firstName : String?  get() = "11"

    private fun split() : Pair<List<String>,String> = (userNameInput ?: "").trim()
      .split("""\s+""".toRegex())
      .map { it.toLowerCase().capitalize() }
      .let { if (it.size == 1) it + "" else it }
      .reversed()
      .let { Pair(it.drop(1).reversed(), it.take(1).first() ) }
  }
}
