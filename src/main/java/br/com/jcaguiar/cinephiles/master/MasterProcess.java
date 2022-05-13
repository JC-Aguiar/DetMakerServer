package br.com.jcaguiar.cinephiles.master;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Streamable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MasterProcess<OBJ> extends PageImpl<OBJ> {

    final Map<Integer, String> log = new HashMap<>();
//    final Streamable<OBJ> result

    private MasterProcess(@NotNull List<OBJ> processes) {
        super(processes);
    }

    private MasterProcess(@NotNull List<OBJ> processes, @NotNull Pageable pageable) {
        super(processes, pageable, processes.size());
    }

    public static MasterProcess<?> of(@NotNull List<ProcessLine> processes) {
        final MasterProcess masterProcess = new MasterProcess(getProcessContent(processes));
        processes.forEach(masterProcess::extractLog);
        return masterProcess;
    }

    public static MasterProcess<?> of(@NotNull List<ProcessLine> processes, @NotNull Pageable pageable) {
        final MasterProcess masterProcess = new MasterProcess(
            getProcessContent(processes),
            pageable);
        processes.forEach(masterProcess::extractLog);
        return masterProcess;
    }

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

    private void extractLog(@NotNull ProcessLine<OBJ> process) {
        log.put(log.size(), process.getFullLog());
    }

}
