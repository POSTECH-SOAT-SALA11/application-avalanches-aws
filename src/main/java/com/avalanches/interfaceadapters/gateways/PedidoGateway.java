package com.avalanches.interfaceadapters.gateways;


import com.avalanches.enterprisebusinessrules.entities.Pedido;
import com.avalanches.enterprisebusinessrules.entities.PedidoProduto;
import com.avalanches.enterprisebusinessrules.entities.StatusPedido;
import com.avalanches.frameworksanddrivers.databases.JsonMappingCustomException;
import com.avalanches.frameworksanddrivers.databases.JsonProcessingCustomException;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.gateways.interfaces.PedidoGatewayInterface;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.lettuce.core.api.sync.RedisCommands;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.webjars.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PedidoGateway implements PedidoGatewayInterface {

    private final RedisCommands<String, String> redisCommands;
    private final JdbcOperations jdbcOperations;

    public PedidoGateway(BancoDeDadosContextoInterface bancoDeDadosContexto) {
        this.jdbcOperations = bancoDeDadosContexto.getJdbcTemplate();
        this.redisCommands = bancoDeDadosContexto.getRedisCommands();
    }

    @Override
    public void cadastrar(Pedido pedido) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "insert into pedido (status, valor, datacriacao, datafinalizacao, idcliente) values (?, ?, ? ,? ,?)",
                                Statement.RETURN_GENERATED_KEYS
                        );
                        ps.setString(1, pedido.getStatus().getValue());
                        ps.setBigDecimal(2, pedido.getValor());
                        ps.setTimestamp(3, Timestamp.valueOf(pedido.getDataCriacao()));
                        ps.setTimestamp(4, Timestamp.valueOf(pedido.getDataFinalizacao()));
                        ps.setInt(5, pedido.getIdCliente());
                        return ps;
                    }
                },
                keyHolder
        );
        pedido.setId((int) keyHolder.getKeys().get("id"));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String pedidoSerializado = objectMapper.writeValueAsString(pedido);
            redisCommands.set(pedido.getId().toString(), pedidoSerializado);
        } catch (JsonMappingException ex) {
            throw new JsonMappingCustomException(ex.getMessage());
        } catch (JsonProcessingException ex) {
            throw new JsonProcessingCustomException(ex.getMessage());
        }
    }

    @Override
    public void cadastrarProdutosPorPedido(Integer idPedido, PedidoProduto pedidoProduto) {
        jdbcOperations.update("INSERT INTO pedido_produto (idPedido, idProduto, quantidade, valorUnitario) VALUES (?, ?, ?, ?);",
                idPedido,
                pedidoProduto.getIdProduto(),
                pedidoProduto.getQuantidade(),
                pedidoProduto.getValorUnitario());
    }

    @Override
    public void atualizaStatus(Integer idPedido, StatusPedido statusPedido) {
        jdbcOperations.update("UPDATE pedido SET status=? WHERE id=?",
                statusPedido.getValue(),
                idPedido
        );
    }
    
    @Override
    public boolean verificaPedidoExiste(Integer idPedido) {
        String sql = "SELECT COUNT(*) FROM pedido WHERE id = ?";
        Integer count = jdbcOperations.queryForObject(sql, new Object[]{idPedido}, Integer.class);
        return count != null && count > 0;
    }

    @Override
    public List<Pedido> listar() {
        try {
            String sql = "SELECT p.id, p.status, p.valor, p.datacriacao, p.datafinalizacao, p.idcliente, "
                    + "pp.idproduto, pp.quantidade, pp.valorunitario "
                    + "FROM pedido p "
                    + "LEFT JOIN pedido_produto pp ON p.id = pp.idpedido "
                    + "where P.status <> 'Finalizado'\n"
                    + "ORDER BY array_position(ARRAY['Pronto', 'EmPreparacao', 'Recebido']::varchar[], p.status),p.datacriacao";

            return jdbcOperations.query(sql, new PedidoResultSetExtractor());
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Produto não existe");
        }
    }


    private static class PedidoResultSetExtractor implements ResultSetExtractor<List<Pedido>> {

        @Override
        public List<Pedido> extractData(ResultSet rs) throws SQLException {
            Map<Integer, Pedido> pedidoMap = new LinkedHashMap<>();

            while (rs.next()) {
                Integer pedidoId = rs.getInt("id");
                Pedido pedido = pedidoMap.get(pedidoId);


                if (pedido == null) {
                    LocalDateTime dataCriacao = rs.getTimestamp("datacriacao").toLocalDateTime();
                    LocalDateTime dataFinalizacao = rs.getTimestamp("datafinalizacao").toLocalDateTime();
                    pedido = new Pedido(
                            pedidoId,
                            StatusPedido.fromValue(rs.getString("status")),
                            rs.getBigDecimal("valor"),
                            dataCriacao,
                            dataFinalizacao,
                            rs.getInt("idcliente")
                    );
                    pedidoMap.put(pedidoId, pedido);
                }

                if (rs.getInt("idproduto") != 0) {
                    PedidoProduto pedidoProduto = new PedidoProduto(
                            rs.getInt("idproduto"),
                            rs.getInt("quantidade"),
                            rs.getBigDecimal("valorunitario")
                    );
                    pedido.adicionarProduto(pedidoProduto);
                }
            }

            return new ArrayList<>(pedidoMap.values());
        }
    }


}
