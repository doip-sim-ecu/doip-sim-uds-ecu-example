import java.util.*

fun RequestsData.addSessionFunctionality() {
    request("10 01", name = "DefaultSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.DEFAULT
        ack()
    }

    request("10 02", name = "ProgrammingSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.PROGRAMMING
        ack()
    }

    request("10 03", name = "ExtendedDiagnosticSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.EXTENDED
        ack()
    }

    request("10 04", name = "SafetySystemDiagnosticSession") {
        val ecuState = ecu.ecuState()
        ecuState.sessionState = SessionState.SAFETY
        ack()
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
