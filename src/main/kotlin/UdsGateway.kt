import library.decodeHex

fun udsGateway(gateway: CreateGatewayFunc) {
    gateway("GATEWAY") {
        // The logical address for your gateway
        logicalAddress = 0x1010
        functionalAddress = 0x5050

        // VIN - will be padded to the right with 0 until 17 chars are reached, if left empty, 0xFF will be used
        vin = "ITS_A_WIN_VIN_123"
        // Define the entity id (defaults to 6 byte of 0x00), typically the MAC of an ECU
        eid = "101010101010".decodeHex()
        // Define the group id (defaults to 6 byte of 0x00), should be used to address a group of ecus when no vin is known
        gid = "909090909090".decodeHex()

        addSessionFunctionality()
        addReset()
        addSecurityAccess()

        udsEcu(::ecu)
    }
}

fun udsEcu(ecu: CreateEcuFunc) {
    ecu("UDSECU") {
        physicalAddress = 0x1020
        functionalAddress = 0x5050

        addSessionFunctionality()
        addReset()
        addSecurityAccess()
    }
}
