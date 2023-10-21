import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import library.SimulatedEcu
import kotlin.system.exitProcess

fun startEmbeddedWebserver() {
    embeddedServer(CIO, port = 8000, module = Application::appModule).start(wait = true)
}

fun SimGateway.toDto(): GatewayDataDto {
    val gatewayEcu = this.ecus.first { it.config.logicalAddress == this.config.logicalAddress }
    return GatewayDataDto(
        name = this.config.name,
        logicalAddress =  this.config.logicalAddress,
        functionalAddress = gatewayEcu.config.functionalAddress,
        ecus = this.ecus.filter { it != gatewayEcu }.map { it.toDto() }
    )
}

fun SimulatedEcu.toDto(): EcuDataDto =
    EcuDataDto(
        name = this.name,
        physicalAddress = this.config.logicalAddress,
        functionalAddress = this.config.functionalAddress,
    )

@Serializable
data class GatewayDataDto(
    var name: String,
    var logicalAddress: Short,
    var functionalAddress: Short,
    var ecus: List<EcuDataDto>,
)

@Serializable
data class EcuDataDto(
    var name: String,
    var physicalAddress: Short,
    var functionalAddress: Short,
)

@OptIn(ExperimentalSerializationApi::class)
fun Application.appModule() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    routing {
        get("/") {
            val data = gatewayInstances().map { it.toDto() }
            call.respond(data)
        }
        addStateRoutes()
        addRecordingRoutes()
        post("/shutdown") {
            call.respond(HttpStatusCode.OK)
            exitProcess(0)
        }
    }
}
