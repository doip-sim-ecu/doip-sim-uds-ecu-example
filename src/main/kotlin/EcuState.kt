import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.serialization.Serializable

fun findByEcuName(ecuName: String): SimEcu? {
    gatewayInstances.forEach {
        val ecu = it.findEcuByName(ecuName)
        if (ecu != null) {
            return ecu
        }
    }
    return null
}

fun Route.addStateRoutes() {
    post("/reset") {
        gatewayInstances.forEach { it.reset() }
        call.respond(HttpStatusCode.NoContent)
    }
    get("/{ecu}/state") {
        val ecu = findByEcuName(call.parameters["ecu"]!!) ?: return@get call.respond(HttpStatusCode.NotFound)
        call.respond(HttpStatusCode.OK, ecu.ecuState())
    }
}

@Serializable
data class EcuState(
    var sessionState: SessionState? = SessionState.DEFAULT,
    var securityAccess: SecurityAccess? = SecurityAccess.LOCKED,
    var bootSoftwareVersions: List<SoftwareVersionIdentifier> = emptyList(),
    var applicationSoftwareVersions: List<SoftwareVersionIdentifier> = emptyList(),
    var vin: String = "ITS_A_WIN_VIN_123",
    var seed: ByteArray? = null,
)

enum class SessionState(val value: Byte) {
    DEFAULT(0x01),
    PROGRAMMING(0x02),
    EXTENDED(0x03),
    SAFETY(0x04)
}

enum class SecurityAccess(val level: Byte) {
    LOCKED(0),
    LEVEL_3(3),
    LEVEL_5(5),
    LEVEL_7(7);

    companion object {
        fun parse(level: Byte) =
            values().firstOrNull { it.level == level }
    }
}

val initialStateByEcu: MutableMap<String, EcuState> = mutableMapOf()

fun RequestsData.setInitialState(state: EcuState) {
    initialStateByEcu[this.name] = state
}

fun SimEcu.ecuState(): EcuState {
    val ecuState by this.storedProperty { initialStateByEcu[this.name]?.copy() ?: EcuState() }
    return ecuState
}

