package br.com.jcaguiar.cinephiles.master;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MasterProcess<OBJ> {

    final Map<Integer, String> log = new HashMap<>();
    final StatusTypes status;
    enum StatusTypes {
        SUCCESSES,
        PARCIAL,
        FAIL,
        EMPTY
    }

    public MasterProcess(@NotNull List<ProcessLine> processes) {
//        this.log = checkContent(processes) ? HashBasedTable.create() : null;
        this.status = validadeProcesses(processes);
    }

    public MasterProcess(@NotNull List<ProcessLine> processes, @NotNull Pageable pageable) {
//        this.log = checkContent(processes) ? HashBasedTable.create() : null;
        this.status = validadeProcesses(processes);
    }

    private boolean checkContent(@NotNull List<ProcessLine> processes) {
        final int cont = processes.stream()
            .filter(ProcessLine::isOk)
            .filter(ProcessLine::isObjectPresent)
            .toList().size();
        return cont > 0;
    }

    private StatusTypes validadeProcesses(@NotNull List<ProcessLine> processes) {
        if(this.log == null) return StatusTypes.EMPTY;
        final int errorCount = populateAndContErrors(processes);
        return checkFinalStatus(processes.size(), errorCount);
    }

    private int populateAndContErrors(@NotNull List<ProcessLine> processes) {
        final AtomicInteger errorCont = new AtomicInteger();
        processes.stream().forEach(p -> {
            this.log.put(this.log.size(), p.getLog());
            if(!p.isOk()) errorCont.getAndIncrement();
        });
        return errorCont.get();
    }

    private StatusTypes checkFinalStatus(int lines, int errorCont) {
        if(errorCont == 0) {
            return StatusTypes.SUCCESSES;}
        else {
            return (errorCont == lines) ?
                StatusTypes.FAIL :
                StatusTypes.PARCIAL;}
    }

//    public static MasterProcess<?> of(@NotNull List<ProcessLine> processes) {
//        final MasterProcess masterProcess = new MasterProcess(getProcessContent(processes));
//        processes.forEach(masterProcess::extractLog);
//        return masterProcess;
//    }
//
//    public static MasterProcess<?> of(@NotNull List<ProcessLine> processes, @NotNull Pageable pageable) {
//
//        final List<String> logList = processes.stream().map(ProcessLine::getLog).toList();
//        return new MasterProcess(
//            getProcessContent(processes),
//            pageable,
//            logList);
//    }

//    if(process.isOk()) {
//        final List<OBJ> list = List.of(process.getObject());
//        this.result = new PageImpl<OBJ>(list);
//    }

    protected static List<?> getProcessContent(@NotNull List<ProcessLine> processes) {
        return processes.stream()
            .filter(ProcessLine::isOk)
            .map(ProcessLine::getObject)
            .toList();
    }

//    public void setResult(@NotNull List<ProcessLine<OBJ>> processes) {
//        this.result = new PageImpl<>(getProcessContent(processes));
//    }

//    private Map<Boolean, String> extractLog(@NotNull ProcessLine<OBJ> process) {
//        return process.getFullLog();
//    }

}
