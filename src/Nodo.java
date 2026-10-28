
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * Nodo del arbol de busqueda.
 */
public final class Nodo implements Comparable<Nodo> {

    private final Estado estado;
    private final Nodo padre;
    private final int accion;
    private final int profundidad;
    private final float costoAcumulado;

    private int heuristica;
    private int valor;
    private int hijosPendientes;
    private boolean expandido;
    private boolean reexpandible;

    /**
     * Crea la raiz del arbol.
     */
    public Nodo(Estado estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado no puede ser null");
        }

        this.estado = estado;
        this.padre = null;
        this.accion = -1;
        this.profundidad = 0;
        this.costoAcumulado = 0.0f;
        this.heuristica = 0;
        this.valor = 0;
    }

    /**
     * Crea un nodo hijo con coste de accion 1.
     */
    public Nodo(Nodo padre, int accion) {
        this(padre, accion, 1.0f);
    }

    /**
     * Crea un nodo hijo con el coste indicado.
     */
    public Nodo(Nodo padre, int accion, float costoAccion) {
        if (padre == null) {
            throw new IllegalArgumentException("El padre no puede ser null");
        }
        if (costoAccion < 0.0f || !Float.isFinite(costoAccion)) {
            throw new IllegalArgumentException("El coste de la accion no es valido");
        }

        this.padre = padre;
        this.accion = accion;
        this.estado = padre.estado.aplicar(accion);
        this.profundidad = padre.profundidad + 1;
        this.costoAcumulado = padre.costoAcumulado + costoAccion;
        this.heuristica = 0;
        this.valor = (int) this.costoAcumulado;
    }

    public Estado estado() {
        return estado;
    }

    public Nodo padre() {
        return padre;
    }

    public int accion() {
        return accion;
    }

    public int profundidad() {
        return profundidad;
    }

    public float costoAcumulado() {
        return costoAcumulado;
    }

    public void fijarHeuristica(int h) {
        this.heuristica = h;
    }

    public int valor() {
        return valor;
    }

    public void fijarValor(int v) {
        this.valor = v;
    }

    public void restarHijo() {
        if (hijosPendientes > 0) {
            hijosPendientes--;
        }
    }

    public void reexpandir() {
        this.expandido = false;
        this.reexpandible = true;
    }

    public void marcarExpandido() {
        this.expandido = true;
        this.reexpandible = false;
    }

    public boolean completado() {
        return expandido && hijosPendientes == 0;
    }

    public boolean agotado() {
        return expandido && hijosPendientes == 0 && !reexpandible;
    }

    public void respaldarMinimo(int v) {
        this.valor = Math.min(this.valor, v);
    }

    public boolean esRaiz() {
        return padre == null;
    }

    @Override
    public int compareTo(Nodo otro) {
        int comparacion = Integer.compare(this.valor, otro.valor);

        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Float.compare(this.costoAcumulado, otro.costoAcumulado);

        if (comparacion != 0) {
            return comparacion;
        }

        comparacion = Integer.compare(this.profundidad, otro.profundidad);

        if (comparacion != 0) {
            return comparacion;
        }

        return 0;
    }

    /**
     * Devuelve el camino desde la raiz hasta este nodo.
     */
    public ObjectList<Nodo> camino() {
        ObjectList<Nodo> resultado = new ObjectArrayList<>();

        for (Nodo actual = this; actual != null; actual = actual.padre) {
            resultado.add(actual);
        }

        for (int i = 0, j = resultado.size() - 1; i < j; i++, j--) {
            Nodo temporal = resultado.get(i);
            resultado.set(i, resultado.get(j));
            resultado.set(j, temporal);
        }

        return resultado;
    }

    /**
     * Representa las acciones del camino desde la raiz hasta la solucion.
     */
    public String caminoComoTexto() {
        if (esRaiz()) {
            return "(ya resuelto)";
        }

        StringBuilder resultado = new StringBuilder();

        for (Nodo nodo : camino()) {
            if (nodo.esRaiz()) {
                continue;
            }

            if (resultado.length() > 0) {
                resultado.append(' ');
            }

            resultado.append(Estado.accionComoTexto(nodo.accion));
        }

        return resultado.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Nodo otro)) {
            return false;
        }
        return estado.equals(otro.estado);
    }

    @Override
    public int hashCode() {
        return estado.hashCode();
    }

    @Override
    public String toString() {
        return "Nodo{estado=" + estado
                + ", profundidad=" + profundidad
                + ", costoAcumulado=" + costoAcumulado
                + ", valor=" + valor + '}';
    }
}
