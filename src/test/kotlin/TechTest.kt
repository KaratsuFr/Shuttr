import org.testng.Assert
import org.testng.annotations.Test
import java.net.URLDecoder

class TechTest {


    @Test
    fun decodeValue() {
        val encodeToken = "Token%20eyJ1c2VybmFtZSI6InRlc3QiLCJwYXNzd29yZCI6InRlc3QiLCJtYWlsIjoiIn06TjNQZktQaXA5cXlwV2dwU1RpTFBLUnRnNU1XUzBzWm9TMDhsK2NtZ05Ybz0%3D"

        val decoded = URLDecoder.decode(encodeToken, "UTF-8")

        println(decoded)
        Assert.assertNotEquals(encodeToken, decoded)
    }
}