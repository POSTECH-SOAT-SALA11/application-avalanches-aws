package com.avalanches.interfaceadapters.controllers.interfaces;

import com.avalanches.enterprisebusinessrules.entities.Cliente;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.presenters.dtos.ClienteDto;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;

public interface ClienteControllerInterface {
    void cadastrar(Cliente cliente, BancoDeDadosContextoInterface bancoDeDadosContexto);

    ClienteDto consultar(String cpf, BancoDeDadosContextoInterface bancoDeDadosContexto);

    void excluir(String cpf, BancoDeDadosContextoInterface bancoDeDadosContexto);
}
