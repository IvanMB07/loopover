import java.util.function.LongToIntFunction;
import java.util.List;

/**
 * Busqueda en espacio de estados con estrategias configurables y poda por
 * estados visitados.
 *
 * El mapa de visitados almacena el mejor valor visto para cada bitboard.
 * Al extraer un nodo de la frontera se poda si su estado ya esta registrado
 * con un valor igual o mejor; en caso contrario se registra y se expanden
 * todos sus sucesores.
 *
 * TODO Tarea 3: completar asignarValor() para VORAZ, A_ESTRELLA,
 * A_ESTRELLA_ACOTADA e implementar hacerSitio() para SMA*.
 */
public final class Busqueda {

    public enum Estrategia {
        PROFUNDIDAD,
        ANCHURA,
        COSTO_UNIFORME,
        VORAZ,
        A_ESTRELLA,
        A_ESTRELLA_ACOTADA
    }

    private static final int MAX_NODOS_POR_DEFECTO = 10_000_000;

    private final Estado estadoInicial;
    private final Estrategia estrategia;
    private final int profundidadMaxima;
    private final int maxNodosArbol;
    private final LongToIntFunction heuristica;
    private final long maxVisitados;

    private int nodosEnArbol;
    private int nodosExpandidos;
    private int estadosVisitados;
    private long tiempoMs;
    private boolean limiteVisitadosAlcanzado;

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima) {
        this(estadoInicial, estrategia, profundidadMaxima, MAX_NODOS_POR_DEFECTO,
                Heuristicas::heuristicaManhattanToroidal, 0L);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
            int maxNodosArbol) {
        this(estadoInicial, estrategia, profundidadMaxima, maxNodosArbol,
                Heuristicas::heuristicaManhattanToroidal);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
            int maxNodosArbol, LongToIntFunction heuristica) {
        this(estadoInicial, estrategia, profundidadMaxima, maxNodosArbol, heuristica, 0L);
    }

    public Busqueda(Estado estadoInicial, Estrategia estrategia, int profundidadMaxima,
            int maxNodosArbol, LongToIntFunction heuristica, long maxVisitados) {
        if (estadoInicial == null)
            throw new IllegalArgumentException("El estado inicial no puede ser nulo");
        if (estrategia == null)
            throw new IllegalArgumentException("La estrategia no puede ser nula");
        if (profundidadMaxima < 0)
            throw new IllegalArgumentException("La profundidad maxima debe ser >= 0: " + profundidadMaxima);
        if (estrategia == Estrategia.A_ESTRELLA_ACOTADA && maxNodosArbol < 1)
            throw new IllegalArgumentException("A* acotada requiere maximo de nodos del arbol >= 1");
        if (maxVisitados < 0)
            throw new IllegalArgumentException("El limite de visitados debe ser >= 0");
        this.estadoInicial = estadoInicial;
        this.estrategia = estrategia;
        this.profundidadMaxima = profundidadMaxima;
        this.maxNodosArbol = maxNodosArbol;
        this.heuristica = heuristica;
        this.maxVisitados = maxVisitados;
    }

    /**
     * Ejecuta la busqueda y devuelve el nodo solucion, o null si no se encontro.
     */

    public Nodo buscar() {
        long inicio = System.nanoTime();

        nodosEnArbol = 0;
        nodosExpandidos = 0;
        estadosVisitados = 0;
        tiempoMs = 0;
        limiteVisitadosAlcanzado = false;

        Visitados visitados = new Visitados();
        Frontera frontera = new Frontera(maxNodosArbol);

        try {
            // Crear e insertar el nodo raíz.
            Nodo raiz = new Nodo(estadoInicial);
            asignarValor(raiz);

            visitados.registrarSiMejor(
                    raiz.estado().bitboard(),
                    valorVisitado(raiz));

            estadosVisitados = visitados.tamano();
            frontera.insertar(raiz);

            // Bucle principal de búsqueda.
            while (!frontera.vacia()) {

                Nodo actual = frontera.extraerMejor();

                long bitboard = actual.estado().bitboard();
                int mejorValor = visitados.mejorValor(bitboard);

                // Descartar nodos cuyo camino ya no sea el mejor conocido.
                if (valorVisitado(actual) > mejorValor) {
                    continue;
                }

                // Comprobar si hemos encontrado la solución.
                if (actual.estado().esResuelto()) {
                    return actual;
                }

                // No expandir nodos que hayan alcanzado la profundidad máxima.
                if (actual.profundidad() >= profundidadMaxima) {
                    continue;
                }

                // Obtener los sucesores del estado actual.
                List<Sucesor> sucesores = actual.estado().sucesores();
                nodosExpandidos++;

                for (int i = 0; i < sucesores.size(); i++) {

                    Sucesor sucesor = sucesores.get(i);

                    // El constructor de Nodo espera el índice de Estado.ACCIONES.
                    Nodo hijo = new Nodo(
                            actual,
                            i,
                            sucesor.costo());

                    asignarValor(hijo);

                    long clave = hijo.estado().bitboard();
                    int valor = valorVisitado(hijo);

                    boolean esNuevo = !visitados.contiene(clave);

                    // Comprobar el límite antes de registrar un estado nuevo.
                    if (esNuevo
                            && maxVisitados > 0
                            && visitados.tamano() >= maxVisitados) {

                        limiteVisitadosAlcanzado = true;
                        return null;
                    }

                    // Registrar únicamente estados nuevos o caminos mejores.
                    if (!visitados.registrarSiMejor(clave, valor)) {
                        continue;
                    }

                    estadosVisitados = visitados.tamano();

                    // Comprobar la capacidad máxima de la frontera.
                    if (frontera.tamano() >= maxNodosArbol) {
                        return null;
                    }

                    frontera.insertar(hijo);
                }
            }

            return null;

        } finally {
            tiempoMs = (System.nanoTime() - inicio) / 1_000_000L;
        }
    }

    /**
     * Devuelve el valor que debe almacenarse en Visitados.
     */
    private int valorVisitado(Nodo nodo) {
        return switch (estrategia) {
            case PROFUNDIDAD -> nodo.profundidad();
            case ANCHURA -> nodo.profundidad();
            case COSTO_UNIFORME -> Math.round(nodo.costoAcumulado());
            default -> throw new UnsupportedOperationException(
                    "Estrategia informada: Tarea 3");
        };
    }

    public int nodosExpandidos() {
        return nodosExpandidos;
    }

    public int estadosVisitados() {
        return estadosVisitados;
    }

    public long tiempoMs() {
        return tiempoMs;
    }

    public boolean limiteVisitadosAlcanzado() {
        return limiteVisitadosAlcanzado;
    }

    /**
     * Asigna el valor de ordenamiento del nodo segun la estrategia.
     */
    private void asignarValor(Nodo nodo) {
        switch (estrategia) {
            case PROFUNDIDAD ->
                nodo.fijarValor(-nodo.profundidad());

            case ANCHURA ->
                nodo.fijarValor(nodo.profundidad());

            case COSTO_UNIFORME ->
                nodo.fijarValor(nodo.costoAcumulado());

            case VORAZ, A_ESTRELLA, A_ESTRELLA_ACOTADA ->
                throw new UnsupportedOperationException("TODO: Tarea 3 - estrategia informada");
            default -> {
            }
        }
    }

    /**
     * Para SMA*: libera memoria expulsando una hoja descartable y devuelve true
     * si lo consiguio, false si no habia ninguna.
     *
     * TODO: implementar en Tarea 3.
     */
    private boolean hacerSitio(Frontera frontera) {
        throw new UnsupportedOperationException("TODO: Tarea 3 - hacerSitio()");
    }
}
