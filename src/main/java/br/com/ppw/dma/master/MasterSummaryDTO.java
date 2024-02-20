package br.com.ppw.dma.master;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class MasterSummaryDTO<T> {

    final List<T> saved = new ArrayList<>();
    final List<T> failed = new ArrayList<>();
    SummaryStatus status;

    public enum SummaryStatus {
        SUCESSO(), PARCIAL(), FALHA();
    }

    public MasterSummaryDTO<T> save(T item) {
        saved.add(item);
        return this;
    }

    public MasterSummaryDTO<T> fail(T item) {
        failed.add(item);
        return this;
    }

    public int totalSize() {
        return saved.size() + failed.size();
    }

    public SummaryStatus getStatus() {
        if(saved.isEmpty())
            return SummaryStatus.FALHA;
        else if(failed.isEmpty())
            return SummaryStatus.SUCESSO;
        return SummaryStatus.PARCIAL;
    }

}
