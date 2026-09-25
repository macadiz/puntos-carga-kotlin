# Sistema de gestión de estaciones de carga para vehículos eléctricos — EcoCarga
## Contexto del caso
EcoCarga SpA opera una red de estaciones de carga para vehículos eléctricos en comunas de alta demanda. El área de tecnología ha decidido construir un sistema de consola en Kotlin como núcleo de la lógica de negocio, antes de integrarlo con la aplicación móvil Android que la empresa tiene en desarrollo.

La empresa necesita un sistema que controle la conexión y desconexión de vehículos eléctricos, aplique tarifas diferenciadas según el tipo de vehículo y el perfil del cliente, y genere reportes al cierre de cada turno. El sistema debe estar preparado para manejar situaciones de error sin interrumpir su funcionamiento.

El registro de conexión y desconexión implica comunicación con sensores físicos externos (medidores de energía). Esa comunicación toma tiempo. Por esta razón, el sistema debe procesar esas operaciones de forma asíncrona: no debe detenerse mientras espera respuesta del sensor.

__Importante:__ el sistema debe permanecer operativo ante cualquier error de datos. Un error
no debe detener ni reiniciar el programa.

### Glosario del sistema
- Punto de carga: unidad física donde se conecta un vehículo eléctrico a recargar. Cada punto de carga tiene un número único y un estado.
- Estado: condición actual de un punto de carga. Puede estar libre, en carga, en proceso de registro o fuera de servicio.
- Turno: período de operación del sistema desde que se inicializa hasta que se genera el reporte de cierre.
- Comprobante: documento emitido al desconectar un vehículo. Incluye tiempo de carga y monto cobrado.
- Tarifa base: costo por kWh antes de aplicar descuentos o recargos, según el tipo de vehículo.
- Cliente ocasional: sin beneficios especiales. Paga la tarifa completa.
- Cliente socio: con plan de suscripción mensual. Descuento del 20% sobre la tarifa calculada.
- Cliente adulto mayor: con credencial vigente. Descuento adicional del 50% sobre el monto con IVA.
- Código de vehículo: identificador único del vehículo eléctrico. Formato vigente: tres letras, tres dígitos (mayúsculas). Ejemplo: ECO123.
  
### Requerimientos del sistema
#### R1 — Tipos de vehículos eléctricos y reglas de cobro
El sistema debe reconocer tres tipos de vehículos: autos, motos y bicicletas de reparto. Cada tipo tiene su propia tarifa base y sus propias reglas de cobro. Los tres comparten un conjunto de datos comunes, pero cada uno determina el monto a cobrar de manera diferente.

Datos comunes a todos los vehículos:
- Código de vehículo (formato vigente). No cambia una vez registrado.
- Marca del vehículo. No cambia una vez registrada.
- Fecha y hora exacta de conexión a la estación de carga. No cambia una vez registrada.
- Tipo de cliente: ocasional, socio o adulto mayor. No cambia una vez registrado.

Reglas de cobro por tipo de vehículo:
- Auto eléctrico: tarifa base $300 por kWh estimado según el tiempo de carga. Si el cliente es socio, se aplica un descuento del 20% al calcular la energía consumida.
- Moto eléctrica: tarifa base $150 por kWh. Si el tiempo de carga es inferior a 15 minutos, el cobro es $0 independientemente del tipo de cliente.
- Bicicleta de reparto: tarifa base $60 por kWh. Existe una categoría adicional: carga rápida. Si la carga es rápida, se aplica un recargo del 25% sobre el valor calculado. Este dato se registra al conectar el vehículo y no cambia. El detalle en pantalla para una bicicleta debe indicar explícitamente si la carga es o no rápida.
  
#### R2 — Estados de los puntos de carga
Un punto de carga solo puede estar en uno de cuatro estados:
- Libre: disponible, puede recibir un nuevo vehículo.
- En carga: tiene un vehículo conectado. El sistema debe saber cuál.
- Procesando: está siendo registrado (conexión o desconexión) mientras se espera al sensor. Debe indicarse el motivo del procesamiento.
- Fuera de servicio: inhabilitado. Debe registrarse el motivo.

Toda lógica que dependa del estado debe contemplar los cuatro estados posibles. No es correcto asumir que un punto de carga siempre estará libre u ocupado.

