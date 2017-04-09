package voyage.account

import spock.lang.Specification
import voyage.common.mail.MailService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

class AccountServiceSpec extends Specification {
    User user
    UserService userService = Mock()
    MailService mailService = Mock()
    AccountService accountService = new AccountService(userService, mailService)

    def setup() {
        accountService.appName = 'Voyage'
        accountService.appSupportEmail = 'test@test.com'
    }

    def 'register - applies the values and calls the userService'() {
        given:
            User userIn = new User(
                    firstName:'John',
                    lastName:'Doe',
                    email:'john@doe.com',
                    username:'jdoe',
                    password:'my-secure-password',
            )
            userIn.phones = [
                    new UserPhone(phoneNumber:'111-111-1111', phoneType:PhoneType.MOBILE, user:userIn),
                    new UserPhone(phoneNumber:'222-222-2222', phoneType:PhoneType.MOBILE, user:userIn),
            ]

        when:
            User savedUser = accountService.register(userIn)

        then:
            1 * userService.saveDetached(*_) >> { args ->
                return args[0] // return the given user back
            }
            1 * mailService.send(*_) >> { args ->
                assert args[0].to == 'john@doe.com'
                assert args[0].subject == 'Welcome to Voyage'
                assert args[0].template == 'welcome.ftl'
                assert args[0].model.appName == 'Voyage'
                assert args[0].model.appSupportEmail == 'test@test.com'
            }

            savedUser.firstName == 'John'
            savedUser.lastName == 'Doe'
            savedUser.email == 'john@doe.com'
            savedUser.username == 'jdoe'
            savedUser.password == 'my-secure-password'
            savedUser.isEnabled
            savedUser.isVerifyRequired
            savedUser.phones.size() == 2
            savedUser.phones[0].phoneNumber == '111-111-1111'
            savedUser.phones[0].phoneType == PhoneType.MOBILE
            savedUser.phones[1].phoneNumber == '222-222-2222'
            savedUser.phones[1].phoneType == PhoneType.MOBILE
    }

    def 'register - Welcome email is not sent if the user does not provide an email address'() {
        given:
            User userIn = new User(
                    firstName:'John',
                    lastName:'Doe',
                    username:'jdoe',
                    password:'my-secure-password',
            )

        when:
            User savedUser = accountService.register(userIn)

        then:
            1 * userService.saveDetached(*_) >> { args ->
                return args[0] // return the given user back
            }
            0 * mailService.send(_)

            savedUser.firstName == 'John'
            savedUser.lastName == 'Doe'
            !savedUser.email
            savedUser.username == 'jdoe'
            savedUser.password == 'my-secure-password'
            savedUser.isEnabled
            savedUser.isVerifyRequired
            !savedUser.phones
    }
}