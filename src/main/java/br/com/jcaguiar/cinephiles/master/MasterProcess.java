package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.IntStream;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcess<OBJ> {

    Page<OBJ> result;
    final Map<Integer, String> errors = new HashMap<>();

//    public MasterProcess(@NotNull List<ProcessLine<OBJ>> processes, @NotNull Page<?> page) {
//        this.result = page;
//        IntStream.range(0, processes.size())
//            .forEach(i -> {
//                final ProcessLine obj = processes.get(i);
//                if(obj.isError()) errors.put(i, obj.getErrorCause());
//            });
//    }

    public MasterProcess(@NotNull ProcessLine<OBJ> process) {
        if(process.isError()) errors.put(0, process.getErrorCause());
        else result = (Page<OBJ>) process.getObject().get();
    }

    public MasterProcess(@NotNull List<ProcessLine<OBJ>> processes) {
        final List<ProcessLine<OBJ>> success = new ArrayList<>();
        IntStream.range(0, processes.size())
            .forEach(i -> {
                final ProcessLine obj = processes.get(i);
                if(obj.isError()) errors.put(i, obj.getErrorCause());
                else success.add(processes.get(i));
            });
        this.result = (Page<OBJ>) success.stream()
            .map(ProcessLine::getObject)
            .filter(Optional::isPresent)
            .toList();
    }

    public MasterProcess setPage(@NotNull Page<OBJ> page) {
        this.result = page;
        return this;
    }

}
