(() => {
    const { el } = Ui;

    const telaLogin = document.getElementById('tela-login');
    const formLogin = document.getElementById('form-login');
    const erroLogin = document.getElementById('erro-login');
    const aplicacao = document.getElementById('aplicacao');
    const conteudo = document.getElementById('conteudo');
    const menu = document.getElementById('menu');
    const botaoMenu = document.getElementById('menu-alternar');

    let usuario = null;
    let versaoRota = 0;

    const opcoesDe = (recurso, texto) => async () => (await Api.listar(recurso)).map(item => ({ valor: item.id, texto: texto(item) }));

    const ENTIDADES = {
        professores: {
            recurso: '/api/professores',
            titulo: 'Professores',
            singular: 'Professor',
            novo: 'Novo professor',
            descricao: 'Cadastre, edite e exclua os professores da instituição.',
            descrever: p => `O professor "${p.nome}"`,
            campos: [
                { nome: 'nome', rotulo: 'Nome completo', obrigatorio: true, maxLength: 150, placeholder: 'Ex.: Ana Paula Ribeiro' },
                { nome: 'email', rotulo: 'E-mail', tipo: 'email', obrigatorio: true, maxLength: 150, placeholder: 'nome@unisales.br', transformar: v => v.toLowerCase() },
                { nome: 'titulacao', rotulo: 'Titulação', maxLength: 100, placeholder: 'Ex.: Mestre', sugestoes: ['Graduado', 'Especialista', 'Mestre', 'Doutor'] }
            ],
            colunas: [
                { titulo: 'Nome', texto: p => p.nome },
                { titulo: 'E-mail', texto: p => p.email },
                { titulo: 'Titulação', texto: p => p.titulacao }
            ]
        },
        disciplinas: {
            recurso: '/api/disciplinas',
            titulo: 'Disciplinas',
            singular: 'Disciplina',
            feminino: true,
            novo: 'Nova disciplina',
            descricao: 'Cadastre, edite e exclua as disciplinas oferecidas.',
            descrever: d => `A disciplina "${d.codigo} - ${d.nome}"`,
            linhas: [['codigo', 'cargaHoraria'], ['nome']],
            campos: [
                { nome: 'codigo', rotulo: 'Código', obrigatorio: true, maxLength: 20, placeholder: 'Ex.: ADS201', transformar: v => v.toUpperCase() },
                { nome: 'nome', rotulo: 'Nome', obrigatorio: true, maxLength: 150, placeholder: 'Ex.: Banco de Dados' },
                { nome: 'cargaHoraria', rotulo: 'Carga horária (h)', tipo: 'number', obrigatorio: true, min: 1, max: 400, numerico: true, placeholder: '80', inputmode: 'numeric' }
            ],
            colunas: [
                { titulo: 'Código', texto: d => d.codigo, classe: 'mono' },
                { titulo: 'Nome', texto: d => d.nome },
                { titulo: 'Carga horária', texto: d => `${d.cargaHoraria} h`, classe: 'mono' }
            ]
        },
        turmas: {
            recurso: '/api/turmas',
            titulo: 'Turmas',
            singular: 'Turma',
            feminino: true,
            novo: 'Nova turma',
            descricao: 'Cadastre, edite e exclua as turmas e seus turnos.',
            descrever: t => `A turma "${t.nome}"`,
            linhas: [['nome', 'anoLetivo'], ['curso'], ['turno']],
            campos: [
                { nome: 'nome', rotulo: 'Nome da turma', obrigatorio: true, maxLength: 50, placeholder: 'Ex.: ADS-3N' },
                { nome: 'curso', rotulo: 'Curso', obrigatorio: true, maxLength: 150, placeholder: 'Ex.: Análise e Desenvolvimento de Sistemas' },
                { nome: 'anoLetivo', rotulo: 'Ano letivo', obrigatorio: true, maxLength: 6, padrao: '\\d{4}(/[12])?', mensagemFormato: 'Use AAAA ou AAAA/S (ex.: 2026/2).', placeholder: '2026/2' },
                { nome: 'turno', rotulo: 'Turno', tipo: 'select', obrigatorio: true, opcoes: Object.entries(Ui.TURNOS).map(([valor, texto]) => ({ valor, texto })) }
            ],
            colunas: [
                { titulo: 'Turma', texto: t => t.nome, classe: 'mono' },
                { titulo: 'Curso', texto: t => t.curso },
                { titulo: 'Ano letivo', texto: t => t.anoLetivo, classe: 'mono' },
                { titulo: 'Turno', texto: t => Ui.TURNOS[t.turno] }
            ]
        },
        horarios: {
            recurso: '/api/horarios',
            titulo: 'Horários',
            singular: 'Horário',
            novo: 'Novo horário',
            descricao: 'Distribua as aulas: professor, disciplina, turma, dia e intervalo de horário.',
            descrever: h => `O horário de ${h.disciplina.nome} (${h.diaSemanaDescricao}, ${h.horaInicio} – ${h.horaFim})`,
            linhas: [['professorId'], ['disciplinaId'], ['turmaId'], ['diaSemana', 'sala'], ['horaInicio', 'horaFim']],
            campos: [
                { nome: 'professorId', rotulo: 'Professor', tipo: 'select', obrigatorio: true, numerico: true, semOpcoes: 'Cadastre um professor primeiro', carregarOpcoes: opcoesDe('/api/professores', p => p.nome) },
                { nome: 'disciplinaId', rotulo: 'Disciplina', tipo: 'select', obrigatorio: true, numerico: true, semOpcoes: 'Cadastre uma disciplina primeiro', carregarOpcoes: opcoesDe('/api/disciplinas', d => `${d.codigo} - ${d.nome}`) },
                { nome: 'turmaId', rotulo: 'Turma', tipo: 'select', obrigatorio: true, numerico: true, semOpcoes: 'Cadastre uma turma primeiro', carregarOpcoes: opcoesDe('/api/turmas', t => `${t.nome} - ${t.curso}`) },
                { nome: 'diaSemana', rotulo: 'Dia da semana', tipo: 'select', obrigatorio: true, opcoes: Ui.DIAS.map(d => ({ valor: d.valor, texto: d.texto })) },
                { nome: 'sala', rotulo: 'Sala', maxLength: 30, placeholder: 'Ex.: Lab 01' },
                { nome: 'horaInicio', rotulo: 'Início', tipo: 'time', obrigatorio: true },
                { nome: 'horaFim', rotulo: 'Término', tipo: 'time', obrigatorio: true }
            ],
            validar: v => (v.horaInicio && v.horaFim && v.horaFim <= v.horaInicio)
                ? { horaFim: 'O término deve ser depois do início.' }
                : {},
            paraFormulario: h => ({
                professorId: h.professor.id,
                disciplinaId: h.disciplina.id,
                turmaId: h.turma.id,
                diaSemana: h.diaSemana,
                sala: h.sala,
                horaInicio: h.horaInicio,
                horaFim: h.horaFim
            }),
            colunas: [
                { titulo: 'Dia', texto: h => Ui.DIAS.find(d => d.valor === h.diaSemana).curto, classe: 'mono' },
                { titulo: 'Horário', texto: h => `${h.horaInicio} – ${h.horaFim}`, classe: 'mono' },
                { titulo: 'Disciplina', texto: h => h.disciplina.nome },
                { titulo: 'Professor', texto: h => h.professor.nome },
                { titulo: 'Turma', texto: h => h.turma.nome, classe: 'mono' },
                { titulo: 'Sala', texto: h => h.sala, classe: 'mono' }
            ]
        }
    };

    async function paginaInicio(raiz) {
        const contagens = Object.fromEntries(Object.keys(ENTIDADES).map(k => [k, el('strong', { text: '…' })]));
        raiz.replaceChildren(
            Ui.cabecalhoPagina(`Olá, ${usuario.username}`, 'Organize os horários de aula de professores, disciplinas e turmas.'),
            el('div', { class: 'resumo-numeros' }, Object.entries(ENTIDADES).map(([chave, cfg]) =>
                el('a', { class: 'cartao numero', href: `#/${chave}` }, el('span', { text: cfg.titulo }), contagens[chave]))),
            el('div', { class: 'atalhos' },
                el('a', { class: 'cartao atalho', href: '#/agenda/professor' },
                    el('h2', { text: 'Horários por professor →' }),
                    el('p', { text: 'Disciplinas ministradas, turmas associadas e grade semanal de aulas de um professor.' })),
                el('a', { class: 'cartao atalho', href: '#/agenda/turma' },
                    el('h2', { text: 'Horários por turma →' }),
                    el('p', { text: 'Professores responsáveis, disciplinas oferecidas e grade semanal de aulas de uma turma.' }))));

        await Promise.all(Object.entries(ENTIDADES).map(async ([chave, cfg]) => {
            try {
                contagens[chave].textContent = String((await Api.listar(cfg.recurso)).length);
            } catch {
                contagens[chave].textContent = '—';
            }
        }));
    }

    function rotaAtual() {
        const partes = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
        return partes.length ? partes : ['inicio'];
    }

    async function rotear() {
        if (!usuario) return;
        const versao = ++versaoRota;
        const [nome, tipo, id] = rotaAtual();
        const chaveMenu = nome === 'agenda' ? `agenda/${tipo}` : nome;

        menu.querySelectorAll('a[data-rota]').forEach(a => a.classList.toggle('ativo', a.dataset.rota === chaveMenu));
        fecharMenu();

        const raiz = el('div', {});
        conteudo.replaceChildren(raiz);
        conteudo.focus({ preventScroll: true });
        window.scrollTo(0, 0);

        const contexto = { podeEditar: usuario.perfil === 'ADMIN' };

        if (ENTIDADES[nome]) {
            document.title = `${ENTIDADES[nome].titulo} · Gestão de Horários`;
            await Crud.montar(raiz, ENTIDADES[nome], contexto);
        } else if (nome === 'agenda' && (tipo === 'professor' || tipo === 'turma')) {
            document.title = `Horários por ${tipo} · Gestão de Horários`;
            await Agenda.montar(raiz, tipo, id);
        } else if (nome === 'inicio') {
            document.title = 'Gestão de Horários';
            await paginaInicio(raiz);
        } else {
            location.replace('#/inicio');
        }
        return versao;
    }

    function mostrarLogin(mensagem = '') {
        usuario = null;
        aplicacao.hidden = true;
        telaLogin.hidden = false;
        erroLogin.textContent = mensagem;
        formLogin.reset();
        formLogin.usuario.focus();
    }

    function mostrarAplicacao() {
        telaLogin.hidden = true;
        aplicacao.hidden = false;
        document.getElementById('usuario-nome').textContent = usuario.username;
        document.getElementById('usuario-perfil').textContent = usuario.perfil === 'ADMIN' ? 'Administrador' : 'Consulta';
        rotear();
    }

    function sair(mensagem) {
        Api.sair();
        conteudo.replaceChildren();
        mostrarLogin(mensagem);
    }

    function fecharMenu() {
        menu.classList.remove('aberto');
        botaoMenu.setAttribute('aria-expanded', 'false');
    }

    formLogin.addEventListener('submit', async evento => {
        evento.preventDefault();
        const nome = formLogin.usuario.value.trim();
        const senha = formLogin.senha.value;
        if (!nome || !senha) {
            erroLogin.textContent = 'Informe usuário e senha.';
            return;
        }
        const botao = formLogin.querySelector('button[type="submit"]');
        botao.disabled = true;
        erroLogin.textContent = '';
        try {
            usuario = await Api.entrar(nome, senha);
            mostrarAplicacao();
        } catch (erro) {
            erroLogin.textContent = erro.status === 401 ? 'Usuário ou senha inválidos.' : erro.message;
        } finally {
            botao.disabled = false;
        }
    });

    document.getElementById('sair').addEventListener('click', () => sair(''));

    botaoMenu.addEventListener('click', () => {
        const aberto = menu.classList.toggle('aberto');
        botaoMenu.setAttribute('aria-expanded', String(aberto));
    });

    window.addEventListener('hashchange', rotear);

    Api.aoExpirar(() => sair('Sua sessão expirou. Entre novamente.'));

    async function iniciar() {
        if (Api.autenticado()) {
            try {
                usuario = await Api.get('/api/usuarios/me');
                mostrarAplicacao();
                return;
            } catch {
                Api.sair();
            }
        }
        mostrarLogin();
    }

    iniciar();
})();
