class Auto(codigo: String, marca: String, tipoCliente: TIPO_CLIENTE):
    Vehiculo(codigo, marca, tipoCliente, 300.0) {

    override fun calcularSubTotal(): Double {
        if (tipoCliente == TIPO_CLIENTE.SOCIO) {
            return super.calcularSubTotal() * 0.8
        }
        return super.calcularSubTotal()
    }
}