
import java.util.function.ToIntFunction;

/** Motor de búsqueda del puzzle Loopover. */
public final class Busqueda {

    public enum Estrategia {
        PROFUNDIDAD,
        ANCHURA,
        COSTO_UNIFORME,
        VORAZ,
        A_ESTRELLA,
        A_ESTRELLA_ACOTADA
    }

    private final Estado estadoInicial;
    private final Estrategia estrategia;
    private final int profundidadMaxima;
    private final int maxNodosArbol;
    private final ToIntFunction<Estado> heuristica;
    private final long maxVisitados;

    private int nodosExpandidos;
    private int estadosVisitados;
    private long tiempoMs;
    private boolean limiteVisitadosAlcanzado;

    public Busqueda(
            Estado estadoInicial,
            Estrategia estrategia,
            int profundidadMaxima) {
        this(estadoInicial, estrategia, profundidadMaxima,
                10_000_000, estado -> 0, 0L);
    }

    public Busqueda(
            Estado estadoInicial,
            Estrategia estrategia,
            int profundidadMaxima,
            int maxNodosArbol) {
        this(estadoInicial, estrategia, profundidadMaxima,
                maxNodosArbol, estado -> 0, 0L);
    }

    public Busqueda(
            Estado estadoInicial,
            Estrategia estrategia,
            int profundidadMaxima,
            int maxNodosArbol,
            ToIntFunction<Estado> heuristica) {
        this(estadoInicial, estrategia, profundidadMaxima,
                maxNodosArbol, heuristica, 0L);
    }

    public Busqueda(
            Estado estadoInicial,
            Estrategia estrategia,
            int profundidadMaxima,
            int maxNodosArbol,
            ToIntFunction<Estado> heuristica,
            long maxVisitados) {

        if (estadoInicial == null) {
            throw new IllegalArgumentException(
                    "El estado inicial no puede ser null");
        }
        if (estrategia == null) {
            throw new IllegalArgumentException(
                    "La estrategia no puede ser null");
        }
        if (profundidadMaxima < 0) {
            throw new IllegalArgumentException(
                    "La profundidad maxima no puede ser negativa");
        }
        if (maxNodosArbol <= 0) {
            throw new IllegalArgumentException(
                    "El maximo de nodos debe ser positivo");
        }
        if (maxVisitados < 0) {
            throw new IllegalArgumentException(
                    "El maximo de visitados no puede ser negativo");
        }
        if (estrategia == Estrategia.A_ESTRELLA_ACOTADA) {
            throw new IllegalArgumentException(
                    "A_ESTRELLA_ACOTADA corresponde a la Tarea 3");
        }

        this.estadoInicial = estadoInicial;
        this.estrategia = estrategia;
        this.profundidadMaxima = profundidadMaxima;
        this.maxNodosArbol = maxNodosArbol;
        this.heuristica = heuristica != null
                ? heuristica
                : estado -> 0;
        this.maxVisitados = maxVisitados;
    }

    public Nodo buscar() {
        long inicio = System.nanoTime();

        nodosExpandidos = 0;
        estadosVisitados = 0;
        tiempoMs = 0;
        limiteVisitadosAlcanzado = false;

        try {
            Nodo raiz = new Nodo(estadoInicial);
            prepararValor(raiz);

            Visitados visitados = new Visitados();
            Frontera frontera = new Frontera(maxNodosArbol);

            visitados.registrarSiMejor(
                    raiz.estado().bitboard(),
                    valorVisitado(raiz));
            estadosVisitados = visitados.tamano();

            if (raiz.estado().esResuelto()) {
                return raiz;
            }

            frontera.insertar(raiz);

            while (!frontera.vacia()) {
                Nodo actual = frontera.extraerMejor();

                // La solución se acepta al extraerla de la frontera.
                if (actual.estado().esResuelto()) {
                    return actual;
                }

                // No expandir nodos que hayan alcanzado la profundidad máxima.
                if (actual.profundidad() >= profundidadMaxima) {
                    continue;
                }

                nodosExpandidos++;

                for (Sucesor sucesor : actual.estado().sucesores()) {
                    Nodo hijo = new Nodo(
                            actual,
                            sucesor.accion(),
                            sucesor.costo());

                    prepararValor(hijo);

                    long clave = hijo.estado().bitboard();
                    float valor = valorVisitado(hijo);

                    boolean estadoNuevo = !visitados.contiene(clave);

                    // El límite cuenta estados distintos.
                    if (estadoNuevo
                            && maxVisitados > 0
                            && visitados.tamano() >= maxVisitados) {
                        limiteVisitadosAlcanzado = true;
                        return null;
                    }

                    // Registrar solo estados nuevos o caminos mejores.
                    boolean mejor = visitados.registrarSiMejor(clave, valor);

                    if (!mejor) {
                        continue;
                    }

                    estadosVisitados = visitados.tamano();

                    // Si el estado sigue en la frontera, reemplazar su nodo.
                    if (frontera.contiene(hijo)) {
                        frontera.actualizar(hijo);
                        continue;
                    }

                    // Evitar exceder la capacidad de la frontera.
                    if (frontera.tamano() >= maxNodosArbol) {
                        return null;
                    }

                    /*
                     * Si el estado ya se había expandido y ahora encontramos
                     * un camino mejor, vuelve a entrar en la frontera.
                     */
                    frontera.insertar(hijo);
                }
            }

            return null;
        } finally {
            tiempoMs = (System.nanoTime() - inicio) / 1_000_000;
        }
    }

    private void prepararValor(Nodo nodo) {
        switch (estrategia) {
            case PROFUNDIDAD:
                nodo.fijarValor(-nodo.profundidad());
                break;

            case ANCHURA:
                nodo.fijarValor(nodo.profundidad());
                break;

            case COSTO_UNIFORME:
                nodo.fijarValor(Math.round(nodo.costoAcumulado()));
                break;

            case VORAZ:
                nodo.fijarValor(
                        heuristica.applyAsInt(nodo.estado()));
                break;

            case A_ESTRELLA:
                nodo.fijarValor(Math.round(
                        nodo.costoAcumulado()
                                + heuristica.applyAsInt(nodo.estado())));
                break;

            default:
                throw new IllegalArgumentException(
                        "Estrategia no soportada: " + estrategia);
        }
    }

    private float valorVisitado(Nodo nodo) {
        switch (estrategia) {
            case PROFUNDIDAD:
            case ANCHURA:
                return nodo.profundidad();

            case COSTO_UNIFORME:
            case VORAZ:
            case A_ESTRELLA:
                return nodo.costoAcumulado();

            default:
                throw new IllegalArgumentException(
                        "Estrategia no soportada: " + estrategia);
        }
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
}
