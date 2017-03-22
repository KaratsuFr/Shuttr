package fr.netsah.photoServ.pojo

import com.drew.imaging.ImageMetadataReader
import org.bson.types.ObjectId
import java.io.File
import java.time.LocalDateTime
import javax.imageio.ImageIO

/**
 * Basic store representation of a photo:
 * </br>- upload date
 * </br>- data
 * </br>- meta information
 */
data class Photo(var _id: ObjectId?=null, val dateUpload: LocalDateTime = LocalDateTime.now(), val owner: String, val binImg: ByteArray, var metadata: Map<String,String>) {
    companion object Helper {
        fun build(f: File,owner: String): Photo {
            // check if img
            if (ImageIO.read(f) == null) {
                throw RuntimeException("The file ${f.absoluteFile} could not be opened as an image")
            }

            var metadataFile = ImageMetadataReader.readMetadata(f)
            var metadata = HashMap<String,String>()
            metadataFile.directories
                    .flatMap { it.tags }
                    .forEach { metadata.put(it.tagName, it.description) }

            return Photo(binImg = f.readBytes(), metadata = metadata,owner = owner)
        }
    }
}
