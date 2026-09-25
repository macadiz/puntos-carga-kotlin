import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

val puntosCarga = listOf(
    PuntoCarga(1),
    PuntoCarga(2),
    PuntoCarga(3),
    PuntoCarga(4),
    PuntoCarga(5),
    PuntoCarga(6),
    PuntoCarga(7),
    PuntoCarga(8),
)

fun main() {
    while(true) {
        println("Bienvenido!!!")
        println("1.- Ingresar vehículo")
        println("2.- Desconectar vehículo")
        println("3.- Estado de puntos de carga")
        println("4.- Vehículos de socios")
        println("5.- Códigos de vehículos desconectados")
        println("6.- Vehículo cargado por mas tiempo")
        println("0.- Salir")

        print("Ingrese la opción: ")
        val opcion = readln().toInt()

        when (opcion) {
            1 -> {
                val disponible = puntosCarga.find { it -> it.estado == Libre }
                if (disponible != null) {
                    val vehiculo = ingresarVehiculo()

                    GlobalScope.launch {
                        disponible.conectarVehiculo(vehiculo)
                    }
                }
            }
            2 -> {
                print("Ingrese el código del vehículo a desconectar: ")
                val codigo = readln()
                desconectarVehiculo(codigo)
            }
            3 -> {
                estadoPuntosDeCarga()
            }
            4 -> {
                vehiculosSocios()
            }
            5 -> {
                codigosVehiculosDesconectados()
            }
            6 -> {
                vehiculoCargadoMasTiempo()
            }
            0 -> {
                break;
            }
            else -> { println("Opción no válida") }
        }
    }
}

fun ingresarVehiculo(): Vehiculo {
    var codigo = ""
    while(true) {
        try {
            print("Ingrese el código del vehículo: ")
            codigo = readln()

            validarCodigo(codigo)
            break
        } catch (err: Error) {
            print(err.message)
        }
    }

    println("Ingrese la marca: ")
    val marca = readln()

    var tipoCliente: TIPO_CLIENTE? = null
    while (true) {
        try {
            println("Ingrese el tipo de cliente (1: Ocasional, 2: Socio, 3: Adulto mayor): ")
            var tipoClienteInt = readln().toInt()

            tipoCliente = when (tipoClienteInt) {
                1 -> TIPO_CLIENTE.OCASIONAL
                2 -> TIPO_CLIENTE.SOCIO
                3 -> TIPO_CLIENTE.ADULTO_MAYOR
                else -> throw Error("Tipo cliente no válido")
            }

            break;
        } catch (nfe: NumberFormatException) {
            println("Debe ingresar un número")
        } catch (err: Error) {
            print(err.message)
        }
    }

    while (true) {
        try {
            println("Ingrese el tipo de vehiculo (1: Auto, 2: Moto, 3: Bicicleta): ")
            val tipoVehiculoInt = readln().toInt()

            val vehiculo =  when (tipoVehiculoInt) {
                1 -> Auto(codigo, marca, tipoCliente)
                2 -> Moto(codigo, marca, tipoCliente)
                3 -> Bicicleta(codigo, marca, tipoCliente)
                else -> throw Error("Tipo de vehículo no válido")
            }
            return vehiculo // No hace falta el break del while, porque el return resuelve la función, forzando la salida
        } catch (nfe: NumberFormatException) {
            println("Debe ingresar un número")
        } catch (err: Error) {
            println(err.message)
        }
    }
}

fun desconectarVehiculo(codigo: String) {
    val puntoCargaVehiculo = puntosCarga.filter{ it.vehiculo != null }.find { punto -> punto.vehiculo!!.codigo == codigo }

    if (puntoCargaVehiculo != null) {
        GlobalScope.launch {
            puntoCargaVehiculo.desconectarVehiculo()
        }
    }
}

fun estadoPuntosDeCarga() {
    puntosCarga.forEach { punto ->
        println("Punto: ${punto.id}")
        println("Vehiculo: ${punto.vehiculo ?: "N/A"}")
        println("Estado: ${punto.estado}")
    }
}

fun vehiculosSocios() {
    puntosCarga.flatMap { punto -> punto.historicoVehiculos }.forEach { historicoVehiculo ->
        if (historicoVehiculo.vehiculo.tipoCliente == TIPO_CLIENTE.SOCIO) {
            println(historicoVehiculo.vehiculo)
        }
    }
}

fun codigosVehiculosDesconectados() {
    puntosCarga.flatMap { punto -> punto.historicoVehiculos }.forEach { historicoVehiculo ->
        println("Codigo: ${historicoVehiculo.vehiculo.codigo}")
    }
}

fun vehiculoCargadoMasTiempo() {
    val vehiculoMasTiempo = puntosCarga.flatMap { punto -> punto.historicoVehiculos }.maxBy {
        it.tiempoCarga
    }.vehiculo

    println("Vehiculo con mas tiempo cargado: $vehiculoMasTiempo")
}

fun validarCodigo(codigo: String) {
    if (codigo.length > 6) {
        throw Error("El código debe tener 6 caracteres")
    }

    val primerosTres = codigo.substring(0,3) // del 0 al 2
    val ultimosTres = codigo.substring(3,6) // del 2 al 5

    if (!primerosTres.all{ it.isLetter() }) {
        throw Error("El código debe empezar por 3 letras")
    }

    if (!ultimosTres.all{ it.isDigit() }) {
        throw Error("El código debe finalizar por 3 numeros")
    }
}