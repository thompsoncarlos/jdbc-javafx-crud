
import java.sql.Connection;
import java.util.List;

public class Main3 {
    public static void main(String[] args) {
        try (Connection conexao = ConexaoDB.conectar()) {
            ProdutoDAO produtoDAO = new ProdutoDAO(conexao);

            // Lista todos os produtos (deve estar vazio neste ponto)
            mostrarProdutos(produtoDAO);

            // Excluir por ID
            // produtoDAO.excluir(3);

            // Excluir todos
            produtoDAO.excluirTodos();

			// Lista todos os produtos (deve estar vazio neste ponto)
			System.out.println("Lista após excluir todos");
            mostrarProdutos(produtoDAO);

        } catch (Exception e) {
            System.err.println("Erro geral: " + e.getMessage());
        }
    }

    // Método para listar os produtos
    private static void mostrarProdutos(ProdutoDAO produtoDAO) {
        List<Produto> todosProdutos = produtoDAO.listarTodos();
        if (todosProdutos.isEmpty()) {
            System.out.println("Nenhum produto encontrado.");
        } else {
            System.out.println("Lista de produtos:");
            for (Produto p : todosProdutos) {
                System.out.println(p.getId() + ": " + p.getNome() + " - " + p.getPreco());
            }
        }
    }
}