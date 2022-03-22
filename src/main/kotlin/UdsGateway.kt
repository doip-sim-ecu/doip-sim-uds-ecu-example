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

        setInitialState(
            EcuState(
                bootSoftwareVersions = listOf(SoftwareVersionIdentifier(2, 3, 2)),
                applicationSoftwareVersions = listOf(SoftwareVersionIdentifier(4, 1, 0))
            )
        )

        addSessionFunctionality()
        addReset()
        addSecurityAccess()
        addDiagnosticServices()

        udsEcu(::ecu)
    }
}

fun udsEcu(ecu: CreateEcuFunc) {
    ecu("UDSECU") {
        physicalAddress = 0x1020
        functionalAddress = 0x5050

        setInitialState(
            EcuState(
                bootSoftwareVersions = listOf(SoftwareVersionIdentifier(1, 0, 0)),
                applicationSoftwareVersions = listOf(SoftwareVersionIdentifier(1, 1, 0))
            )
        )

        addSessionFunctionality()
        addReset()
        addSecurityAccess()
        addDiagnosticServices()
    }
}
