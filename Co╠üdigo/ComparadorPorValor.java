import java.util.Comparator;

public class ComparadorPorValor implements Comparator<Pedido> {

	@Override
	public int compare(Pedido o1, Pedido o2) {
		int resultado = Double.compare(o1.valorFinal(), o2.valorFinal());
		if (resultado != 0) {
			return resultado;
		}

		return Integer.compare(o1.getIdPedido(), o2.getIdPedido());
	}
}
