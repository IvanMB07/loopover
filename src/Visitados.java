
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;

/** Tabla de estados visitados. */
public final class Visitados {

    private final Long2FloatOpenHashMap estados;

    public Visitados() {
        this.estados = new Long2FloatOpenHashMap();
    }

    public boolean registrarSiMejor(long estado, float valor) {
        if (!estados.containsKey(estado) || valor < estados.get(estado)) {
            estados.put(estado, valor);
            return true;
        }

        return false;
    }

    public int tamano() {
        return estados.size();
    }
}
