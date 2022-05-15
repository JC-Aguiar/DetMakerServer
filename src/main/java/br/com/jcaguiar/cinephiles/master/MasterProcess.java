package br.com.jcaguiar.cinephiles.master;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcess<OBJ> {

    final Table<Integer, Boolean, String> log = HashBasedTable.create();
    final String status;
    final PageImpl<OBJ> body;
    enum StatusTypes {
        SUCCESSES,
        PARCIAL,
        FAIL;
    }

    private MasterProcess(@NotNull List<ProcessLine> processes) {
        this.status = validadeProcesses(processes);
        this.body = new PageImpl(processes);
    }

    private MasterProcess(@NotNull List<ProcessLine> processes, @NotNull Pageable pageable) {
        this.status = validadeProcesses(processes);
        this.body = new PageImpl(
            getProcessContent(processes),
            pageable,
            processes.size());
    }

    private String validadeProcesses(@NotNull List<ProcessLine> processes) {
        final int errorCount = populateAndContErrors(processes);
        return checkFinalStatus(processes.size(), errorCount);
    }

    private int populateAndContErrors(@NotNull List<ProcessLine> processes) {
        final AtomicInteger errorCont = new AtomicInteger();
        processes.stream().forEach(p -> {
            log.put(log.size(), p.getError(), p.getLog());
            if(!p.isOk()) errorCont.getAndIncrement();
        });
        return errorCont.get();
    }

    private String checkFinalStatus(int lines, int errorCont) {
        if(errorCont == 0) {
            return StatusTypes.SUCCESSES.toString();}
        else {
            return (errorCont == lines) ?
                StatusTypes.FAIL.toString() :
                StatusTypes.PARCIAL.toString();}
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

    private static List<?> getProcessContent(@NotNull List<ProcessLine> processes) {
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
