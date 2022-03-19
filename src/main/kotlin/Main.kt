fun main() {
    println("UDS-Example-ECU")
    udsGateway(::gateway)
    start()
    startEmbeddedWebserver()
}
