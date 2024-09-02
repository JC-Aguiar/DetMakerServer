package br.com.ppw.dma.domain.master;

import lombok.Getter;

public enum QueryMethod {
	DDL("Data Definition Language"),
	//	DDL é usado para definir a estrutura dos objetos no banco de dados, como tabelas, índices, visões e esquemas. Exemplos de comandos DDL incluem CREATE, ALTER e DROP.
	DML("Data Manipulation Language"),
	//	DML é usado para manipular os dados dentro do banco de dados. Isso inclui operações como inserir, atualizar, excluir e recuperar dados.
	DQL("Data Query Language"),
	// DQL é usado para realizar consultas ao banco de dados.
	DCL("Data Control Language"),
	//	DCL é usado para gerenciar as permissões e privilégios no banco de dados. Comandos DCL incluem GRANT para conceder permissões e REVOKE para revogar permissões.
	TCL("Transaction Control Language");
	//	TCL é usado para gerenciar as transações no banco de dados. Comandos TCL incluem COMMIT para confirmar as alterações feitas durante uma transação e ROLLBACK para desfazer as alterações e restaurar o estado anterior.

	@Getter
	public final String fullName;

	QueryMethod(String fullName) {
		this.fullName = fullName;
	}
}
