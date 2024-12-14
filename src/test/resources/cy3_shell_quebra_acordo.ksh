#!/bin/bash
#####################################################################################################################
# Autor:     Victor Hugo Couto Cunha
# Data:      27/03/2023
# Descricao: Processo de Gestao de Acordos, seguindo as modalidades de Quebra de Acordos.
#####################################################################################################################
#* PARAMETROS:
#  $1 - Data de processamento do ciclo de cobranca (formato AAAAMMDD)
#  $2 - N�mero do Subset a processar (0-Nao utiliza recurso do Subset) 
#  $3 - Fase de processamento do fluxo de quebra de acordo (0-Create TempTable / 1-Processamento por Subset)
#
#  set -x # Uncomment to debug this shell script
#  set -n # Uncomment to check command syntax without any execution
#####################################################################################################################
# Historico de Alteracoes
#
# Data          Autor                      Descricao
# -----------   ------------------------   -----------------------
#####################################################################################################################

#* ----------------------------------------------------------------------------------------------------------------*#
#* Configura as variaveis de ambiente necessarias para execucao.                                                   *#
#* ----------------------------------------------------------------------------------------------------------------*#

LIM_PARAMS=3     #* Limite de parametros para o processo vigente *#
TOT_PARAMS=$#    #* Total de parametros informados *#

if [ $TOT_PARAMS -eq 0 -o $TOT_PARAMS -lt $LIM_PARAMS ]; then
    echo "Quantidade ($TOT_PARAMS) invalida de parametros definidos para o processo cy3_shell_quebra_acordo.ksh !"
    echo "Uso cy3_shell_quebra_acordo.ksh PARAM1 PARAM2 PARAM3"
    echo "PARAM1 => Data de referencia para processamento do ciclo de cobranca (formato AAAAMMDD)"
    echo "PARAM2 => N�mero do Subset a processar (0-Nao utiliza recurso do Subset)"
    echo "PARAM3 => Fase de processamento do fluxo de quebra de acordo (0-Create TempTable / 1-Processamento por Subset"
    exit -1
fi


DT_PROC_AA=`expr substr $1 1 4 `
DT_PROC_MM=`expr substr $1 5 2 `
DT_PROC_DD=`expr substr $1 7 2 `
export DATA=${DT_PROC_AA}${DT_PROC_MM}${DT_PROC_DD}

HOR=`date +%H`
MIN=`date +%M`
SEG=`date +%S`
export HORA=$HOR$MIN$SEG

export NU_SUBSET=$2

export NU_FASE=$3

#---- VARIAVEIS DE LOG

export DIR_LOG=$USR_HOME/cy3/log
if test ! -d $DIR_LOG
   then 
        WriteMessage "Diretorio de log \"${DIR_LOG}\", nao existente!"
        WriteMessage "Criando diretorio $DIR_LOG"
        mkdir $DIR_LOG
        chmod 777 $DIR_LOG
fi 
if test ! -w $DIR_LOG || test ! -x $DIR_LOG
   then
	echo "Sem permissao de escrita no Diretorio \"${DIR_LOG}\""
	echo "Fim do processamento com erro"
	exit -2  
fi

#---- VARIAVEIS DE AMBIENTE GLOBAL

. /app/rcvry/shells/CY3_VARIAVEIS.ksh ${DT_PROC_DD} ${DT_PROC_MM} ${DT_PROC_AA}

#---- SET IFXDATE

#. $USR_HOME/shells/cy3_shell_ifxdate_inicia.ksh $DATA

#---- VARIAVEIS DE AMBIENTE LOCAL

export SCRNAME=`basename $0|cut -f1 -d'.'`
#export NLS_LANG=SPANISH_MEXICO.WE8ISO8859P1
export NLS_DATE_FORMAT="dd-mon-yyyy hh24:mi:ss"
export SECONDS_SLEEP=10
export INTERVAL_COMMIT=1000

