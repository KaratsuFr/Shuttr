

import com.mongodb.client.model.IndexOptions
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.IMongodConfig
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import fr.netsah.photoServ.repo.Mongo
import fr.netsah.photoServ.repo.UserRepo
import org.bson.Document
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener

@WebListener
class StartupListener : ServletContextListener {

    companion object {
        private var mongodExecutable: MongodExecutable? = null
        private var bindIp = "localhost"
        private var port = 27017
        private var mongoUser = "demo"
        private var mongoPass ="demo"
        private var databaseName = "kyori"
    }

    override fun contextInitialized(event: ServletContextEvent?) {

        if(System.getProperty("dev") != null) {
            val starter = MongodStarter.getDefaultInstance()

            val mongodConfig: IMongodConfig? = MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(Net(bindIp, port, Network.localhostIsIPv6()))
                    .build()

            mongodExecutable = starter.prepare(mongodConfig!!)
            mongodExecutable!!.start()

        }else{
            if(System.getProperty("MONGODB_URI") != null)  Mongo.settings = System.getProperty("MONGODB_URI")
            else Mongo.settings = "mongodb://$mongoUser:$mongoPass@$bindIp:$port"


            if(System.getProperty("MONGODB_DB") != null)  Mongo.dbName = System.getProperty("MONGODB_DB")
            else Mongo.dbName = databaseName
        }

        // create a text index on the "content" field
        UserRepo.collection.createIndex(Document("username", "text"), IndexOptions().unique(true)).toBlocking().single()

        if(System.getProperty("SIG_APP") == null){
            throw ExceptionInInitializerError("SIG APP not define")
        }

    }

    override fun contextDestroyed(event: ServletContextEvent) {
        // Perform action during application's shutdown
    }
}