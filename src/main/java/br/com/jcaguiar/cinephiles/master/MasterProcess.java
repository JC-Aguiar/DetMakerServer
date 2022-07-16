package br.com.jcaguiar.cinephiles.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.google.common.collect.TreeBasedTable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MasterProcess<OBJ> {

    final Map<Integer, String> log = new HashMap<>();
    StatusTypes status = StatusTypes.EMPTY;
    enum StatusTypes {
        SUCCESSES,
        PARCIAL,
        FAIL,
        EMPTY
    }
    @JsonIgnore
    int errorCount = 0;

    public MasterProcess(@NotNull ProcessLine process) {
        addProcess(process);
        checkFinalStatus();
    }

    public MasterProcess(@NotNull List<ProcessLine> processes) {
        addProcess(processes);
        checkFinalStatus();
    }

    public MasterProcess<?> addProcess(@NotNull ProcessLine<?> process) {
        this.log.put(this.log.size(), process.getLog());
        if(!process.isOk()) errorCount++;
        return this;
    }

    public MasterProcess<?> addProcess(@NotNull List<ProcessLine> process) {
        process.forEach(this::addProcess);
        return this;
    }

    private void checkFinalStatus() {
        if(log.isEmpty()) {
            status = StatusTypes.EMPTY;
            return;
        };
        if(errorCount == 0) {
            status = StatusTypes.SUCCESSES;
        }
        else {
            status = (errorCount >= log.size()) ?
                StatusTypes.FAIL :
                StatusTypes.PARCIAL;
        }
    }

//    public static MasterProcess<?> of(@NotNull List<ServiceProcess> processes) {
//        final MasterProcess masterProcess = new MasterProcess(getProcessContent(processes));
//        processes.forEach(masterProcess::extractLog);
//        return masterProcess;
//    }
//
//    public static MasterProcess<?> of(@NotNull List<ServiceProcess> processes, @NotNull Pageable pageable) {
//
//        final List<String> logList = processes.stream().map(ServiceProcess::getLog).toList();
//        return new MasterProcess(
//            getProcessContent(processes),
//            pageable,
//            logList);
//    }

//    if(process.isOk()) {
//        final List<OBJ> list = List.of(process.getObject());
//        this.result = new PageImpl<OBJ>(list);
//    }

    protected static List<Object> getProcessContent(@NotNull List<ProcessLine> processes) {
        return processes.stream()
            .filter(ProcessLine::isOk) //TODO: revisar lógica!
            .map(ProcessLine::getObject)
            .toList();
    }

//    public void setResult(@NotNull List<ServiceProcess<OBJ>> processes) {
//        this.result = new PageImpl<>(getProcessContent(processes));
//    }

//    private Map<Boolean, String> extractLog(@NotNull ServiceProcess<OBJ> process) {
//        return process.getFullLog();
//    }

}