export ARQLOG=${DIR_LOG}/proc_fluxo_quebra_acordo_${DATA}_F${NU_FASE}_S${NU_SUBSET}.log

if [ -f $ARQLOG ]; then
    mv $ARQLOG ${ARQLOG}.err
else
    >> $ARQLOG
fi

#####################################################################################################################
# Procedimentos e funcoes globais.
#####################################################################################################################

#*---- Uso geral *#

WriteMessage ()
{
    echo "`date '+%d/%m/%Y %H:%M:%S'` $1" >> $ARQLOG
}

error ()
{
    echo "`date '+%d/%m/%Y %H:%M:%S'` $1 - PROCESSAMENTO CANCELADO." >> $ARQLOG
}

abort ()
{
    error "$1"
    exit -3
}

execCreateTempTable ()
{
WriteMessage "----------------------------------------------------------------------------"
WriteMessage " "
WriteMessage "INICIO CRIACAO DA TABELA TEMPORARIA DE APOIO - TB_TEMP_QUEBRA_ACORDO."
WriteMessage " "

export ARQSQL=${DIR_LOG}/procsql_create_temptable_quebra_acordo_${DATA}_F${NU_FASE}.log

rm -f ${ARQSQL} 2>/dev/null

set +vx

sqlplus -s  << END >> ${ARQSQL}
$CYBERCONNECT

set linesize 1000
set echo on
set feedback on
set serveroutput on

drop table RCVRY.TB_TEMP_QUEBRA_ACORDO;

create table RCVRY.TB_TEMP_QUEBRA_ACORDO as
SELECT
   a.AHACCT      ,    -- Documento do Cliente (CPF/CNPJ)
   a.AHDT        ,    -- Data de cria��o do acordo
   a.AHACCTGCOB  ,    -- Grupo do produto de faturas
   a.AHACCTCOB   ,    -- Conta de Cobranca de Correspondencia
   a.AHID,            -- Numero do Acordo
   a.AHSTATUS,        -- Status do Acordo
   a.AHREGDT,         -- Data de registro
   (SELECT COUNT(1) FROM AGPMTDET WHERE APAHID = a.AHID) QT_PARC_ACORDO,  -- Quantidade de parcela
   DECODE(PBFLINDQA, 1, 'DIFERENCIADO', 2, 'DUPLICADO', NULL) QUEBRA,     -- Flag que indica quebra de acordo por valor duplicado
   PBNUMPARC PARCELA_QUEBRADA,
   DECODE((SELECT 'S' FROM TB_ESTACORDO_PARCELAS_CONTAS WHERE ID_ACORDO = a.AHID AND ROWNUM = 1), NULL,'S', 'N') SEM_ESTRUTURA,
   0 AS SUBSET,
   a.AHTPENVIO
FROM
   RCVRY.AGRHDR a LEFT OUTER JOIN (SELECT
                                       MIN(PBFLINDQA) PBFLINDQA,
                                       PBNUMACORDO,
                                       MIN(PBNUMPARC) PBNUMPARC
                                   FROM 
                                       RCVRY.TB_PAGTO_BOLETO b 
                                   WHERE
                                       b.PBDTCICLO  = TO_DATE('${DATA}','YYYYMMDD')
                                   AND (b.PBFLINDQA = '1' OR b.PBFLINDQA = '2')
                                   GROUP BY PBNUMACORDO
                                  ) b
   ON b.PBNUMACORDO = a.AHID
WHERE
   a.AHSTATUS = 'A';

update RCVRY.TB_TEMP_QUEBRA_ACORDO set SUBSET = MOD(ROWNUM,${MAX_SUBSET})+1;
commit;

select SUBSET, COUNT(*) from RCVRY.TB_TEMP_QUEBRA_ACORDO group by SUBSET order by 1;

create index RCVRY.IX_TEMP_QUEBRA_ACORDO_01 ON RCVRY.TB_TEMP_QUEBRA_ACORDO (SUBSET);

create index RCVRY.IX_TEMP_QUEBRA_ACORDO_02 ON RCVRY.TB_TEMP_QUEBRA_ACORDO (AHACCT,AHACCTCOB);

select count(1) from (
select
    t.*,
    decode(substr(a.AHACCT,-1), '0', 10, to_number(substr(a.AHACCT,-1))) as SUBSET_NEW
from
   RCVRY.TB_TEMP_QUEBRA_ACORDO t,
   (select
		AHACCT, ADACCT, count(*)
	from 
		RCVRY.AGRDM, RCVRY.AGRHDR
	where
		AHID = ADAHID
	and AHSTATUS = 'A'
	group by 
		AHACCT, ADACCT
	having count(*) > 1
	order by 1
	) a
where
	t.AHACCT    = a.AHACCT
and	t.AHACCTCOB = a.ADACCT
)
where SUBSET <> SUBSET_NEW;

declare
    n_contador Number := 0;
begin    
    for c1 in ( select
                   t.rowid rowid_idx,
                   decode(substr(a.AHACCT,-1), '0', 10, to_number(substr(a.AHACCT,-1))) as SUBSET_NEW
                from
                   RCVRY.TB_TEMP_QUEBRA_ACORDO t,
                   (select
                        AHACCT, ADACCT, count(*)
                    from 
                        RCVRY.AGRDM, RCVRY.AGRHDR
                    where
                        AHID = ADAHID
                    and AHSTATUS in ('A', 'P')
                    group by 
                        AHACCT, ADACCT
                    having count(*) > 1
                    order by 1
                   ) a
                where
                    t.AHACCT    = a.AHACCT
                and	t.AHACCTCOB = a.ADACCT
              ) loop
        n_contador := n_contador + 1;
        update RCVRY.TB_TEMP_QUEBRA_ACORDO set SUBSET = c1.SUBSET_NEW where rowid = c1.rowid_idx;
        if (mod(n_contador, 2000) = 0) then
            commit;
        end if;
    end loop;
    commit;
    dbms_output.put_line('TOTAL de Registros Atualizados com Remarca��o de Subset em TB_TEMP_QUEBRA_ACORDO: [' || to_char(n_contador)  || ']');
end;
/

analyze table RCVRY.TB_TEMP_QUEBRA_ACORDO estimate statistics;	

-- Esta consulta tem de retornar 0 como count().
select count(1) from (
select
    t.*,
    decode(substr(a.AHACCT,-1), '0', 10, to_number(substr(a.AHACCT,-1))) as SUBSET_NEW
from
   RCVRY.TB_TEMP_QUEBRA_ACORDO t,
   (select
		AHACCT, ADACCT, count(*)
	from 
		RCVRY.AGRDM, RCVRY.AGRHDR
	where
		AHID = ADAHID
	and AHSTATUS = 'A'
	group by 
		AHACCT, ADACCT
	having count(*) > 1
	order by 1
	) a
where
	t.AHACCT    = a.AHACCT
and	t.AHACCTCOB = a.ADACCT
)
where SUBSET <> SUBSET_NEW;

select SUBSET, COUNT(*) from RCVRY.TB_TEMP_QUEBRA_ACORDO group by SUBSET order by 1;

quit
END

set -vx

if grep "ORA-" $ARQSQL >/dev/null 2>&1
then
    if ! grep "ORA-00942" $ARQSQL   ### Tabela ou view nao existe
	then
	    WriteMessage "ERRO SQL NA ATUALIZACAO DA TABELA DE APOIO DE QUEBRA DE ACORDO!"
		abort "SQL - TB_TEMP_QUEBRA_ACORDO"
	fi
fi    

WriteMessage "FIM CRIACAO DA TABELA TEMPORARIA DE APOIO - TB_TEMP_QUEBRA_ACORDO."
WriteMessage " "
WriteMessage "---------------------------------------------------------------------------"
}
 
