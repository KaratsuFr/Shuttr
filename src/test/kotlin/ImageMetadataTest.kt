
import com.drew.imaging.ImageMetadataReader
import com.google.gson.Gson
import org.testng.annotations.Test





class ImageMetadataTest{

    val gson = Gson()

    @Test fun extractMetaData(){

        var metadata = ImageMetadataReader.readMetadata( javaClass.classLoader.getResourceAsStream("imgTest.JPG"))

        for (directory in metadata.directories) {
            for (tag in directory.tags) {
                println("[${directory.name}] - ${tag.tagName} = ${tag.description}")
            }
            if (directory.hasErrors()) {
                for (error in directory.errors) {
                    println("ERROR: $error")
                }
            }
        }
    }

//    @Test fun checkMetadataToJson(){
//        var metadata = ImageMetadataReader.readMetadata( javaClass.classLoader.getResourceAsStream("imgTest.JPG"))
//        println(gson.toJson(metadata))
//    }
}