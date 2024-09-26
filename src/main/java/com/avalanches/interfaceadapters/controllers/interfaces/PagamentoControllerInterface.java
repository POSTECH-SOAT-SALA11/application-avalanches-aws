package com.avalanches.interfaceadapters.controllers.interfaces;

import com.avalanches.enterprisebusinessrules.entities.StatusPagamento;
import com.avalanches.frameworksanddrivers.api.dto.WebHookMockParams;
import com.avalanches.frameworksanddrivers.api.dto.WebhookParams;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import org.springframework.jdbc.core.JdbcOperations;

public interface PagamentoControllerInterface {

    void webhook(WebhookParams webhook, BancoDeDadosContextoInterface bancoDeDadosContexto, WebHookMockParams webHookMockParams);

    StatusPagamento consultaStatus(Integer idPedido, BancoDeDadosContextoInterface bancoDeDadosContexto, WebHookMockParams webHookMockParams);

}
