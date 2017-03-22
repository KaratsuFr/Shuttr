package fr.netsah.photoServ.rest

import fr.netsah.photoServ.pojo.Password
import fr.netsah.photoServ.pojo.User
import fr.netsah.photoServ.repo.UserRepo
import fr.netsah.photoServ.security.UserSecurityUtils
import mu.KLogging
import rx.internal.util.ActionSubscriber
import javax.annotation.security.PermitAll
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext




@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class UserRest {

    companion object: KLogging()

    @Context
    lateinit  var secuContext: SecurityContext

    @PermitAll
    @GET
    fun getMock(@Suspended response: AsyncResponse) {
        val sub = buildUserSubResponse(response)

        val username = "test"
        UserRepo.instance.findOneUser(username).subscribe({ u ->
            print("user found $u")
            response.resume(u)
        }, { ex ->
            println("exception not found $ex")
            val u = User(username = username, password = Password.encode("pass", "salt"))
            UserRepo.instance.saveOneUser(u).subscribe({ UserRepo.instance.findOneUser(username).subscribe(sub) }
                    , { ex -> response.resume(ex) })
        })
    }

    @GET
    @Path("/{username}")
    fun getUserInfo(@PathParam("username") username: String, @Suspended response: AsyncResponse) {

        val sub = buildUserSubResponse(response)
        // current user is user to get
        if (username.equals(secuContext!!.userPrincipal.name))
            UserRepo.instance.findOneUser(username).subscribe(sub)
        else
            UserRepo.instance.findOneUser(username).map( {u->  u.copy( mail=null ) }).subscribe(sub)
    }


    @POST
    @Produces("application/json")
    @Consumes("application/x-www-form-urlencoded")
    @Path("/login")
    fun authenticateUser(@FormParam("username") username: String,
                         @FormParam("password") password: String): Response {

        try {
            // Authenticate the user using the credentials provided
            UserSecurityUtils.instance.isValidUser(username, password)
            return Response.ok().entity(UserSecurityUtils.instance.generateToken(username)).build()
        } catch (e: Exception) {
            logger.warn { e }
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

    }

    fun buildUserSubResponse(response: AsyncResponse): ActionSubscriber<User> {
        return ActionSubscriber<User>({ u: User ->
            response.resume(u)
        }, { ex: Throwable -> logger.error { ex }
            response.resume(Response.noContent().build()) }, {})
    }
}

