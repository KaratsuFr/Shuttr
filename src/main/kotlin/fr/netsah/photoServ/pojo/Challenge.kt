package fr.netsah.photoServ.pojo

import org.bson.types.ObjectId

/**
 * Challenge need:
 * <br/>- title
 * <br/>- a description
 * <br/>- isOpen allow to add photos
 * <br/>- list of photos of challenge
 */
data class Challenge(var _id: ObjectId?=null, var title: String, var description : String,var isOpen: Boolean,var lstIdPhoto: String)