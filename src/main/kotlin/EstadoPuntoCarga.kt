sealed class EstadoPuntoCarga {}
object Libre: EstadoPuntoCarga() {}
class Procesando (val mensaje: String): EstadoPuntoCarga() {}
object EnCarga : EstadoPuntoCarga() {}
class FueraDeServicio(val motivo: String): EstadoPuntoCarga() {}