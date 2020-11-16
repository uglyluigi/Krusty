package bot

import bindings.RustDefs
import bot.CommandHandler.ArgType.NUMBER
import bot.CommandHandler.Command.Companion.ArgRange
import bot.Hasher.Companion.sha256
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.cdimascio.dotenv.dotenv
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import org.opencv.objdetect.Objdetect
import java.io.File
import java.net.URL
import javax.imageio.ImageIO
import kotlin.random.Random

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME)
            val dotenv = dotenv()
            val client = DiscordClient.create(dotenv["BOT_TOKEN"])
            val gateway = client.login().block()
            val handler = CommandHandler()

            handler.addCommand(
                commandName = "ping",
                argRange = ArgRange.DONT_CARE_DIDNT_ASK,
                numArgs = 0,
                argNames = emptyArray(),
                argTypes = emptyArray()
            ) { args, msg ->
                print("Running !ping")
                msg.channel.block()?.createMessage("Pong!")?.block()
            }

            handler.addCommand(
                commandName = "blur",
                argRange = ArgRange.EXACTLY,
                numArgs = 1,
                argNames = arrayOf("passes"),
                argTypes = arrayOf(NUMBER)
            ) { args, message ->
                val passes = args[0] as Int

                if (message.attachments.isNotEmpty()) {
                    println("Downloading images")
                    val files = downloadImagesFrom(message)

                    for (f in files) {
                        Java2RustUtils.getObjectCasted<String>(
                            RustDefs.blurImage(
                                Java2RustUtils.createInstance(f.absolutePath),
                                Java2RustUtils.createInstance(passes)
                            )
                        )?.let {
                            Helper.emitImageResult(message, it)
                        }
                    }
                }
            }

            handler.addCommand(
                commandName = "rotate",
                argRange = ArgRange.EXACTLY,
                numArgs = 4,
                argNames = arrayOf("degree", "r", "g", "b"),
                argTypes = arrayOf(NUMBER, NUMBER, NUMBER, NUMBER),
            ) { args, message ->
                val degree = args[0] as Int
                val red = args[1] as Int
                val green = args[2] as Int
                val blue = args[3] as Int

                if (message.attachments.isNotEmpty()) {
                    println("Downloading images")
                    val files = downloadImagesFrom(message)

                    for (f in files) {
                        Java2RustUtils.getObjectCasted<String>(
                            RustDefs.rotateImage(
                                Java2RustUtils.createInstance(f.absolutePath),
                                Java2RustUtils.createInstance(degree),
                                Java2RustUtils.createInstance(red),
                                Java2RustUtils.createInstance(green),
                                Java2RustUtils.createInstance(blue)
                            )
                        )?.let {
                            Helper.emitImageResult(message, it)
                        }
                    }
                }
            }

            handler.addCommand(
                commandName = "findFace",
                argRange = ArgRange.EXACTLY,
                numArgs = 0,
                argNames = emptyArray(),
                argTypes = emptyArray()
            ) { args, message ->
                if (message.attachments.isNotEmpty()) {
                    val images = downloadImagesFrom(message)

                    if (images.isNotEmpty()) {
                        for (img in images) {
                            val classifier = CascadeClassifier()
                            val imageMat = loadImage(img.absolutePath)
                            val minFaceSize = Math.round(imageMat.rows() * 0.1f).toDouble()
                            val detections = MatOfRect()

                            classifier.load(File(Main::class.java.getResource("./lbpcascade_frontalface.xml").toURI()).absolutePath)

                            if (!classifier.empty()) {
                                if (!imageMat.empty()) {
                                    classifier.detectMultiScale(imageMat, detections, 1.1, 3, Objdetect.CASCADE_SCALE_IMAGE, Size(minFaceSize, minFaceSize), Size())
                                    val faces = detections.toArray()

                                    fun getRand(): Double {
                                        return Random.nextDouble(0.0, 255.0)
                                    }

                                    for (face in faces) {
                                        Imgproc.rectangle(imageMat, face.tl(), face.br(), Scalar(getRand(), getRand(), getRand()), 3)
                                    }

                                    saveImage(imageMat, "face_out.jpg")
                                    Helper.emitImageResult(message, "face_out.jpg")
                                } else {
                                    println("Couldn\'t open input file")
                                }
                            } else {
                                println("Couldn\'t load XML configuration file. Face recognition will not take place")
                            }
                        }
                    }
                } else {
                    Helper.reply(message, "This command requires an image attachment with your message.")
                }
            };

            gateway?.on(MessageCreateEvent::class.java)?.subscribe { event: MessageCreateEvent ->
                val message = event.message
                val content = message.content

                if (content.isNotEmpty() && handler.runCommand(content, message)) {
                    println("Ran command: $content")
                }
            }

            gateway?.onDisconnect()?.block()
        }

        fun downloadImagesFrom(msg: Message): ArrayList<File> {
            val urls = msg.attachments.map { URL(it.url) }
            val files = arrayListOf<File>()
            files.ensureCapacity(urls.size)
            val hash = urls.joinToString { it.path }.sha256()

            for ((index, url) in urls.withIndex()) {
                ImageIO.read(url)?.let {
                    val file = File("image_${hash}_$index.png")
                    ImageIO.write(it, "png", file)
                    files.add(file)
                }
            }

            return files
        }

        fun loadImage(imagePath: String): Mat {
            return Imgcodecs.imread(imagePath)
        }

        fun saveImage(matrix: Mat, path: String) {
            Imgcodecs.imwrite(path, matrix)
        }
    }
}