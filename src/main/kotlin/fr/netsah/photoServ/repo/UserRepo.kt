package fr.netsah.photoServ.repo

import com.mongodb.client.model.Filters.eq
import com.mongodb.rx.client.Success
import fr.netsah.photoServ.pojo.User
import org.bson.Document
import rx.Single


/**
 * User repo management
 */
object UserRepo {

    val collection = Mongo.instance.getCollection(UserRepo::class.java.simpleName)!!


    fun findOneUser(username: String): Single<User> = collection.find(eq("username", username)).first().toSingle().map{ doc -> Mongo.gson.fromJson(doc.toJson(), User::class.java)
    }

    fun saveOneUser(user: User): Single<Success> = collection.insertOne(Document.parse(Mongo.gson.toJson(user))).toSingle()

}
