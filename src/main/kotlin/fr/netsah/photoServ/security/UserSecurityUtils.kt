package fr.netsah.photoServ.security

import fr.netsah.photoServ.pojo.User
import fr.netsah.photoServ.repo.UserRepo
import mu.KLogging
import org.jboss.resteasy.util.Base64
import java.security.GeneralSecurityException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.ws.rs.NotFoundException


object UserSecurityUtils {

    val LOG = KLogging().logger

    val SIGNATURE_APP = System.getProperty("SIG_APP")!!

    fun isValidUser(username: String, password: String): User {
        try {
            val userBd = UserRepo.findOneUser(username).toBlocking().value()
            LOG.info("userBd : $userBd - pass to check : $password")
            if (userBd.password.equals(password)) {
                return userBd
            } else {
                throw NotFoundException(username)
            }
        } catch (ex: Exception) {
            throw NotFoundException(username,ex)
        }
    }

    fun generateToken(username: String): String {
        return "Token " + Base64.encodeBytes((username + ":" + calculateHMAC(username)).toByteArray())
    }

    fun calculateHMAC(secret: String): String {
        try {
            val signingKey = SecretKeySpec(secret.toByteArray(), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(signingKey)
            val rawHmac = mac.doFinal(SIGNATURE_APP.toByteArray())
            val result = String(Base64.encodeBytesToBytes(rawHmac))
            return result
        } catch (e: GeneralSecurityException) {
            throw IllegalArgumentException()
        }
    }
}