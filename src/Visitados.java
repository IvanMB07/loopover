
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

/** Tabla de estados visitados y mejor valor conocido para cada estado. */
public final class Visitados {

    private final Long2FloatOpenHashMap estados;

    public Visitados() {
        estados = new Long2FloatOpenHashMap();
    }

    /**
     * Registra un estado si es nuevo o si se ha encontrado un camino mejor.
     *
     * @return true si el estado se ha registrado o actualizado.
     */
    public boolean registrarSiMejor(long estado, float valor) {
        if (!estados.containsKey(estado) || valor < estados.get(estado)) {
            estados.put(estado, valor);
            return true;
        }

        return false;
    }

    public boolean contiene(long estado) {
        return estados.containsKey(estado);
    }

    public int tamano() {
        return estados.size();
    }
}