#### R3 — Cálculo de tarifas y validación de datos
El monto que paga cada vehículo al desconectarse se calcula siempre en este orden:
1. Costo según el tiempo de carga y las reglas del tipo de vehículo (R1).
2. Se aplica el IVA del 19% sobre el resultado anterior.
3. Si el cliente tiene beneficio de adulto mayor, se aplica un 50% de descuento sobre el monto con IVA.
Validaciones:
- Un código de vehículo que no cumpla el formato del glosario es inválido; no se debe registrar el vehículo.
- Ninguna tarifa puede ser negativa ni igual a cero; si ocurre, se reporta como error de datos.
- El tipo de cliente solo puede ser uno de los tres valores definidos en el glosario.

Datos del sistema: la estación se llama "EcoCarga Providencia" y tiene capacidad para 8 puntos de carga. Debe registrarse cuánto se ha recaudado en total durante el turno y cuánto por cada tipo de vehículo.
#### R4 — Gestión del catálogo y consultas de negocio
El sistema debe responder a estas consultas:
- ¿Cuántos puntos de carga están disponibles en este momento?
- ¿Qué vehículos del historial del turno pertenecen a clientes socios?
- ¿Cuál es el ingreso promedio por vehículo atendido en el turno?
- ¿Cuáles son los códigos de todos los vehículos que se han desconectado durante el turno?
- ¿Qué vehículo estuvo cargando por más tiempo durante el turno?
- 
Reporte de cierre de turno: por cada vehículo atendido, mostrar número de comprobante,
tipo, código, tiempo de carga y monto pagado. Al final: total recaudado, cantidad de
vehículos atendidos, ingreso promedio, tipo de vehículo que más ingresos generó y
cantidad de puntos de carga disponibles al cierre.

#### R5 — Registro asíncrono de conexiones y desconexiones
Las operaciones con el sensor no son instantáneas; el sistema no debe bloquearse mientras
las procesa.

Conexión:
1. Se busca el primer punto de carga libre disponible.
2. Mientras espera confirmación del sensor, el punto queda en estado Procesando (registrando conexión).
3. La espera simula la comunicación con el sensor y dura 4 segundos.
4. Confirmada la operación, el punto pasa a En carga con el vehículo asignado. Se informa el resultado en pantalla.
   
Desconexión:
1. Se localiza el punto de carga que tiene el vehículo con el código indicado.
2. Mientras procesa la desconexión, el punto queda en estado Procesando (calculando tarifa).
3. La espera dura 5,5 segundos.
4. Completada la operación, se emite el comprobante, se agrega el vehículo al historial, se actualiza la recaudación total y se libera el punto. En ambas operaciones el sistema debe actuar según el estado actual del punto antes de proceder (por ejemplo, no asignar un vehículo a un punto que ya está en carga o fuera de servicio).

#### R6 — Comportamiento ante errores
El sistema debe seguir funcionando ante estas situaciones, mostrando un mensaje claro (sin trazas técnicas):
- Código de vehículo inválido: no se realiza el registro; se notifica el error.
- Resultado de tarifa inválido: monto negativo o cero donde no debería serlo; se notifica sin interrumpir la sesión.
- Vehículo no encontrado: al intentar desconectar un código que no está en la
estación.
- Estación sin capacidad: al intentar conectar sin puntos libres disponibles.
El sistema no debe terminar abruptamente por ninguno de estos errores; debe continuar
con las siguientes operaciones normalmente.

#### R7 — Organización técnica del proyecto
La solución debe distribuirse en archivos Kotlin con responsabilidades claramente
separadas (por ejemplo: modelos/tipos de vehículo, estados, estación, operaciones
asíncronas, menú/consola, cálculo de tarifas). No mezclar responsabilidades entre archivos.
Datos de prueba sugeridos
Vehículos a registrar:
- Auto — código ECO123 — BYD Dolphin — cliente: socio
- Auto — código SUN045 — Tesla Model 3 — cliente: ocasional
- Moto — código MOT210 — NIU NQi GT — cliente: ocasional
- Bicicleta — código BIC908 — Rappi eBike — cliente: adulto mayor — carga rápida: sí
- Bicicleta — código BIC077 — Cornershop eBike — cliente: ocasional — carga rápida: no
Prueba de error de código: 12ABC3 — formato inválido.
Tiempos de carga sugeridos al registrar desconexiones:
- ECO123 → 90 minutos (auto, socio — descuento 20%)
- SUN045 → 150 minutos (auto, ocasional — sin descuento)
- MOT210 → 10 minutos (moto — tarifa $0 por ser menos de 15 min)
- BIC908 → 70 minutos (bicicleta, carga rápida, adulto mayor)
- BIC077 → 35 minutos (bicicleta, ocasional, sin carga rápida)
