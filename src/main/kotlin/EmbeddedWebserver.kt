import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.serialization.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import library.SimulatedEcu

@OptIn(ExperimentalSerializationApi::class)
fun startEmbeddedWebserver() {
    embeddedServer(CIO, port = 8000) {
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
            get ("/") {

                val data = gatewayInstances.map { it.toDto() }
                call.respond(data)
            }
            addStateRoutes()
        }
    }.start(wait = true)
}

fun SimGateway.toDto(): GatewayDataDto {
    val gatewayEcu = this.ecus.first { it.config.physicalAddress == this.config.logicalAddress }
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
        physicalAddress = this.config.physicalAddress,
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
