package io.holunda.funstuff.lumberghini.camunda.login

import io.holunda.funstuff.lumberghini.camunda.login.UserCreationAndLoginServlet.NewUserModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.validation.Validation

/**
 * Tests validation of form input and transformation to info object.
 */
internal class NewUserTest {

  private val validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  internal fun `validate user input`() {
    assertThat(NewUserModel().validate())
      .containsExactlyInAnyOrder("Must not be empty", "Must not be null")

    assertThat(NewUserModel("787_____87").validate()).containsExactly("Must contain letters and whitespace only")
    assertThat(NewUserModel("!!!!!Jan  ").validate()).containsExactly("Must contain letters and whitespace only")
    assertThat(NewUserModel("          Jan      ").validate()).isEmpty()
    assertThat(NewUserModel("          JAN      ").validate()).isEmpty()
    assertThat(NewUserModel("          jan      ").validate()).isEmpty()
  }

  @Test
  internal fun `split first last name`() {
    UserCreationAndLoginServlet.NewUserInfo(NewUserModel(userNameInput = "     Jan    Galinski    "))
      .assert("jangalinski", "Jan", "Galinski")


    UserCreationAndLoginServlet.NewUserInfo(NewUserModel(userNameInput = "     Jan        "))
      .assert("jan", "Jan", "")

    UserCreationAndLoginServlet.NewUserInfo(NewUserModel(userNameInput = "     Jan   Phillip KaLLa      "))
      .assert("janphillipkalla", "Jan Phillip", "Kalla")
  }

  private fun UserCreationAndLoginServlet.NewUserInfo.assert(expectedUserId : String, expectedFirstName :String, expectedLastName:String) {
    assertThat(this.id).isEqualTo(expectedUserId)
    assertThat(this.firstName).isEqualTo(expectedFirstName)
    assertThat(this.lastName).isEqualTo(expectedLastName)
  }

  private fun NewUserModel.validate() = validator.validate(this).map { it.message }
}
