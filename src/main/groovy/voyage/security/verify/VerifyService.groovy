package voyage.security.verify

import groovy.time.TimeCategory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import voyage.common.sms.AwsSmsService
import voyage.common.sms.SmsMessage
import voyage.security.crypto.CryptoService
import voyage.security.user.PhoneType
import voyage.security.user.User
import voyage.security.user.UserPhone
import voyage.security.user.UserService

import javax.validation.constraints.NotNull

@Service
@Validated
class VerifyService {
    @Value('${security.user-verification.verify-code-expire-minutes}')
    private int verifyCodeExpires

    @Value('${app.name}')
    private String appName

    private final UserService userService
    private final CryptoService cryptoService
    private final AwsSmsService smsService

    @Autowired
    VerifyService(UserService userService, CryptoService cryptoService, AwsSmsService smsService) {
        this.userService = userService
        this.cryptoService = cryptoService
        this.smsService = smsService
    }

    boolean verifyCurrentUser(@NotNull String code) {
        User user = userService.currentUser

        UserPhone userPhone = user.phones?.find { phone ->
            cryptoService.hashMatches(code, phone.verifyCode)
        }

        if (!userPhone) {
            throw new InvalidVerificationCodeException()
        }

        if (userPhone.verifyCodeExpired) {
            throw new VerifyCodeExpiredException()
        }

        userPhone.isValidated = true
        userPhone.verifyCode = null
        userPhone.verifyCodeExpiresOn = null
        user.isVerifyRequired = false

        userService.saveDetached(user)

        return true
    }

    void sendVerifyCodeToCurrentUser() {
        User user = userService.currentUser

        Iterable<UserPhone> mobilePhones = user.phones?.findAll {
            it.phoneType == PhoneType.MOBILE
        }

        if (!mobilePhones) {
            throw new InvalidVerificationPhoneNumberException()
        }

        user.isVerifyRequired = true

        // Limit the number of phones that can receive a verification code to 5. This prevents an attacker from overloading
        // the list of phone numbers for a user and spamming an infinite number of phones with security codes.
        int count = (mobilePhones.size() > 5 ? 5 : mobilePhones.size())
        for (int i=0; i < count; i++) {
            sendVerifyCodeToPhoneNumber(mobilePhones[i])
        }

        userService.saveDetached(user)
    }

    private sendVerifyCodeToPhoneNumber(@NotNull UserPhone mobilePhone) {
        String verifyCode = SecurityCode.userVerifyCode
        mobilePhone.verifyCode = cryptoService.hashEncode(verifyCode)
        use(TimeCategory) {
            mobilePhone.verifyCodeExpiresOn = new Date() + verifyCodeExpires.minutes
        }

        SmsMessage smsMessage = new SmsMessage()
        smsMessage.to = mobilePhone.phoneNumber
        smsMessage.text = "Your ${appName} verification code is: ${verifyCode}"
        smsService.send(smsMessage)
    }
}
