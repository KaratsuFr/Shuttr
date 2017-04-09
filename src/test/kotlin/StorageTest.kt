import com.google.gson.Gson
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import fr.netsah.photoServ.pojo.Password
import fr.netsah.photoServ.pojo.Photo
import fr.netsah.photoServ.pojo.User
import fr.netsah.photoServ.repo.Mongo
import fr.netsah.photoServ.repo.UserRepo
import org.bson.Document
import org.testng.Assert
import org.testng.annotations.AfterClass
import org.testng.annotations.BeforeClass
import org.testng.annotations.Test
import rx.observers.TestSubscriber
import java.io.File


class StorageTest {

    var mongodExecutable: MongodExecutable? = null
    val bindIp = "localhost"
    val port = 12345

    val gson = Gson()

    @BeforeClass fun init() {
        val starter = MongodStarter.getDefaultInstance()

        val mongodConfig = MongodConfigBuilder()
                .version(Version.Main.PRODUCTION)
                .net(Net(bindIp, port, Network.localhostIsIPv6()))
                .build()

        mongodExecutable = starter.prepare(mongodConfig)
        mongodExecutable!!.start()
        Mongo.settings = "mongodb://$bindIp:$port"

    }

    @AfterClass fun stop() {
        mongodExecutable!!.stop()
    }


    @Test fun shouldInsertAndRetrieveUser() {

        val u = User(username = "test", mail = "test@test.fr", password = Password.encode("pass", "salt"))

        var userR: User? = null

        val testSub: TestSubscriber<Any> = TestSubscriber()
        val testSubFetch: TestSubscriber<Any> = TestSubscriber()

        Mongo.instance.getCollection("test").insertOne(Document.parse(gson.toJson(u))).doOnCompleted({
            Mongo.instance.getCollection("test").find().first().map { obs ->
                userR = gson.fromJson(obs.toJson(), User::class.java)
                println(userR)
            }.subscribe(testSubFetch)
        }).subscribe(testSub)

        testSubFetch.awaitTerminalEvent()

        testSub.assertNoErrors()
        testSubFetch.assertNoErrors()

        Assert.assertEquals(userR, u)
    }

    @Test fun shouldInsertAndRetrievePhoto() {
        val f = File(javaClass.classLoader.getResource("imgTest.JPG").toURI())
        val photo = Photo.build(f,"Junit")

        val testSub: TestSubscriber<Any> = TestSubscriber()
        val testSubFetch: TestSubscriber<Any> = TestSubscriber()

        Mongo.instance.getCollection("testPhoto").insertOne(Document.parse(gson.toJson(photo))).doOnCompleted({
            Mongo.instance.getCollection("testPhoto").find().first().map { obs ->
                val photoFetch = gson.fromJson(obs.toJson(), Photo::class.java)
                println(photoFetch.metadata)
                val saveFile = File.createTempFile("img", ".jpg")
                saveFile.writeBytes(photoFetch.binImg)
                println(saveFile.absolutePath)
            }.subscribe(testSubFetch)
        }).subscribe(testSub)


        testSubFetch.awaitTerminalEvent()

        testSub.assertNoErrors()
        testSubFetch.assertNoErrors()
    }

    @Test fun shouldInsertOnlyOnce() {
        Mongo.instance.getCollection(UserRepo::class.java.simpleName).createIndex(Document("username", "text"), IndexOptions().unique(true)).toBlocking().single()
        println(UserRepo::class.java.simpleName)
        val u = User(username = "test", mail = "test@test.fr", password = Password.encode("pass", "salt"))

        val testSub: TestSubscriber<Any> = TestSubscriber()
        val testSub2: TestSubscriber<Any> = TestSubscriber()

        UserRepo.saveOneUser(u).doOnSuccess {
            UserRepo.saveOneUser(u).doOnSuccess {
                val nbUser =Mongo.instance.getCollection(UserRepo::class.java.simpleName).count().toBlocking().first()
                println("Nb User : $nbUser")

                Assert.assertEquals(1,nbUser)
            }.subscribe(testSub2)
        }.subscribe(testSub)


        testSub2.awaitTerminalEvent()
        testSub2.assertError(MongoWriteException::class.java)

        //testSub2.assertError(Exception::class.java)

        testSub.awaitTerminalEvent()
        testSub.assertNoErrors()

    }
}