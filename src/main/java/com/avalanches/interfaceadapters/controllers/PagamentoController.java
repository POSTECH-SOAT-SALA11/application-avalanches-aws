package com.avalanches.interfaceadapters.controllers;

import com.avalanches.applicationbusinessrules.usecases.PagamentoUseCase;
import com.avalanches.enterprisebusinessrules.entities.StatusPagamento;
import com.avalanches.frameworksanddrivers.api.dto.WebHookMockParams;
import com.avalanches.frameworksanddrivers.api.dto.WebhookParams;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.controllers.interfaces.PagamentoControllerInterface;
import com.avalanches.interfaceadapters.gateways.PagamentoGateway;
import com.avalanches.interfaceadapters.gateways.interfaces.PagamentoGatewayInterface;

public class PagamentoController implements PagamentoControllerInterface {

    @Override
    public void webhook(WebhookParams webhook, BancoDeDadosContextoInterface bancoDeDadosContexto, WebHookMockParams webHookMockParams) {
        PagamentoGatewayInterface pagamentoGateway = new PagamentoGateway(bancoDeDadosContexto, webHookMockParams);
        PagamentoUseCase pagamentoUseCase = new PagamentoUseCase();
        pagamentoUseCase.cadastrar(webhook, pagamentoGateway);
    }

    @Override
    public StatusPagamento consultaStatus(Integer idPedido, BancoDeDadosContextoInterface bancoDeDadosContexto, WebHookMockParams webHookMockParams) {
        PagamentoGatewayInterface pagamentoGateway = new PagamentoGateway(bancoDeDadosContexto, webHookMockParams);
        PagamentoUseCase pagamentoUseCase = new PagamentoUseCase();
        return pagamentoUseCase.consultaStatus(idPedido, pagamentoGateway);
    }

}