execFluxoQuebraAcordoPorSubset ()
{
WriteMessage "----------------------------------------------------------------------------"
WriteMessage " "
WriteMessage "INICIO FLUXO DA GESTAO DE QUEBRA DE ACORDO - SUBSET ${NU_SUBSET}."
WriteMessage " "

set +vx

sqlplus -s  << END >> ${ARQLOG}
$CYBERCONNECT

set serveroutput on;
	
EXEC RCVRY.PKG_FLUXO_QUEBRA_ACORDO.Fluxo_quebra_acordo('${DATA}', '${NU_SUBSET}', ${INTERVAL_COMMIT});

set heading on;

quit
END

set -vx

diro=`pwd`
cd $USR_HOME/utl_file/log
DTEXEC=` date +%Y%m%d`
export LOG_SERVICO=`ls -1tr pr_fluxoQuebraDeAcordo_${DTEXEC}-??????_S${NU_SUBSET}.log | tail -1`
cd $diro

CHECK_ERR_ORA=`grep "ORA-" $USR_HOME/utl_file/log/$LOG_SERVICO | wc -l `

if [ ${CHECK_ERR_ORA} -ne 0 ]; then
    abort "Erro ORACLE na execucao do procedimento PL/SQL PKG_FLUXO_QUEBRA_ACORDO.Fluxo_quebra_acordo! Favor verificar arquivo \"${LOG_SERVICO}\"."
fi

WriteMessage "FIM FLUXO DA GESTAO DE QUEBRA DE ACORDO - SUBSET ${NU_SUBSET}."
WriteMessage " "
WriteMessage "---------------------------------------------------------------------------"
}

