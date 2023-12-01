//======================================= Inserindo informações fixas =======================================
window.onload = function () {

    //Definindo título da página html
    document.title = 'DET ' + atividadeNome;

    //Ação ao clicar na logo Peopleware
    document.getElementById("ppw").addEventListener("click", (event) => {
        event.preventDefault();
        window.scrollTo(0, 0);
    });
    //Ação ao clicar no menu Identificação
    document.getElementById("menu-identificacao").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("identificacao-titulo").scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
    });
    //Ação ao clicar no menu Detalhamento dos Testes
    document.getElementById("menu-detalhamento").addEventListener("click", (event) => {
        event.preventDefault();
        document.getElementById("detalhamento-titulo").scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
    });
    //Texto dos detalhes dos testes
    document.getElementById("assinatura").innerHTML = assinatura;

    //Texto referente o nome da atividade
    document.getElementById("atividade-nome").innerHTML = atividadeNome;

    //Texto referente o nome do projeto
    document.getElementById("projeto-id").innerHTML = projetoId;
    document.getElementById("projeto-nome").innerHTML = projetoNome;

    //Texto referente aos detalhes do teste
    document.getElementById("teste-tipo").innerHTML = testeTipo;
    document.getElementById("teste-sistema").innerHTML = testeSistema;

    //Texto dos envolvidos no teste
    document.getElementById("user-nome-1").innerHTML = userNome;
    document.getElementById("user-cargo-1").innerHTML = userCargo;
    document.getElementById("user-empresa-1").innerHTML = userEmpresa;
    document.getElementById("user-email-1").innerHTML = userEmail;
    document.getElementById("user-phone-1").innerHTML = userPhone;

    //Texto dos detalhes dos testes
    document.getElementById("modulos").innerHTML = detalhesModulos.replaceAll("\n" , "<br/>");
    document.getElementById("parametros").innerHTML = detalhesParametros.replaceAll("\n" , "<br/>");
    document.getElementById("dados").innerHTML = detalhesDados.replaceAll("\n" , "<br/>");
    document.getElementById("configuracao").innerHTML = detalhesConfig.replaceAll("\n" , "<br/>");
    document.getElementById("ambiente").innerHTML = detalhesAmbiente.replaceAll("\n" , "<br/>");
}
//======================================= Controle de estatística =======================================
const resultadosTotais = [];
const gerarEstatisticas = () => {
    let aprovados = 0;
    let reprovados = 0;
    let parciais = 0;
    resultadosTotais.forEach((resultado, index) => {
        if (resultado === "Reprovado") reprovados++;
        else if (resultado === "Parcial") parciais++;
        else if (resultado === "Aprovado") aprovados++;
    });
    let aprovadosPorcent = Math.round((aprovados * 100) / resultadosTotais.length);
    let reprovadosPorcent = Math.round((reprovados * 100) / resultadosTotais.length);
    let parciaisPorcent = Math.round((parciais * 100) / resultadosTotais.length);

    const elementos = [];
    if (aprovados > 0)
        elementos.push({ valor: `Jobs Aprovados: ${aprovados} (${aprovadosPorcent}%)`, cor: 'bg-primary' });
    if (parciais > 0)
        elementos.push({ valor: `Jobs Parciais: ${parciais} (${parciaisPorcent}%)`, cor: 'bg-secondary' });
    if (reprovados > 0)
        elementos.push({ valor: `Jobs Reprovados: ${reprovados} (${reprovadosPorcent}%)`, cor: 'bg-warning' });

    const itensStatus = elementos.map((item, index) =>
        React.createElement('span', { key: index, className: `me-2 badge bg-opacity-75 ${item.cor}` }, item.valor)
    );

    return React.createElement('h5', { className: 'py-0 my-0' }, itensStatus);
}
//===========================================================================================================
//============================ Criando modelo de tabela dos test-cases com React ============================
function TabelaTestcase(
    evidenciaId, nome, descricao, requisitos, comentario, resultado, parametros,
    revisor, data, queries, tabelasNome, tabelasPreJob, tabelasPosJob,
    logsConteudo, logsNome, cargasConteudo, cargasNome, saidasConteudo, saidasNome) {
    //-------------------------------------------------------------------
    //Lista dos anexos
    const anexosTotais = [];
    resultadosTotais.push(resultado);
    //Preenchendo anexos de tabelas por query
    queries.forEach((query, index) => {
        const nomePreJob = `Tabela ${tabelasNome[index]} antes` + '\n';
        const nomePosJob = `Tabela ${tabelasNome[index]} depois` + '\n';
        const propsPreJob = {
            id: 'list-group-item',
            className: 'btn btn-outline-primary py-0 px-1 me-2 ',
            onClick: () => abrirAnexo(nomePreJob, tabelasPreJob[index])
        };
        const propsPosJob = {
            id: 'list-group-item',
            className: 'btn btn-outline-primary py-0 px-1 me-2 ',
            onClick: () => abrirAnexo(nomePosJob, tabelasPosJob[index])
        };
        const bancoInfo = React.createElement( 'div', {},
            React.createElement( 'h5', { className: 'fw-bold mt-3' }, tabelasNome[index]),
            React.createElement( 'p', { className: 'my-0 px-2 py-0 small font-monospace bg-light' }, query + '\n'),
            React.createElement( 'div', propsPreJob, nomePreJob),
            React.createElement( 'div', propsPosJob, nomePosJob)
        );
        anexosTotais.push(bancoInfo);
    })
    //Preenchendo anexos de logs
    logsNome.forEach((nome, index) => {
        const sessao = index === 0 ? React.createElement( 'h5', {className: 'fw-bold mt-3'}, "LOGS") : null;
        const props = {
            id: 'list-group-item',
            className: 'btn btn-outline-primary py-0 px-1 me-2 ',
            onClick: () => abrirAnexo(nome, logsConteudo[index])
        };
        const logInfo = React.createElement( 'div', {},
            sessao,
            React.createElement( 'div', props, nome+"\n")
        );
        anexosTotais.push(logInfo);
    })
    //Preenchendo anexos de cargas
    cargasNome.forEach((nome, index) => {
        const sessao = index === 0 ? React.createElement( 'h5', {className: 'fw-bold mt-3'}, "CARGAS") : null;
        const props = {
            id: 'list-group-item',
            className: 'btn btn-outline-primary py-0 px-1 me-2 ',
            onClick: () => abrirAnexo(nome, cargasConteudo[index])
        };
        const cargaInfo = React.createElement( 'div', {},
            sessao,
            React.createElement( 'div', props, nome+"\n")
        );
        anexosTotais.push(cargaInfo);
    })
    //Preenchendo anexos de saídas
    saidasNome.forEach((nome, index) => {
        const sessao = index === 0 ? React.createElement( 'h5', {className: 'fw-bold mt-3'}, "SAÍDAS") : null;
        const props = {
            id: 'list-group-item',
            className: 'btn btn-outline-primary py-0 px-1 me-2 ',
            onClick: () => abrirAnexo(nome, saidasConteudo[index])
        };
        const saidaInfo = React.createElement( 'div', {},
            sessao,
            React.createElement( 'div', props, nome+"\n")
        );
        anexosTotais.push(saidaInfo);
    })
    //Gerando tabela Testcase
    return React.createElement('div', { className: 'table-responsive' },
        React.createElement('h3', { id: evidenciaId, className: 'pt-3' }, nome),
        React.createElement('table', { className: 'table table-light table-striped m-0 mb-5 text-start' },
            React.createElement('tbody', null,
                React.createElement('tr', null,
                    React.createElement('th', null, 'Evidência ID'),
                    React.createElement('td', null, evidenciaId)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Descrição do Job'),
                    React.createElement('td', null, descricao)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Parâmetros do Job'),
                    React.createElement('td', null, parametros)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Requisitos'),
                    React.createElement('td', null, requisitos)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Data do Teste'),
                    React.createElement('td', null, data)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Resultado Obtido'),
                    React.createElement('td', null, resultado)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Revisor'),
                    React.createElement('td', null, revisor)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Notas do Revisor'),
                    React.createElement('td', null, comentario)
                ),
                React.createElement('tr', null,
                    React.createElement('th', null, 'Anexos de Evidência'),
                    React.createElement('td', null, anexosTotais)
                )
            )
        )
    );
}
//==========================================================================================================
//=========================== Renderizando tabelas e links nos menus com React =============================
document.addEventListener('DOMContentLoaded', function () {
    //Renderizando Tabelas
    ReactDOM.render(
        React.createElement('div', null, AllTabelasTestecase),
        document.getElementById('tabela-testes')
    );
    //Renderizando Estatística
    ReactDOM.render(
        React.createElement('div', null, gerarEstatisticas()),
        document.getElementById('estatistica')
    );
    //Criando Menu-Links
    let testesLinks = [];
    AllTabelasTestecase.forEach(function (element, index) {
        let jobNome = element.props.children[0].props.children;
        let linkNome = (index+1) + "." + jobNome;
        testesLinks.push(React.createElement('a', { id: linkNome, key: index, className: 'dropdown-item' }, linkNome));
    });
    //Renderizando Menu-Links
    ReactDOM.render(
        React.createElement('li', null, testesLinks),
        document.getElementById('dropdown-testes-itens')
    );
    //Vinculando Tabelas x Menu-Links
    testesLinks.forEach(function (element, index) {
        let testeId = AllTabelasTestecase[index].props.children[0].props.id;
        let testeNome = testesLinks[index].props.id;
        const tituloTeste = document.getElementById(testeId);
        const menuTeste = document.getElementById(testeNome);
        menuTeste.addEventListener("click", (event) => {
            event.preventDefault();
            tituloTeste.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'start' });
        });
    });
});
//==========================================================================================================
//=========================== Lógica para anexar e abrir evidências de texto ===============================
function abrirAnexo(nome, conteudo) {
    const bytes = Uint8Array.from(atob(conteudo), c => c.charCodeAt(0));
    const decodificador = new TextDecoder('utf-8');
    conteudo = decodificador.decode(bytes);

    const newWindow = window.open('', '_blank');
    newWindow?.document.write(`<head><title>${nome}</title></head><pre>${conteudo}</pre>`);
    newWindow?.document.close();
}
function abrirNovaAba(conteudo) {
    const newWindow = window.open('', '_blank');
    newWindow?.document.write(`<img src=${conteudo} class="card-img" alt="Imagem" />`);
    newWindow?.document.close();
}
//==========================================================================================================
//=========================== Textos a serem usados para preenchimento dinâmico ============================
