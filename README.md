# DET-MAKER SERVER



## Entidades
### Recursos Acionáveis
Categoria das entidades que mapeiam os recursos da empresa.
Por estarem sempre disponíveis para serem atualizados livremente a qualquer momento, são voláteis e mutáveis.
- **Job**:      Representa o cadastro de um artefato ou recurso acionável, do qual será usado para formar Pipelines e gerar Evidências.
- **JobQuery**: Representa o cadastro de uma query a ser executada antes e depois do acionamento de um Job para se coletar Evidências.
- **Pipeline**: Representa o cadastro de um fluxo de acionamento sequencial composto por Jobs.

### Pré-Acionamento
Categoria das entidades que guardam as solicitações de acionamento. Tendo em vista que múltiplos acionamentos consecutivos podem impactar negativamente uns aos outros, gerando um comportamento imprevisível. 
Atemporais e imutáveis.


### Pós-Acionamento
Categoria das entidades que guardam resultados pós-execução.
Estão disponíveis para leitura apenas. Portanto, atemporais e imutáveis.
- **Evidência**: Para cada Job executado, um novo registro é salvo como prova.
- **ExecQuery**: Para cada query executada por um Job, os registro antes e depois do acionamento são salvos como prova.




## Configurações da Aplicação
 - Execução Inicial Pós-Spring: `DetMakerBeans.init`



