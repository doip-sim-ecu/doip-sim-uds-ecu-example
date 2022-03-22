import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*

fun RequestsData.addSessionFunctionality() {
    request("10 01", name = "DefaultSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.DEFAULT
        ack("00 32 01 F4")
    }

    request("10 02", name = "ProgrammingSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.PROGRAMMING
        ack("00 32 01 F4")
    }

    request("10 03", name = "ExtendedDiagnosticSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.EXTENDED
        ack("00 32 01 F4")
    }

    request("10 04", name = "SafetySystemDiagnosticSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.SAFETY
        ack("00 32 01 F4")
    }
}

fun RequestsData.addReset() {
    request("11 01", name = "HardReset") {
        val ecuState = ecu.ecuState()
        ecuState.securityAccess = SecurityAccess.LOCKED
        ecuState.sessionState = SessionState.DEFAULT
        ack()
    }

    request("11 02", name = "KeyOffOnReset") {
        val ecuState = ecu.ecuState()
        ecuState.securityAccess = SecurityAccess.LOCKED
        ecuState.sessionState = SessionState.DEFAULT
        ack()
    }

    request("11 03", name = "SoftReset") {
        val ecuState = ecu.ecuState()
        ecuState.securityAccess = SecurityAccess.LOCKED
        ecuState.sessionState = SessionState.DEFAULT
        ack()
    }
}

fun RequestsData.addSecurityAccess() {
    request("27 []", name = "RequestSeed_SendKey") {
        val ecuState = ecu.ecuState()
        val subFunction = message[1]
        if (subFunction % 2 == 1) {
            // Request Seed
            val level = SecurityAccess.parse(subFunction)
            if (level == null) {
                nrc(NrcError.RequestOutOfRange)
            } else {
                // Create seed and fill with random data
                val seed = ByteArray(8)
                Random().nextBytes(seed)

                ecuState.seed = seed
                ack(byteArrayOf((level.level + 1).toByte(), *seed))
            }
        } else {
            // Send key
            val level = SecurityAccess.parse((subFunction - 1).toByte())
            if (level == null) {
                nrc(NrcError.RequestOutOfRange)
            } else {
                val data = this.message.copyOfRange(2, this.message.size - 1)
                if (ecuState.seed != null) {
                    // Use a super secure algorithm
                    val rot13 = ecuState.seed!!.map { (it + 13 % 0xFF).toByte() }.toByteArray()
                    if (data.contentEquals(rot13)) {
                        ecuState.securityAccess = level
                        ecuState.seed = null
                        ack()
                    } else {
                        nrc(NrcError.InvalidKey)
                    }
                } else {
                    nrc(NrcError.RequestSequenceError)
                }
            }
        }

    }
}

@Serializable
class SoftwareVersionIdentifier(val major: Byte, val minor: Byte, val patch: Byte) {
    val asByteArray: ByteArray
        get() = byteArrayOf(major, minor, patch)

    companion object {
        fun parse(data: ByteBuffer) : SoftwareVersionIdentifier {
            val major = data.get()
            val minor = data.get()
            val patch = data.get()
            return SoftwareVersionIdentifier(major, minor, patch)
        }
    }
}

fun RequestsData.addDiagnosticServices() {
    request("22 F1 80", name = "BootSoftwareIdentificationDataIdentifier") {
        val ecuState = ecu.ecuState()
        ack(byteArrayOf(ecuState.bootSoftwareVersions.size.toByte()) + ecuState.bootSoftwareVersions.map { it.asByteArray }.combine())
    }

    request("22 F1 81", name = "ApplicationSoftwareIdentificationDataIdentifier") {
        val ecuState = ecu.ecuState()
        ack(byteArrayOf(ecuState.applicationSoftwareVersions.size.toByte()) + ecuState.applicationSoftwareVersions.map { it.asByteArray }.combine())
    }

    request("22 F1 86", name = "ActiveDiagnosticSessionDataIdentifier") {
        val ecuState = ecu.ecuState()
        ack(byteArrayOf(ecuState.sessionState!!.value))
    }

    request("22 F1 90", name = "VINDataIdentifier") {
        val ecuState = ecu.ecuState()
        ack(ecuState.vin.encodeToByteArray())
    }
}


fun List<ByteArray>.combine(): ByteArray {
    val out = ByteArrayOutputStream()
    this.forEach { out.write(it) }
    return out.toByteArray()
}

