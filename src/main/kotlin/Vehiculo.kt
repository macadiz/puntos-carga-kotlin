import java.time.LocalDateTime
import java.time.Duration

open class Vehiculo(val codigo: String, val marca: String, val tipoCliente: TIPO_CLIENTE, val tarifa: Double = 0.0) {
    var fechaConexion: LocalDateTime? = null

    fun conectar() {
        fechaConexion = LocalDateTime.now()
    }

    fun desconectar() {
        fechaConexion = null
    }

    override fun toString(): String {
        return "Vehiculo(codigo: ${codigo}, marca: ${marca}, tipoCliente: ${tipoCliente.toString()})"
    }

    open fun calcularSubTotal(): Double {
        val ponderadorDescuento = if (tipoCliente == TIPO_CLIENTE.SOCIO) {
            0.8
        } else {
            1.0
        }

        val ahora = LocalDateTime.now()
        val diferenciaMinutos = Duration.between(ahora, fechaConexion!!).toMinutes()

        return tarifa * (diferenciaMinutos / 60) * ponderadorDescuento
    }

    fun calcularTotal(): Double {
        val total = calcularSubTotal() * 1.19

        if (tipoCliente == TIPO_CLIENTE.ADULTO_MAYOR) {
            return total * 0.5
        }
        return total
    }
}