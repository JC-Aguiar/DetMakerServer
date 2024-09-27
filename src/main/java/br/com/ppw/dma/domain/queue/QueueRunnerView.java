package br.com.ppw.dma.domain.queue;

import br.com.ppw.dma.domain.ambiente.Ambiente;
import br.com.ppw.dma.domain.cliente.Cliente;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.sql.ResultSet;
import java.sql.SQLException;

import static lombok.AccessLevel.*;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = PRIVATE)
public class QueueRunnerView implements ResultSetExtractor<QueueRunnerView> {

    TaskQueue taskQueue;
    Ambiente ambiente;
    Cliente cliente;

    @Override
    public QueueRunnerView extractData(ResultSet rs)
    throws SQLException, DataAccessException {
         var taskQueue = rs.getObject(1, TaskQueue.class);
         var ambiente = rs.getObject(2, Ambiente.class);
         var cliente = rs.getObject(3, Cliente.class);
         log.info(taskQueue.toString());
         log.info(ambiente.toString());
         log.info(cliente.toString());
        return new QueueRunnerView(taskQueue, ambiente, cliente);
    }
//    TaskQueue getTaskQueue();
//    Cliente getCliente();
//    Ambiente getAmbiente();

}
