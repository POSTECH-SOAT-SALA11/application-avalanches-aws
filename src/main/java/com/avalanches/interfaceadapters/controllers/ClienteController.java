package com.avalanches.interfaceadapters.controllers;

import com.avalanches.applicationbusinessrules.usecases.ClienteUseCase;
import com.avalanches.enterprisebusinessrules.entities.Cliente;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.controllers.interfaces.ClienteControllerInterface;
import com.avalanches.interfaceadapters.gateways.ClienteGateway;
import com.avalanches.interfaceadapters.gateways.interfaces.ClienteGatewayInterface;
import com.avalanches.interfaceadapters.presenters.ClientePresenter;
import com.avalanches.interfaceadapters.presenters.dtos.ClienteDto;
import com.avalanches.interfaceadapters.presenters.interfaces.ClientePresenterInterface;

public class ClienteController implements ClienteControllerInterface {

    @Override
    public void cadastrar(Cliente cliente, BancoDeDadosContextoInterface bancoDeDadosContexto) {
        ClienteGatewayInterface clienteGateway = new ClienteGateway(bancoDeDadosContexto);
        ClienteUseCase clienteUseCase = new ClienteUseCase();
        clienteUseCase.cadastrarCliente(cliente, clienteGateway);
    }

    @Override
    public ClienteDto consultar(String cpf, BancoDeDadosContextoInterface bancoDeDadosContexto) {
        ClienteGatewayInterface clienteGateway = new ClienteGateway(bancoDeDadosContexto);
        ClienteUseCase clienteUseCase = new ClienteUseCase();
        Cliente cliente = clienteUseCase.consultar(cpf, clienteGateway);
        ClientePresenterInterface clientePresenter = new ClientePresenter();
        return clientePresenter.clienteToDto(cliente);
    }

    @Override
    public void excluir(String cpf, BancoDeDadosContextoInterface bancoDeDadosContexto) {
        ClienteGatewayInterface clienteGateway = new ClienteGateway(bancoDeDadosContexto);
        ClienteUseCase clienteUseCase = new ClienteUseCase();
        clienteUseCase.excluir(cpf, clienteGateway);
    }
}
