package fr.netsah.photoServ.repo

import com.google.gson.Gson
import com.mongodb.rx.client.MongoClients
import com.mongodb.rx.client.MongoDatabase


/**
 * Database Helper
 */
class Mongo private constructor() {

    private var db: MongoDatabase

    init {
        if(settings == null) {
            db = MongoClients.create().getDatabase(dbName)
        }else{
            db = MongoClients.create(settings).getDatabase(dbName)
        }
    }

    private object Holder {
        val INSTANCE = Mongo().db
    }

    companion object {
        var settings : String? = null
        var dbName : String = "dbname"
        val gson : Gson = Gson()

        val instance: MongoDatabase by lazy { Holder.INSTANCE }
    }

}
