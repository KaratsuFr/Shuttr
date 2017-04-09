package fr.netsah.photoServ.security

import org.jboss.resteasy.core.Headers
import org.jboss.resteasy.core.ResourceMethodInvoker
import org.jboss.resteasy.core.ServerResponse
import org.jboss.resteasy.plugins.server.embedded.SimplePrincipal
import org.jboss.resteasy.util.Base64
import java.io.IOException
import java.security.Principal
import java.util.*
import javax.annotation.security.DenyAll
import javax.annotation.security.PermitAll
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.NotFoundException
import javax.ws.rs.container.ContainerRequestContext
import javax.ws.rs.container.ContainerRequestFilter
import javax.ws.rs.core.Context
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.ext.Provider


/**
 * This interceptor verify the access permissions for a user
 * based on username and passowrd provided in request
 * */
@Provider
class SecurityInterceptor : ContainerRequestFilter {

    @Context
    lateinit var webRequest: HttpServletRequest

    companion object {
        val AUTHORIZATION_PROPERTY = "Authorization"
        val AUTHENTICATION_BASIC_SCHEME = "Basic "
        val AUTHENTICATION_TOKEN_SCHEME = "Token "

        val ACCESS_DENIED = ServerResponse("Access denied for this resource", 401, Headers<Any>())
        val ACCESS_FORBIDDEN = ServerResponse("Nobody can access this resource", 403, Headers<Any>())
        val SERVER_ERROR = ServerResponse("INTERNAL SERVER ERROR", 500, Headers<Any>())
    }

    override fun filter(requestContext: ContainerRequestContext) {
        val methodInvoker = requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker") as ResourceMethodInvoker
        val method = methodInvoker.method
        //Access allowed for all
        if (!method.isAnnotationPresent(PermitAll::class.java)) {
            //Access denied for all
            if (method.isAnnotationPresent(DenyAll::class.java)) {
                requestContext.abortWith(ACCESS_FORBIDDEN)
                return
            }
            //Get request headers
            val headers = requestContext.headers
            //Fetch authorization header
            val authorization = headers[AUTHORIZATION_PROPERTY]
            //If no authorization information present; block access
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED)
                return
            }

            val username: String?

            //Get encoded username and password
            val basicAuthInfo = authorization.find { h -> h.startsWith(AUTHENTICATION_BASIC_SCHEME) }
            val tokenInfo = authorization.find { h -> h.startsWith(AUTHENTICATION_TOKEN_SCHEME) }

            if (tokenInfo != null) {
                var tokenEncoded = tokenInfo.replaceFirst(AUTHENTICATION_TOKEN_SCHEME, "")

                try {
                    tokenEncoded = String(Base64.decode(tokenEncoded))
                } catch (e: IOException) {
                    requestContext.abortWith(SERVER_ERROR)
                    return
                }

                //Split username and password tokens
                val tokenizer = StringTokenizer(tokenEncoded, ":")
                username = tokenizer.nextToken()
                val tokenToCheck = tokenizer.nextToken()

                if (tokenToCheck != UserSecurityUtils.calculateHMAC(username)) {
                    requestContext.abortWith(ACCESS_DENIED)
                    return
                }
            } else if (basicAuthInfo != null) {
                val encodedUserPassword = basicAuthInfo.replaceFirst(AUTHENTICATION_BASIC_SCHEME, "")
                //Decode username and password
                val usernameAndPassword: String?
                try {
                    usernameAndPassword = String(Base64.decode(encodedUserPassword))
                } catch (e: IOException) {
                    requestContext.abortWith(SERVER_ERROR)
                    return
                }
                //Split username and password tokens
                val tokenizer = StringTokenizer(usernameAndPassword, ":")
                username = tokenizer.nextToken()
                if (username == null) {
                    requestContext.abortWith(ACCESS_DENIED)
                    return
                }
                val password = tokenizer.nextToken()

                try {
                    UserSecurityUtils.isValidUser(username, password)
                } catch (ex: NotFoundException) {
                    requestContext.abortWith(ACCESS_DENIED)
                    return
                }
            } else {
                requestContext.abortWith(ACCESS_DENIED)
                return
            }
            requestContext.securityContext = object : SecurityContext {
                override fun getUserPrincipal(): Principal {
                    return SimplePrincipal(username)
                }

                override fun isUserInRole(s: String): Boolean {
                    return false
                }

                override fun isSecure(): Boolean {
                    return false
                }

                override fun getAuthenticationScheme(): String? {
                    return null
                }
            }

            //Verify user access
//            if (method.isAnnotationPresent(RolesAllowed::class.java)) {
//                val rolesAnnotation = method.getAnnotation(RolesAllowed::class.java)
//                val rolesSet = HashSet<String>(rolesAnnotation.value.asList())
//
//
//                //Is user valid?
//                if (!isUserAllowed(userBd, rolesSet)) {
//                    requestContext.abortWith(ACCESS_DENIED)
//                    return
//                }
//            }
        }
    }


//    private fun isUserAllowed(userBd: User, rolesSet: Set<String>): Boolean {
//        // NO Role management yet
//        return true
//

//
//        val isAllowed = false
//        //Step 1. Fetch password from database and match with password in argument
//        //If both match then get the defined role for user from database and continue; else return isAllowed [false]
//        //Access the database and do this part yourself
//        //String userRole = userMgr.getUserRole(username);
//        val userRole = "ADMIN"
//        //Step 2. Verify user role
//        if (rolesSet.contains(userRole)) {
//            isAllowed = true
//        }
//        return isAllowed
//    }
}

