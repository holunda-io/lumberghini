package io.holunda.funstuff.lumberghini.camunda.login

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

@Controller
@RequestMapping("/public")
class UserCreationAndLoginServlet(private val identityService: IdentityService) {
  companion object : KLogging()

  private val authenticationService = AuthenticationService()

  @PostMapping(value = ["/create-user"], consumes = ["application/x-www-form-urlencoded;charset=UTF-8"])
  fun createAndLogin(@ModelAttribute newUserModel: @Valid NewUserModel, session: HttpSession?): ModelAndView? {
    val userId = newUserModel.userId!!.trim { it <= ' ' }
    return try {
      if (identityService.createUserQuery().userId(userId).count() == 0L) {
        logger.info { "User created: '$userId'" }
        val user = identityService.newUser(userId)
        user.firstName = newUserModel.firstName
        user.lastName = newUserModel.lastName
        identityService.saveUser(user)
      }
      logger.info { "Cleared authentication" }
      Authentications.clearCurrent()
      val authentications = Authentications()
      authentications.addAuthentication(authenticationService.createAuthenticate("default", userId))
      Authentications.updateSession(session, authentications)
      logger.info("User '$userId' is now logged in.")
      // redirect to welcome
      ModelAndView("redirect:/app/welcome/default/")
    } catch (e: Exception) {
      logger.error("Error logging you in", e)
      ModelAndView("/index.html")
    }
  }

  /**
   * Spring request model.
   */
  data class NewUserModel(
    var userId: @NotNull @NotEmpty String? = null,
    var lastName: String? = null,
    var firstName: String? = null
  )
}
