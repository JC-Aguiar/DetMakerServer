package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcess<OBJ> {

    Page<OBJ> result = new PageImpl<>(new ArrayList<>());
    final Map<Integer, String> log = new HashMap<>();

//    public MasterProcess(@NotNull List<ProcessLine<OBJ>> processes, @NotNull Page<?> page) {
//        this.result = page;
//        IntStream.range(0, processes.size())
//            .forEach(i -> {
//                final ProcessLine obj = processes.get(i);
//                if(obj.isError()) errors.put(i, obj.getErrorCause());
//            });
//    }

    public MasterProcess(@NotNull ProcessLine<OBJ> process) {
        if(process.isOk()) {
            final List<OBJ> list = List.of(process.getObject());
            this.result = new PageImpl<OBJ>(list);
        }
        extractLog(process);
    }

    public MasterProcess(@NotNull ProcessLine<OBJ> process, @NotNull Page<OBJ> result) {
        this.result = result;
        extractLog(process);
    }

    public MasterProcess(@NotNull List<ProcessLine<OBJ>> processes) {
        setResult(processes);
        processes.forEach(this::extractLog);
//        final List<ProcessLine<OBJ>> success = new ArrayList<>();
//        IntStream.range(0, processes.size())
//            .forEach(i -> {
//                final ProcessLine obj = processes.get(i);
//                if(obj.isError()) errors.put(i, obj.getLog());
//                else success.add(processes.get(i));
//            });
//        this.result = (Page<OBJ>) success.stream()
//            .map(ProcessLine::getObject)
//            .filter(Optional::isPresent)
//            .toList();
    }

    public MasterProcess(@NotNull List<ProcessLine<OBJ>> processes, @NotNull Page<OBJ> result) {
        this.result = result;
        processes.forEach(this::extractLog);
    }

    public void setResult(@NotNull List<ProcessLine<OBJ>> processes) {
        this.result = new PageImpl<>(
                processes.stream()
                        .filter(ProcessLine::isOk)
                        .map(ProcessLine::getObject)
                        .toList());
    }

    private void extractLog(@NotNull ProcessLine<OBJ> process) {
        log.put(log.size(), process.getFullLog());
    }

}