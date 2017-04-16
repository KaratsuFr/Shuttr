package fr.netsah.photoServ.rest

import org.jboss.resteasy.annotations.cache.Cache
import java.nio.charset.Charset
import javax.annotation.security.PermitAll
import javax.ws.rs.*
import javax.ws.rs.core.MediaType




@Path("/app")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class AppRest {

    @GET
    @Cache(maxAge = 86400)
    @PermitAll
    fun getTranslation(@QueryParam("lang") lang: String): String{
        val stream = AppRest::class.java.getResource("/lang/$lang.json")

        return  stream.readText(Charset.forName("UTF-8"))
    }
}