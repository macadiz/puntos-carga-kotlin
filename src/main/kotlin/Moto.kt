import java.time.LocalDateTime
import java.time.Duration

class Moto(codigo: String, marca: String, tipoCliente: TIPO_CLIENTE):
    Vehiculo(codigo, marca, tipoCliente, 150.0) {

    override fun calcularSubTotal(): Double {
        val ahora = LocalDateTime.now()
        val diferenciaMinutos = Duration.between(ahora, fechaConexion!!).toMinutes()

        if (diferenciaMinutos < 15) {
            return 0.0
        }

        return super.calcularSubTotal()
    }
}