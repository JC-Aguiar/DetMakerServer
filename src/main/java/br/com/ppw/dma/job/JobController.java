package br.com.ppw.dma.job;

import br.com.ppw.dma.ambiente.AmbienteAcessoDTO;
import br.com.ppw.dma.ambiente.AmbienteService;
import br.com.ppw.dma.cliente.Cliente;
import br.com.ppw.dma.cliente.ClienteService;
import br.com.ppw.dma.configQuery.ComandoSql;
import br.com.ppw.dma.configQuery.ConfigQueryController;
import br.com.ppw.dma.evidencia.Evidencia;
import br.com.ppw.dma.evidencia.EvidenciaController;
import br.com.ppw.dma.master.MasterController;
import br.com.ppw.dma.net.ConectorSftp;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("job")
@Slf4j
public class JobController extends MasterController<Long, Job, JobController> {

    private final JobService jobService;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private EvidenciaController evidenciaController;

    @Autowired
    private ConfigQueryController queryController;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private AmbienteService ambienteService;


    public JobController(@Autowired JobService jobService) {
        super(jobService);
        this.jobService = jobService;
    }

    @GetMapping("cliente/{clienteId}")
    public ResponseEntity<?> getAll( @PathVariable(name = "clienteId") Long clienteId) {
        final List<JobInfoDTO> dtos = jobService.findAllByCliente(clienteId)
            .stream()
            .map(JobInfoDTO::converterJob)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @Override
    public ResponseEntity<?> parseOne(Job entity) {
        final JobInfoDTO dto = JobInfoDTO.converterJob(entity);
        return ResponseEntity.ok(dto);
    }

    /**
     * Método herdado do {@link MasterController} para conversão de Entidade para DTO
     * @param jobs {@link Page} de {@link Job}s a converter
     * @return {@link ResponseEntity} contendo uma {@link Page} de {@link JobInfoDTO}s
     */
    @Override
    public ResponseEntity<Page<JobInfoDTO>> parseAll(Page<Job> jobs) {
        final Page<JobInfoDTO> responsePage = jobs.map(JobInfoDTO::converterJob);
        return ResponseEntity.ok(responsePage);
    }

    public List<Job> findByClienteAndNome(@NonNull Cliente cliente, @NonNull List<String> nomes) {
        log.info("Procurando Jobs do Cliente '{}' para os seguintes nomes: {}.",
            cliente.getNome(), String.join(", ", nomes));

        val jobs = jobService.findByClienteAndNome(cliente, nomes);
        log.info("Total de Jobs encontrados: {}", jobs.size());
        return jobs;
    }

    //TODO: javadoc
    //TODO: criar novo controlar para ter essa responsabilidade?
    @PostMapping(value = "read/xlsx/planilha/{planilhaNome}/cliente/{clienteId}")
    public ResponseEntity<List<JobInfoDTO>> readXlsx(
        @PathVariable() String planilhaNome,
        @PathVariable() Long clienteId,
        @RequestParam("file") final MultipartFile file)
    throws IOException {
        val cliente = clienteService.findById(clienteId);
        log.info("Lendo arquivo Excel (formato xlsx) para Cliente '{}'.", cliente.getNome());
        val excelX = jobService.lerXlsx(file);
        val jobsDto = jobService.mapExcelToJobInfoDto(excelX, planilhaNome);
        log.info("Total de jobs mapeados da planilha: {}.", jobsDto.size());

        //val configJobDto = jobsDto.stream()
        //    .map(this::criarJobConfigDto)
        //    .toList();
        return ResponseEntity.ok(jobsDto);
    }

    //TODO: javadoc
    @Transactional
    @PostMapping(value = "save/all/cliente/{clienteId}")
    public ResponseEntity<?> saveJobs(
        @PathVariable() Long clienteId,
        @NonNull @RequestBody List<JobInfoDTO> jobsDto) {
        //----------------------------------------------------
        if(jobsDto.isEmpty()) {
            return ResponseEntity.badRequest().body("A lista de Jobs enviada está vazia.");
        }
        val cliente = clienteService.findById(clienteId);
        log.info("Salvando jobs no banco.");
        int jobsSalvos = 0;
        for(val dto: jobsDto) {
            try {
                log.debug(dto.toString());
                log.info("Convertendo DTO em Entidade.");
                val jobEntidade = mapper.map(dto, Job.class).refinarCampos();
                jobService.persist(jobEntidade);
                jobsSalvos++;
            }
            catch(Exception e) {
                log.warn("Erro ao tentar salvar Job ID {}: {}.", dto.getNome(), e.getMessage());
            }
        }
        val mensagem = "Total de jobs salvos: " +jobsSalvos+ ".";
        log.info(mensagem);

        if(jobsSalvos == 0)
            return ResponseEntity
                .internalServerError()
                .body("Não foi possível salvar nenhum dos jobs enviados.");
        return ResponseEntity.ok(mensagem);
    }

    //TODO: criar exception própria
    //TODO: javadoc
    public List<Evidencia> executeJobsAndGetEvidencias(
        @NonNull AmbienteAcessoDTO banco,
        @NonNull AmbienteAcessoDTO ftp,
        @NonNull List<JobExecuteDTO> jobsExecute) {
        //----------------------------------------------
        val connFtp = ConectorSftp.conectar(ftp.getConexao(), ftp.getUsuario(), ftp.getSenha());
        val jobsPojo = jobService.executarJobs(connFtp, banco, jobsExecute);
        return evidenciaController.gerarEvidencias(jobsPojo);
    }

    //TODO: javadoc
    private JobConfigDTO criarJobConfigDto(@NonNull JobInfoDTO infoDto) {
        final List<ComandoSql> queries = new ArrayList<>();
        try {
            queries.addAll(
                queryController.getAllByJob(infoDto.getId()).getBody()
            );
        }
        catch(Exception e) {
            log.warn(e.getMessage());
        }
        log.info("Verificando se alguma ConfigQuery está pendente.");
        log.info("ConfigQuery tabelas: {}.", queries.size());
        log.info("Job tabelas: {}.", infoDto.getTabelas().size());
        infoDto.getTabelas()
            .stream()
            .filter(tabela -> queries.stream().noneMatch(query -> query.getTabela().trim().equals(tabela)))
            .peek(tabela -> log.info("Tabela '{}' pendente de ConfigQuery.", tabela))
            .map(ComandoSql::new)
            .forEach(queries::add);

        val configDto = new JobConfigDTO();
        configDto.setJob(infoDto);
        configDto.addQuery(queries);

        log.info("JobConfigDto montado com sucesso.");
        log.info(configDto.toString());
        return configDto;
    }
}
