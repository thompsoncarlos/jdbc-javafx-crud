import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.List;

public class ProdutoGUI extends Application {
	private ProdutoDAO produtoDAO;
	private ObservableList<Produto> produtos;
	private TableView<Produto> tableView;
	private TextField nomeInput, quantidadeInput, precoInput;
	private ComboBox<String> statusComboBox;
	private Connection conexaoDB;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage palco) {
		conexaoDB = ConexaoDB.conectar();
		produtoDAO = new ProdutoDAO(conexaoDB); // Inicializa o DAO
		produtos = FXCollections.observableArrayList(produtoDAO.listarTodos()); // Carrega todos os produtos do DB

		palco.setTitle("Gerenciamento de Estoque de Produtos");

		VBox vbox = new VBox();
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);

		HBox nomeProdudoBox = new HBox();
		nomeProdudoBox.setSpacing(10);
		Label nomeLabel = new Label("Produto:");
		nomeInput = new TextField();
		nomeProdudoBox.getChildren().addAll(nomeLabel, nomeInput);

		HBox quantidadeBox = new HBox();
		quantidadeBox.setSpacing(10);
		Label quantidadeLabel = new Label("Quantidade:");
		quantidadeInput = new TextField();
		quantidadeBox.getChildren().addAll(quantidadeLabel, quantidadeInput);

		HBox precoBox = new HBox();
		precoBox.setSpacing(10);
		Label precoLabel = new Label("Preço:");
		precoInput = new TextField();
		precoBox.getChildren().addAll(precoLabel, precoInput);

		HBox statusBox = new HBox();
		statusBox.setSpacing(10);
		Label statusLabel = new Label("Status:");
		statusComboBox = new ComboBox<>();
		statusComboBox.getItems().addAll("Estoque Normal", "Estoque Baixo");
		statusBox.getChildren().addAll(statusLabel, statusComboBox);

		Button addButton = new Button("Adicionar");
		addButton.setOnAction(e -> {
			String preco = precoInput.getText().replace(',', '.'); // Substitui vírgula por ponto no preco
			Produto produto = new Produto(nomeInput.getText(),
					Integer.parseInt(quantidadeInput.getText()),
					Double.parseDouble(preco),
					statusComboBox.getValue());
			produtoDAO.inserir(produto); // Insere novo produto na DB
			produtos.setAll(produtoDAO.listarTodos()); // Atualiza a lista de produtos na tela
			limparCampos(); // Limpa os campos de entrada para uma nova digitação
		});	

		Button updateButton = new Button("Atualizar");
		updateButton.setOnAction(e -> {
			Produto selectedProduto = tableView.getSelectionModel().getSelectedItem(); // Obtém o produto selecionado
			if (selectedProduto != null) {
				selectedProduto.setNome(nomeInput.getText());
				selectedProduto.setQuantidade(Integer.parseInt(quantidadeInput.getText()));
				String preco = precoInput.getText().replace(',', '.');
				selectedProduto.setPreco(Double.parseDouble(preco));
				selectedProduto.setStatus(statusComboBox.getValue());
				produtoDAO.atualizar(selectedProduto); // Atualiza o produto no DB
				produtos.setAll(produtoDAO.listarTodos()); // Atualiza a lista de produtos
				limparCampos();
			}
		});


		Button deleteButton = new Button("Excluir");
		deleteButton.setOnAction(e -> {
			Produto selectedProduto = tableView.getSelectionModel().getSelectedItem(); // Obtém o produto selecionado
			if (selectedProduto != null) {
				produtoDAO.excluir(selectedProduto.getId()); // Exclui o produto do DB
				produtos.setAll(produtoDAO.listarTodos()); // Atualiza a lista de produtos
				limparCampos(); // Limpa os campos de entrada
			}
		});	

		Button clearButton = new Button("Limpar");
		clearButton.setOnAction(e -> limparCampos());

		tableView = new TableView<>();
		tableView.setItems(produtos); // DEfine a lista de produtos na tabela
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS); // Ajusta o tamanho das colunas
		List<TableColumn<Produto, ?>> columns = List.of(
			criarColuna("ID", "id"),
			criarColuna("Produto", "nome"),
			criarColuna("Quantidade", "quantidade"),
			criarColuna("Preço", "preco"),
			criarColuna("Status", "status")
		);
		tableView.getColumns().addAll(columns);

		tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				nomeInput.setText(newSelection.getNome());
				quantidadeInput.setText(String.valueOf(newSelection.getQuantidade()));
				precoInput.setText(String.valueOf(newSelection.getPreco()));
				statusComboBox.setValue(newSelection.getStatus());
			}
		});

		HBox buttonBox = new HBox();
		buttonBox.setSpacing(10);
		buttonBox.getChildren().addAll(addButton, updateButton, deleteButton, clearButton);

		vbox.getChildren().addAll(nomeProdudoBox, quantidadeBox, precoBox, statusBox, buttonBox, tableView);

		Scene scene = new Scene(vbox, 800, 600);
		scene.getStylesheets().add("styles-produtos.css"); // Adiciona a folha de estilos
		palco.setScene(scene);
		palco.show();
	}

	// O método stop é automaticamente chamado quando a aplicação JavaFX é encerrada.
	@Override
	public void stop() {
		try {
			conexaoDB.close(); // Fecha a conexão com o DB.
		} catch (SQLException e) {
			System.err.println("Erro ao fechar conexão" + e.getMessage());
		}
	}

	// Limpar os campos de entreda do fomrulário
	private void limparCampos() {
		nomeInput.clear();
		quantidadeInput.clear();
		precoInput.clear();
		statusComboBox.setValue(null);
	}

	/**
	 * Criar uma coluna para a TableView.
	 * @param title O título da coluna que será exibido no cabeçalho.
	 * @param property A propriedadedo objeto Produto que esta coluna deve exibir.
	 * @return A coluna configurada para a TAbleView.
	 */ 
	private TableColumn<Produto, String> criarColuna(String title, String property) {
		TableColumn<Produto, String> col = new TableColumn<>(title);
		col.setCellValueFactory(new PropertyValueFactory<>(property)); // Define a propriedade da coluna
		return col;
	}

}