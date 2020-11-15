package bot

import bindings.RustDefs
import bot.CommandHandler.ArgType.*
import bot.CommandHandler.Command.Companion.ArgRange
import bot.Hasher.Companion.sha256
import discord4j.core.DiscordClient
import discord4j.core.`object`.entity.Attachment
import discord4j.core.`object`.entity.Message
import discord4j.core.event.domain.message.MessageCreateEvent
import io.github.cdimascio.dotenv.dotenv
import org.astonbitecode.j4rs.api.java2rust.Java2RustUtils
import org.opencv.core.Core
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.highgui.HighGui
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.net.URL
import java.util.logging.Handler
import javax.imageio.ImageIO

open class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
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
                commandName = "findFace",
                argRange = ArgRange.DONT_CARE_DIDNT_ASK,
                numArgs = 0,
                argNames = emptyArray(),
                argTypes = emptyArray()
            ) { args, message ->
                val classifier = CascadeClassifier(javaClass.getResource("/lbpcascade_frontalface.xml").path)
                val image = Imgcodecs.imread(javaClass.getResource("/lena.png").path)
                val detections = MatOfRect()

                classifier.detectMultiScale(image, detections)

                println("Detected ${detections.toArray().size} faces")

                for (rect in detections.toArray()) {
                    Imgproc.rectangle(image, Point(rect.x.toDouble(), rect.y.toDouble()), Point(rect.x + rect.width.toDouble(), rect.y + rect.height.toDouble()),  Scalar(0.0, 255.0, 0.0))
                }

                val fileName = "detection.png"
                println("Writing $fileName")
                Imgcodecs.imwrite(fileName, image)
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
    }
}