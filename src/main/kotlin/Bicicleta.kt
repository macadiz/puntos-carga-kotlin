class Bicicleta(codigo: String, marca: String, tipoCliente: TIPO_CLIENTE):
    Vehiculo(codigo, marca, tipoCliente, 60.0) {

    var cargaRapida = false

    fun conectar(cargaRaida: Boolean) {
        this.cargaRapida = cargaRaida

        super.conectar()
    }

    override fun calcularSubTotal(): Double {
       if (cargaRapida) {
            return super.calcularSubTotal() * 1.25
        }

        return super.calcularSubTotal()
    }
}