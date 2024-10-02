package com.avalanches.applicationbusinessrules.usecases;

import com.avalanches.applicationbusinessrules.usecases.interfaces.PedidoUseCaseInterface;
import com.avalanches.enterprisebusinessrules.entities.Pedido;
import com.avalanches.enterprisebusinessrules.entities.PedidoProduto;
import com.avalanches.enterprisebusinessrules.entities.StatusPedido;
import com.avalanches.frameworksanddrivers.databases.StatusPedidoInvalidoException;
import com.avalanches.interfaceadapters.gateways.interfaces.ClienteGatewayInterface;
import com.avalanches.interfaceadapters.gateways.interfaces.PagamentoGatewayInterface;
import com.avalanches.interfaceadapters.gateways.interfaces.PedidoGatewayInterface;
import com.avalanches.interfaceadapters.gateways.interfaces.ProdutoGatewayInterface;
import org.webjars.NotFoundException;

import java.math.BigDecimal;
import java.util.List;

public class PedidoUseCase implements PedidoUseCaseInterface {

    @Override
    public Integer cadastrar(Pedido pedido,
                             PedidoGatewayInterface pedidoGateway,
                             ProdutoGatewayInterface produtoGateway,
                             PagamentoGatewayInterface pagamentoGateway,
                             ClienteGatewayInterface clienteGateway) {
        if(!clienteGateway.verificaClienteExiste(pedido.getIdCliente())) {
            throw new NotFoundException("O cliente não foi encontrado.");
        }

        for(PedidoProduto p: pedido.getListaProduto())
            if(!produtoGateway.verificaProdutoExiste(p.getIdProduto())) {
                throw new NotFoundException("O produto " + p.getIdProduto() + " não foi encontrado.");
            }

        pedido.setValor(this.calcularValorTotal(pedido, produtoGateway));

        pedidoGateway.cadastrar(pedido);
        PagamentoUseCase pagamentoUseCase = new PagamentoUseCase();
        pagamentoUseCase.efetuarPagamento(pedido.getId(), pagamentoGateway);

        for(PedidoProduto p: pedido.getListaProduto())
            pedidoGateway.cadastrarProdutosPorPedido(pedido.getId(), p);

        return pedido.getId();
    }

    @Override
    public List<Pedido> listar(PedidoGatewayInterface pedidoGateway) {
        return pedidoGateway.listar();
    }

    @Override
    public void atualizaStatus(Integer idPedido, StatusPedido statusPedido, PedidoGatewayInterface pedidoGateway) {
        if (!pedidoGateway.verificaPedidoExiste(idPedido))  {
            throw new NotFoundException("Pedido não encontrado.");
        }

        if(!VerificaStatusValido(idPedido, statusPedido, pedidoGateway)){
            throw new StatusPedidoInvalidoException(statusPedido);
        }

        pedidoGateway.atualizaStatus(idPedido, statusPedido);
    }

    private BigDecimal calcularValorTotal(Pedido pedido, ProdutoGatewayInterface produtoGateway) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        for(PedidoProduto p: pedido.getListaProduto()){
            if(!produtoGateway.verificaProdutoExiste(p.getIdProduto())) {
                throw new NotFoundException("O produto " + p.getIdProduto() + " não foi encontrado.");
            }
            BigDecimal valorProduto = p.getValorUnitario().multiply(BigDecimal.valueOf(p.getQuantidade()));
            valorTotal = valorTotal.add(valorProduto);
        }
        return valorTotal;
    }

    private boolean VerificaStatusValido(Integer idPedido, StatusPedido statusPedido, PedidoGatewayInterface pedidoGateway) {

        StatusPedido statusAtual = StatusPedido.fromValue(pedidoGateway.buscarStatusPedido(idPedido));

        StatusPedido proximoStatusValido = switch (statusAtual) {
            case RECEBIDO -> StatusPedido.EMPREPARACAO;
            case EMPREPARACAO -> StatusPedido.PRONTO;
            case PRONTO -> StatusPedido.FINALIZADO;
            default -> null;
        };

        return statusPedido == proximoStatusValido;
    }
}
