import java.util.Comparator;

/**
 * Critério C - Índice de Economia (decrescente).
 * O índice de economia é a diferença entre o valor de catálogo atual e o valor
 * efetivamente pago.
 * Desempate 1: Valor Final do Pedido (crescente).
 * Desempate 2: Código Identificador do pedido (crescente).
 */
public class ComparadorCriterioC implements Comparator<Pedido> {

    @Override
    public int compare(Pedido o1, Pedido o2) {
        int resultado = Double.compare(o2.indiceEconomia(), o1.indiceEconomia());
        if (resultado != 0) {
            return resultado;
        }

        resultado = Double.compare(o1.valorFinal(), o2.valorFinal());
        if (resultado != 0) {
            return resultado;
        }

        return Integer.compare(o1.getIdPedido(), o2.getIdPedido());
    }
}
