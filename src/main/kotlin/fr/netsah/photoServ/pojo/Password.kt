package fr.netsah.photoServ.pojo
import java.nio.charset.Charset
import java.security.MessageDigest
import java.util.*

/**
 * Representation of a hashed password
 * @property hash The hashed password
 * @property salt The salt that was used
 */
data class Password(val hash: String, val salt: String) {
    companion object {
        /**
         * Encode a plain test password
         * @param password The plain text password to encode
         * @param salt The salt to encode it with
         * @return The encoded password
         */
        fun encode(password: String, salt: String = UUID.randomUUID().toString()): Password {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(salt.toByteArray(Charset.forName("UTF-8")))
            md.update(password.toByteArray(Charset.forName("UTF-8")))
            val hashedPassword = md.digest()

            return Password(hash = Base64.getEncoder().encodeToString(hashedPassword),
                    salt = salt)
        }
    }

    /**
     * Compare the object to another object for equality
     * This has a special side case that if it compares to a String then it will automatically encode
     * that String using the same salt and then do the comparison
     * @param other The object to compare to
     * @return True if the two objects represent the same password
     */
    override fun equals(other: Any?): Boolean =
            when (other) {
                null -> false
                is String -> equals(Password.encode(password = other, salt = salt))
                is Password -> hash == other.hash && salt == other.salt
                else -> false
            }

    override fun hashCode(): Int {
        var result = hash.hashCode()
        result = 31 * result + salt.hashCode()
        return result
    }
}