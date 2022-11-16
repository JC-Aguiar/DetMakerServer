package br.com.jcaguiar.cinephiles.master;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//TODO: DELETAR!
@Setter
@Getter
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class MasterProcessManager<OBJ> {

//    String name = "";
//    final Map<Integer, String> log = new HashMap<>();
//    StatusTypes status;
//    enum StatusTypes {
//        SUCCESSES,
//        PARCIAL,
//        FAIL,
//        EMPTY
//    }
//    @JsonIgnore
//    int errorCount = 0;
//    @JsonIgnore
//    final Instant startTime = Instant.now();
//
////    protected MasterProcessManager(@NotNull MasterServiceLog process) {
////        addProcess(process);
////    }
////
////    protected MasterProcessManager(@NotNull List<MasterServiceLog> processes) {
////        addProcess(processes);
////    }
//
//    public MasterProcessManager<?> addProcess(@NotNull MasterServiceResult<?> process) {
//        this.log.put(this.log.size(), process.getLog());
//        if(process.isError()) { this.errorCount++; }
//        checkFinalStatus();
//        return this;
//    }
//
//    public MasterProcessManager<?> addProcess(@NotNull List<MasterServiceResult> process) {
//        process.forEach(this::addSneakyProcess);
//        checkFinalStatus();
//        return this;
//    }
//
//    private void addSneakyProcess(@NotNull MasterServiceResult<?> process) {
//        this.log.put(this.log.size(), process.getLog());
//        if(process.isError()) { this.errorCount++; }
//    }
//
//    private void checkFinalStatus() {
//        if(log.isEmpty()) {
//            this.status = StatusTypes.EMPTY;
//            return;
//        };
//        if(errorCount == 0) {
//            this.status = StatusTypes.SUCCESSES;
//        }
//        else {
//            this.status = (errorCount >= log.size()) ?
//                StatusTypes.FAIL :
//                StatusTypes.PARCIAL;
//        }
//    }

//    public static MasterProcessManager<?> of(@NotNull List<ServiceProcess> processes) {
//        final MasterProcessManager masterProcess = new MasterProcessManager(getProcessContent(processes));
//        processes.forEach(masterProcess::extractLog);
//        return masterProcess;
//    }
//
//    public static MasterProcessManager<?> of(@NotNull List<ServiceProcess> processes, @NotNull Pageable pageable) {
//
//        final List<String> logList = processes.stream().map(ServiceProcess::getLog).toList();
//        return new MasterProcessManager(
//            getProcessContent(processes),
//            pageable,
//            logList);
//    }

//    if(process.isOk()) {
//        final List<OBJ> list = List.of(process.getObject());
//        this.result = new PageImpl<OBJ>(list);
//    }

//    protected static List<?> getProcessContent(@NotNull List<?> processes) {
//        return processes.stream()
//                        .filter(MasterServiceLog::isOk)
//                        .map(MasterServiceLog::getObject)
//                        .toList();
//    }

//    public void setResult(@NotNull List<ServiceProcess<OBJ>> processes) {
//        this.result = new PageImpl<>(getProcessContent(processes));
//    }

//    private Map<Boolean, String> extractLog(@NotNull ServiceProcess<OBJ> process) {
//        return process.getFullLog();
//    }z

}
