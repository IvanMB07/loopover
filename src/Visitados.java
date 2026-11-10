
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;

/**
 * Tabla de estados visitados que almacena el mejor valor obtenido
 * para cada estado visto.
 */
public final class Visitados {

    private final Long2IntOpenHashMap mapa;

    public Visitados() {
        this.mapa = new Long2IntOpenHashMap();
        this.mapa.defaultReturnValue(Integer.MAX_VALUE);
    }

    /**
     * Registra un estado si es nuevo o si se ha encontrado un valor mejor.
     */
    public boolean registrarSiMejor(long estado, int valor) {
        int anterior = mapa.get(estado);

        if (valor < anterior) {
            mapa.put(estado, valor);
            return true;
        }

        return false;
    }

    /**
     * Comprueba si un estado ya está registrado.
     */
    public boolean contiene(long estado) {
        return mapa.containsKey(estado);
    }

    /**
     * Devuelve el mejor valor registrado para un estado.
     */
    public int mejorValor(long estado) {
        return mapa.get(estado);
    }

    public int tamano() {
        return mapa.size();
    }
}