#####################################################################################################################
# Submete processo.
#####################################################################################################################

WriteMessage "=================================================================================================="
WriteMessage "Configuracao das variaveis de ambiente usadas pelos sistemas CyberFinancial concluida."
WriteMessage "Referencia (DIA) : ${DT_PROC_DD}"
WriteMessage "Referencia (MES) : ${DT_PROC_MM}" 
WriteMessage "Referencia (ANO) : ${DT_PROC_AA}"
WriteMessage "Referencia (DATA): ${DT_PROC_AA}${DT_PROC_MM}${DT_PROC_DD}"
WriteMessage "USR_HOME         : $USR_HOME"
WriteMessage "EXEC             : $EXEC"
WriteMessage "ORACLE_HOME      : $ORACLE_HOME"
WriteMessage "ORACLE_SID       : $ORACLE_SID"
WriteMessage "NLS_LANG         : $NLS_LANG"
WriteMessage "NLS_DATE_FORMAT  : $NLS_DATE_FORMAT"
WriteMessage "NU_SUBSET        : $NU_SUBSET (0-Nao utiliza recurso do Subset)"
WriteMessage "NU_FASE          : $NU_FASE (0-Create TempTable / 1-Processamento por Subset)"
WriteMessage "=================================================================================================="

#*---- Validar par�metros de entrada.

if [ $NU_FASE -ne 0 -a $NU_FASE -ne 1 ]; then
    WriteMessage "Par�metro do N�mero da Fase de Processamento da Gestao de Quebra de Acordo, INV�LIDO (${NU_FASE})!"
	abort "PAR�METRO DE ENTRADA INV�LIDO - ${NU_FASE}"
fi

WriteMessage "EXEC RCVRY.PKG_NAO_EXISTENTE.mock()"
VARIAVEL_INUTIL="SELECT * FROM RCVRY.TB_VAZIA WHRE 1=1"
VARIAVEL_RESULTADO=$sql({$VARIAVEL_INUTIL})

case "$NU_FASE" in
    "0")
	   execCreateTempTable 
	   ;;
    "1")
	   execFluxoQuebraAcordoPorSubset 
	   ;;
esac

#####################################################################################################################
# Retorna informando sucesso.
#####################################################################################################################

WriteMessage " "
WriteMessage "Fim normal de processamento."
WriteMessage " "
WriteMessage "----------------------------"
