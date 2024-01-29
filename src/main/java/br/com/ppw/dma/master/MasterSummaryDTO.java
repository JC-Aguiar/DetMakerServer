package br.com.ppw.dma.master;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class MasterSummaryDTO<T> {

    final List<T> saved = new ArrayList<>();
    final List<T> failed = new ArrayList<>();

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
}
