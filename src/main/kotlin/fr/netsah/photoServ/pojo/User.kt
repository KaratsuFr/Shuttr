package fr.netsah.photoServ.pojo

import com.fasterxml.jackson.annotation.JsonIgnore
import org.bson.types.ObjectId

/**
 * User is composed by
 * </br>- username
 * </br>- password for access
 * </br>- mail to notify
 * </br>- avatar to illustrate his face
 * </br>- Gallery to manage his photos
 */
data class User(val username: String, @JsonIgnore var password: Password, var mail: String? = null, var avatar: ByteArray? = null){
    var _id: ObjectId?=null
}