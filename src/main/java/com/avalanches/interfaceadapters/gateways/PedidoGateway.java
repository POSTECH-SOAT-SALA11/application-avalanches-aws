package com.avalanches.interfaceadapters.gateways;

import com.avalanches.enterprisebusinessrules.entities.Pedido;
import com.avalanches.enterprisebusinessrules.entities.PedidoProduto;
import com.avalanches.enterprisebusinessrules.entities.StatusPedido;
import com.avalanches.frameworksanddrivers.databases.interfaces.BancoDeDadosContextoInterface;
import com.avalanches.interfaceadapters.gateways.interfaces.PedidoGatewayInterface;
import com.avalanches.interfaceadapters.presenters.interfaces.JsonPresenterInterface;
import io.lettuce.core.api.sync.RedisCommands;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.webjars.NotFoundException;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class PedidoGateway implements PedidoGatewayInterface {

    private final RedisCommands<String, String> redisCommands;
    private final JdbcOperations jdbcOperations;
    private final JsonPresenterInterface jsonPresenter;

    public PedidoGateway(BancoDeDadosContextoInterface bancoDeDadosContexto,
                         JsonPresenterInterface jsonPresenter) {
        this.jdbcOperations = bancoDeDadosContexto.getJdbcTemplate();
        this.redisCommands = bancoDeDadosContexto.getRedisCommands();
        this.jsonPresenter = jsonPresenter;
    }

    @Override
    public void cadastrar(Pedido pedido) {
        cadastrarPostgres(pedido);
        cadastrarRedis(pedido);
    }

    @Override
    public void cadastrarProdutosPorPedido(Integer idPedido, PedidoProduto pedidoProduto) {
         cadastrarProdutosPorPedidoPostgres(idPedido, pedidoProduto);
    }

    @Override
    public void atualizaStatus(Integer idPedido, StatusPedido statusPedido) {
        atualizaStatusPostgres(idPedido, statusPedido);
        atualizaStatusRedis(idPedido, statusPedido);
    }

    @Override
    public boolean verificaPedidoExiste(Integer idPedido) {
        return verificaPedidoExisteRedis(idPedido) || verificaPedidoExistePostgres(idPedido);
    }

    @Override
    public List<Pedido> listar() {
        return listarPostgres();
    }

    public String buscarStatusPedido(Integer idPedido) {
        String statusPedidoRedis = buscarStatusPedidoRedis(idPedido);
        if (statusPedidoRedis != null) return statusPedidoRedis;
        return buscarStatusPedidoPostgres(idPedido);
    }

    private void cadastrarPostgres(Pedido pedido) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcOperations.update(
                new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(
                                "INSERT INTO pedido (status, valor, datacriacao, datafinalizacao, idcliente) VALUES (?, ?, ? ,? ,?)",
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
    }

    private void cadastrarRedis(Pedido pedido) {
        String pedidoSerializado = jsonPresenter.serialize(pedido);
        redisCommands.set(getPedidoKey(pedido), pedidoSerializado);
    }

    private void cadastrarProdutosPorPedidoPostgres(Integer idPedido, PedidoProduto pedidoProduto) {
        jdbcOperations.update("INSERT INTO pedido_produto (idPedido, idProduto, quantidade, valorUnitario) VALUES (?, ?, ?, ?);",
                idPedido,
                pedidoProduto.getIdProduto(),
                pedidoProduto.getQuantidade(),
                pedidoProduto.getValorUnitario());
    }

    private boolean verificaPedidoExisteRedis(Integer idPedido) {
        return redisCommands.exists(getPedidoKey(idPedido)) > 0;
    }

    private boolean verificaPedidoExistePostgres(Integer idPedido) {
        String sql = "SELECT COUNT(*) FROM pedido WHERE id = ?";
        Integer count = jdbcOperations.queryForObject(sql, new Object[]{idPedido}, Integer.class);
        return count != null && count > 0;
    }

    private @Nullable List<Pedido> listarPostgres() {
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

    private void atualizaStatusPostgres(Integer idPedido, StatusPedido statusPedido) {
        jdbcOperations.update("UPDATE pedido SET status=? WHERE id=?",
                statusPedido.getValue(),
                idPedido
        );
    }

    private void atualizaStatusRedis(Integer idPedido, StatusPedido statusPedido) {
        String redisPedido = redisCommands.get(getPedidoKey(idPedido));
        if (redisPedido != null) {
            Pedido pedido = jsonPresenter.deserialize(redisPedido, Pedido.class);
            pedido.setStatus(statusPedido);
            String pedidoSerializado = jsonPresenter.serialize(pedido);
            redisCommands.set(getPedidoKey(pedido), pedidoSerializado);
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

    private String buscarStatusPedidoRedis(Integer idPedido) {
        String redisPedido = redisCommands.get(getPedidoKey(idPedido));

        if (redisPedido != null) {
            Pedido pedido = jsonPresenter.deserialize(redisPedido, Pedido.class);
            return pedido.getStatus().getValue();
        }

        return null;
    }

    private String buscarStatusPedidoPostgres(Integer idPedido) {
        String sql = "SELECT status FROM pedido WHERE id = ?";
        String status = jdbcOperations.queryForObject(sql, new Object[]{idPedido}, String.class);
        return status;
    }

    private static @NotNull String getPedidoKey(Pedido pedido) {
        return getPedidoKey(pedido.getId());
    }

    private static @NotNull String getPedidoKey(Integer pedidoId) {
        return "pedido:" + pedidoId;
    }

}
