package io.holunda.funstuff.lumberghini.camunda.login

import io.holunda.funstuff.lumberghini.camunda.login.UserCreationAndLoginServlet.NewUserModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.validation.Validation
import javax.validation.ValidatorFactory

internal class NewUserModelTest {

  private val validator = Validation.buildDefaultValidatorFactory().validator

  @Test
  internal fun validates() {
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
    NewUserModel(userNameInput = "     Jan    Galinski    ")
      .assert("jangalinski", "Jan", "Galinski")


    NewUserModel(userNameInput = "     Jan        ")
      .assert("jan", "Jan", "")

    NewUserModel(userNameInput = "     Jan   Phillip KaLLa      ")
      .assert("janphillipkalla", "Jan Phillip", "Kalla")
  }

  private fun NewUserModel.assert(expectedUserId : String, expectedFirstName :String, expectedLastName:String?) {
    assertThat(this.validate()).isEmpty()
    assertThat(this.getUserId()).isEqualTo(expectedUserId)
    assertThat(this.getFirstName()).isEqualTo(expectedFirstName)
    assertThat(this.getLastName()).isEqualTo(expectedLastName)
  }

  private fun NewUserModel.validate() = validator.validate(this).map { it.message }
}
