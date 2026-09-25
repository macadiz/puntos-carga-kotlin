import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime

class PuntoCarga(val id: Int) {
    var vehiculo: Vehiculo? = null
    var estado: EstadoPuntoCarga = Libre

    val historicoVehiculos: MutableList<RegistroVehiculo> = mutableListOf()

    suspend fun conectarVehiculo(vehiculo: Vehiculo) {
        if (estado != Libre) {
            throw Error("El punto de carga no está disponible")
        }

        estado = Procesando("Conectando vehículo")
        delay(4000L)

        this.vehiculo = vehiculo
        vehiculo.conectar()

        estado = EnCarga
    }

    suspend fun desconectarVehiculo() {
        if (vehiculo == null || estado != EnCarga) {
            throw Error("El punto de carga no está conectado a un vehículo")
        }

        estado = Procesando("Desconectando vehículo")
        delay(5500L)

        val total = this.vehiculo!!.calcularSubTotal()

        val ahora = LocalDateTime.now()
        val diferenciaMinutos = Duration.between(ahora, this.vehiculo!!.fechaConexion).toMinutes()

        historicoVehiculos.add(RegistroVehiculo(this.vehiculo!!, total, diferenciaMinutos))
        this.vehiculo!!.desconectar()

        this.vehiculo = null

        estado = Libre
    }
}