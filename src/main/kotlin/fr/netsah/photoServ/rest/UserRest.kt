package fr.netsah.photoServ.rest

import com.mongodb.rx.client.Success
import fr.netsah.photoServ.pojo.Password
import fr.netsah.photoServ.pojo.User
import fr.netsah.photoServ.pojo.UserInfoDto
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

    companion object : KLogging()

    @Context
    lateinit var secuContext: SecurityContext

    @GET
    @Path("/{username}")
    fun getUserInfo(@PathParam("username") username: String, @Suspended response: AsyncResponse) {

        val sub = buildUserSubResponse(response)
        // current user is user to get
        if (secuContext.userPrincipal != null && username == secuContext.userPrincipal.name)
            UserRepo.findOneUser(username).subscribe(sub)
        else
            UserRepo.findOneUser(username).map({ u -> u.copy(mail = null) }).subscribe(sub)
    }

    @GET
    fun pingUser(): String? {

        val currentUserInfo = secuContext.userPrincipal
        if (currentUserInfo == null) {
            return null
        } else {
            val username = currentUserInfo.name
            return "{\"username\":\"$username\"}"
        }
    }

    @PermitAll
    @POST
    @Path("/login")
    fun loginUser(user: UserInfoDto): Response {

        try {
            logger.info("login user $user")

            // Authenticate the user using the credentials provided
            UserSecurityUtils.isValidUser(user.username, user.password)
            val token = UserSecurityUtils.generateToken(user.username)

            return Response.ok().entity("{\"token\":\"$token\"}").build()
        } catch (e: Exception) {
            logger.warn { e }
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }
    }

    @PermitAll
    @POST
    @Path("/register")
    fun registerUser(userInfo: UserInfoDto, @Suspended response: AsyncResponse) {
        logger.info("register user $userInfo")
        val user = User(userInfo.username, Password.encode(userInfo.password, UserSecurityUtils.SIGNATURE_APP), userInfo.mail)

        val sub = ActionSubscriber<Success>({
            response.resume("{\"token\":\"" + UserSecurityUtils.generateToken(userInfo.username) + "\"}")
        }, { ex: Throwable ->
            logger.error { ex }
            response.resume(Response.status(Response.Status.CONFLICT).build())
        }, {})

        UserRepo.saveOneUser(user).doOnSuccess({response.resume("{\"token\":\"" + UserSecurityUtils.generateToken(userInfo.username) + "\"}")}).doOnError( { ex: Throwable ->
            logger.error { ex }
            response.resume(Response.status(Response.Status.CONFLICT).build())
        }).toBlocking().value()

    }

    fun buildUserSubResponse(response: AsyncResponse): ActionSubscriber<User> {
        return ActionSubscriber({ u: User ->
            response.resume(u)
        }, { ex: Throwable ->
            logger.error { ex }
            response.resume(Response.noContent().build())
        }, {})
    }
}

