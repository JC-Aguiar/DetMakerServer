package br.com.ppw.dma.master;

import br.com.ppw.dma.exception.SummaryErrorWithNoMessage;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;
import java.util.function.Function;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public class MasterSummary<T> {

    //TODO: Alterar no front
    final List<T> saved = new ArrayList<>();
    final Map<T, String> failed = new HashMap<>();


    public enum SummaryStatus {
        SUCESSO(HttpStatus.OK),
        PARCIAL(HttpStatus.PARTIAL_CONTENT),
        FALHA(HttpStatus.INTERNAL_SERVER_ERROR);

        public final HttpStatus httpStatus;

        SummaryStatus(HttpStatus httpStatus) {
            this.httpStatus = httpStatus;
        }
    }


    //TODO: javadoc
    public static <T> MasterSummary<T> startsPositive(Collection<T> initialRecords) {
        var summary = new MasterSummary<T>();
        summary.saved.addAll(initialRecords);
        return summary;
    }

    //TODO: javadoc
    public static <T> MasterSummary<T> startsNegative(Collection<T> initialRecords) {
        var summary = new MasterSummary<T>();
//        summary.failed.addAll(initialRecords);
        initialRecords.forEach(
            record -> summary.failed.put(record, "Não processado ainda."));
        return summary;
    }

    public List<T> getSaved() {
        return List.copyOf(saved);
    }

    public Map<T, String> getFailed() {
        return Map.copyOf(failed);
    }

    public MasterSummary<T> save(@NonNull T item) {
        saved.add(item);
        failed.remove(item);
        return this;
    }

    public MasterSummary<T> fail(@NonNull T item, @NonNull String motivo) {
        failed.put(item, motivo);
        saved.remove(item);
        return this;
    }

    public void tryAndSet(T obj, Function<T, Boolean> action) {
        try {
            if(action.apply(obj)) save(obj);
            else throw new SummaryErrorWithNoMessage();
        }
        catch(Exception e) {
            e.printStackTrace();
            fail(obj, e.getMessage());
        }
        catch(SummaryErrorWithNoMessage e) {
            fail(obj, e.getMessage());
        }
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

    public static <T> ResponseEntity<MasterSummary<T>> toResponseEntity(
        @NonNull MasterSummary<T> summary) {
        //----------------------------------------------------------------
        return ResponseEntity.status(summary.getStatus().httpStatus).body(summary);
    }

}
