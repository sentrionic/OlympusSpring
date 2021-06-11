package xyz.olympusblog.service

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.ObjectMetadata
import net.coobird.thumbnailator.Thumbnails
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import xyz.olympusblog.config.AppProperties
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO

@Service
class ImageUploadService(private val appProperties: AppProperties) {

    private val credentials = BasicAWSCredentials(appProperties.awsAccessKey, appProperties.awsSecretAccessKey)
    private val client = AmazonS3ClientBuilder
        .standard()
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .withRegion(Regions.EU_CENTRAL_1)
        .build()

    fun uploadArticleImage(file: MultipartFile, directory: String): String {
        val metadata = ObjectMetadata()
        metadata.contentType = "image/jpg"
        val key = "files/$directory.jpg"

        val image = ImageIO.read(file.inputStream)
        val outputStream = ByteArrayOutputStream()

        if (image.height < DIM_MIN || image.width < DIM_MIN) {
            Thumbnails.of(image).size(DIM_MIN, DIM_MIN).keepAspectRatio(true).outputFormat("jpg")
                .toOutputStream(outputStream)
        } else {
            Thumbnails.of(image).size(DIM_MAX, DIM_MAX).keepAspectRatio(true).outputFormat("jpg")
                .toOutputStream(outputStream)
        }

        client.putObject(
            appProperties.awsStorageBucketName,
            key,
            ByteArrayInputStream(outputStream.toByteArray()),
            metadata
        )
        outputStream.close()
        return "https://${appProperties.awsStorageBucketName}.s3.${client.region}.amazonaws.com/$key"
    }

    fun uploadAvatarImage(file: MultipartFile, directory: String): String {
        val metadata = ObjectMetadata()
        metadata.contentType = "image/jpg"
        val key = "files/$directory.jpg"

        val outputStream = ByteArrayOutputStream()
        Thumbnails.of(file.inputStream).size(150, 150).keepAspectRatio(true)
            .toOutputStream(outputStream)

        client.putObject(
            appProperties.awsStorageBucketName,
            key,
            ByteArrayInputStream(outputStream.toByteArray()),
            metadata
        )
        outputStream.close()
        return "https://${appProperties.awsStorageBucketName}.s3.${client.region}.amazonaws.com/$key"
    }

    companion object {
        const val DIM_MAX = 1080
        const val DIM_MIN = 320
    }
}