package com.avalanches.interfaceadapters.controllers.interfaces;

import com.avalanches.enterprisebusinessrules.entities.CategoriaProduto;
import com.avalanches.enterprisebusinessrules.entities.Produto;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.presenters.dtos.ProdutoDto;
import org.springframework.jdbc.core.JdbcOperations;

import java.util.List;

public interface ProdutoControllerInterface {
    void cadastrarProduto(Produto produto, BancoDeDadosContextoInterface bancoDeDadosContexto);

    List<ProdutoDto> consultarProdutos(CategoriaProduto categoriaProduto, BancoDeDadosContextoInterface bancoDeDadosContexto);

    void atualizarProduto(Produto produto, BancoDeDadosContextoInterface bancoDeDadosContexto);

    void excluirProduto(int id, BancoDeDadosContextoInterface bancoDeDadosContexto);
}
