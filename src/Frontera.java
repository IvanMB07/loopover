
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/** Frontera de la busqueda (heap min-max). */
public final class Frontera {

    private final ObjectArrayList<Nodo> nodos;
    private final Object2IntOpenHashMap<Nodo> posiciones;
    private final int maxNodos;

    public Frontera(int maxNodos) {
        if (maxNodos <= 0) {
            throw new IllegalArgumentException(
                    "El maximo de nodos debe ser positivo");
        }

        this.maxNodos = maxNodos;
        this.nodos = new ObjectArrayList<>();
        this.posiciones = new Object2IntOpenHashMap<>();
        this.posiciones.defaultReturnValue(-1);
    }

    public int tamano() {
        return nodos.size();
    }

    public boolean vacia() {
        return nodos.isEmpty();
    }

    public boolean contiene(Nodo nodo) {
        return posiciones.containsKey(nodo);
    }

    public void insertar(Nodo nodo) {
        if (nodo == null) {
            throw new IllegalArgumentException("El nodo no puede ser null");
        }
        if (nodos.size() >= maxNodos) {
            throw new IllegalStateException(
                    "La frontera ha alcanzado su capacidad maxima");
        }
        if (contiene(nodo)) {
            return;
        }

        int indice = nodos.size();
        nodos.add(nodo);
        posiciones.put(nodo, indice);
        subir(indice);
    }

    public Nodo extraerMejor() {
        if (nodos.isEmpty()) {
            return null;
        }

        Nodo mejor = nodos.get(0);
        int ultimoIndice = nodos.size() - 1;
        Nodo ultimo = nodos.remove(ultimoIndice);
        posiciones.removeInt(mejor);

        if (!nodos.isEmpty()) {
            nodos.set(0, ultimo);
            posiciones.put(ultimo, 0);
            bajar(0);
        }

        return mejor;
    }

    public Nodo eliminarPeorHojaNoRaiz() {
        throw new UnsupportedOperationException("TODO: Tarea 3");
    }

    private void subir(int indice) {
        int nivel = nivel(indice);

        if (nivel % 2 == 0) {
            int padre = padre(indice);

            if (padre >= 0 && comparar(indice, padre) > 0) {
                intercambiar(indice, padre);
                subirMax(padre);
            } else {
                subirMin(indice);
            }
        } else {
            int padre = padre(indice);

            if (padre >= 0 && comparar(indice, padre) < 0) {
                intercambiar(indice, padre);
                subirMin(padre);
            } else {
                subirMax(indice);
            }
        }
    }

    private void subirMin(int indice) {
        while (abuelo(indice) >= 0) {
            int abuelo = abuelo(indice);

            if (comparar(indice, abuelo) >= 0) {
                break;
            }

            intercambiar(indice, abuelo);
            indice = abuelo;
        }
    }

    private void subirMax(int indice) {
        while (abuelo(indice) >= 0) {
            int abuelo = abuelo(indice);

            if (comparar(indice, abuelo) <= 0) {
                break;
            }

            intercambiar(indice, abuelo);
            indice = abuelo;
        }
    }

    private void bajar(int indice) {
        if (nivel(indice) % 2 == 0) {
            bajarMin(indice);
        } else {
            bajarMax(indice);
        }
    }

    private void bajarMin(int indice) {
        while (true) {
            int candidato = descendienteExtremo(indice, true);

            if (candidato < 0 || comparar(candidato, indice) >= 0) {
                return;
            }

            if (padre(candidato) != indice) {
                intercambiar(candidato, indice);

                int padreCandidato = padre(candidato);
                if (comparar(candidato, padreCandidato) > 0) {
                    intercambiar(candidato, padreCandidato);
                }

                indice = candidato;
            } else {
                intercambiar(candidato, indice);
                return;
            }
        }
    }

    private void bajarMax(int indice) {
        while (true) {
            int candidato = descendienteExtremo(indice, false);

            if (candidato < 0 || comparar(candidato, indice) <= 0) {
                return;
            }

            if (padre(candidato) != indice) {
                intercambiar(candidato, indice);

                int padreCandidato = padre(candidato);
                if (comparar(candidato, padreCandidato) < 0) {
                    intercambiar(candidato, padreCandidato);
                }

                indice = candidato;
            } else {
                intercambiar(candidato, indice);
                return;
            }
        }
    }

    private int descendienteExtremo(int indice, boolean minimo) {
        int mejorIndice = -1;
        int inicio = indice * 2 + 1;
        int fin = Math.min(nodos.size() - 1, indice * 2 + 2);

        for (int i = inicio; i <= fin; i++) {
            if (mejorIndice < 0
                    || (minimo
                            ? comparar(i, mejorIndice) < 0
                            : comparar(i, mejorIndice) > 0)) {
                mejorIndice = i;
            }
        }

        int nietoInicio = inicio * 2 + 1;
        int nietoFin = Math.min(nodos.size() - 1, inicio * 2 + 4);

        for (int i = nietoInicio; i <= nietoFin; i++) {
            if (mejorIndice < 0
                    || (minimo
                            ? comparar(i, mejorIndice) < 0
                            : comparar(i, mejorIndice) > 0)) {
                mejorIndice = i;
            }
        }

        return mejorIndice;
    }

    private int comparar(int i, int j) {
        return nodos.get(i).compareTo(nodos.get(j));
    }

    private void intercambiar(int i, int j) {
        if (i == j) {
            return;
        }

        Nodo primero = nodos.get(i);
        Nodo segundo = nodos.get(j);

        nodos.set(i, segundo);
        nodos.set(j, primero);

        posiciones.put(segundo, i);
        posiciones.put(primero, j);
    }

    private int padre(int indice) {
        return indice == 0 ? -1 : (indice - 1) / 2;
    }

    private int abuelo(int indice) {
        int p = padre(indice);
        return p < 0 ? -1 : padre(p);
    }

    private int nivel(int indice) {
        return 31 - Integer.numberOfLeadingZeros(indice + 1);
    }
}
