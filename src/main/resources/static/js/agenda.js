const Agenda = (() => {
    const { el } = Ui;

    const TIPOS = {
        professor: {
            titulo: 'Horários por professor',
            descricao: 'Selecione um professor para ver as disciplinas ministradas, as turmas associadas e os dias e horários das aulas.',
            rotuloSeletor: 'Professor',
            recursoLista: '/api/professores',
            opcao: p => ({ valor: p.id, texto: p.nome }),
            agenda: id => `/api/agenda/professores/${id}`,
            vazioLista: 'Nenhum professor cadastrado.',
            cabecalho: d => ({
                titulo: d.professor.nome,
                subtitulo: [d.professor.titulacao, d.professor.email].filter(Boolean).join(' · ')
            }),
            resumos: d => [
                {
                    titulo: 'Disciplinas ministradas',
                    vazio: 'Nenhuma disciplina vinculada.',
                    itens: d.disciplinas.map(x => ({ principal: x.nome, secundario: x.codigo, lateral: `${x.cargaHoraria} h` }))
                },
                {
                    titulo: 'Turmas associadas',
                    vazio: 'Nenhuma turma vinculada.',
                    itens: d.turmas.map(x => ({ principal: x.nome, secundario: x.curso, lateral: `${Ui.TURNOS[x.turno]} · ${x.anoLetivo}` }))
                }
            ],
            detalheAula: a => [`Turma ${a.turma}`, a.sala]
        },
        turma: {
            titulo: 'Horários por turma',
            descricao: 'Selecione uma turma para ver os professores responsáveis, as disciplinas oferecidas e os dias e horários das aulas.',
            rotuloSeletor: 'Turma',
            recursoLista: '/api/turmas',
            opcao: t => ({ valor: t.id, texto: `${t.nome} — ${t.curso}` }),
            agenda: id => `/api/agenda/turmas/${id}`,
            vazioLista: 'Nenhuma turma cadastrada.',
            cabecalho: d => ({
                titulo: `Turma ${d.turma.nome}`,
                subtitulo: `${d.turma.curso} · ${Ui.TURNOS[d.turma.turno]} · ${d.turma.anoLetivo}`
            }),
            resumos: d => [
                {
                    titulo: 'Professores responsáveis',
                    vazio: 'Nenhum professor vinculado.',
                    itens: d.professores.map(x => ({ principal: x.nome, secundario: x.email, lateral: x.titulacao || '' }))
                },
                {
                    titulo: 'Disciplinas oferecidas',
                    vazio: 'Nenhuma disciplina vinculada.',
                    itens: d.disciplinas.map(x => ({ principal: x.nome, secundario: x.codigo, lateral: `${x.cargaHoraria} h` }))
                }
            ],
            detalheAula: a => [`Prof. ${a.professor}`, a.sala]
        }
    };

    async function montar(raiz, tipo, idInicial) {
        const cfg = TIPOS[tipo];
        const seletor = el('select', { id: 'seletor-agenda', name: 'seletor' });
        const resultado = el('div', {});

        raiz.replaceChildren(
            Ui.cabecalhoPagina(cfg.titulo, cfg.descricao),
            el('div', { class: 'seletor' },
                el('label', { class: 'campo', for: 'seletor-agenda' }, el('span', { text: cfg.rotuloSeletor }), seletor)),
            resultado);

        let lista;
        try {
            lista = await Api.listar(cfg.recursoLista);
        } catch (erro) {
            resultado.replaceChildren(Ui.vazio(erro.message));
            return;
        }

        seletor.replaceChildren(
            el('option', { value: '', text: lista.length ? `Selecione ${tipo === 'professor' ? 'um professor' : 'uma turma'}…` : cfg.vazioLista }),
            ...lista.map(item => {
                const o = cfg.opcao(item);
                return el('option', { value: String(o.valor), text: o.texto });
            }));

        seletor.addEventListener('change', () => {
            const id = seletor.value;
            history.replaceState(null, '', id ? `#/agenda/${tipo}/${id}` : `#/agenda/${tipo}`);
            carregar(id);
        });

        if (idInicial && lista.some(item => String(item.id) === String(idInicial))) {
            seletor.value = String(idInicial);
            await carregar(String(idInicial));
        } else {
            resultado.replaceChildren(el('section', { class: 'cartao' }, Ui.vazio(lista.length
                ? `Selecione ${tipo === 'professor' ? 'um professor' : 'uma turma'} para visualizar os horários.`
                : cfg.vazioLista)));
        }

        async function carregar(id) {
            if (!id) {
                resultado.replaceChildren(el('section', { class: 'cartao' }, Ui.vazio('Nenhuma seleção.')));
                return;
            }
            resultado.replaceChildren(el('section', { class: 'cartao' }, Ui.carregando()));
            try {
                const dados = await Api.get(cfg.agenda(id));
                if (seletor.value === String(id)) {
                    resultado.replaceChildren(...desenhar(cfg, dados));
                }
            } catch (erro) {
                resultado.replaceChildren(el('section', { class: 'cartao' }, Ui.vazio(erro.message)));
            }
        }
    }

    function desenhar(cfg, dados) {
        const cabecalho = cfg.cabecalho(dados);
        const totalMinutos = dados.aulas.reduce((soma, a) => soma + Ui.minutos(a.horaFim) - Ui.minutos(a.horaInicio), 0);
        const diasComAula = new Set(dados.aulas.map(a => a.diaSemana)).size;

        const topo = el('section', { class: 'cartao agenda-cabecalho' },
            el('div', {}, el('h2', { text: cabecalho.titulo }), el('p', { text: cabecalho.subtitulo })),
            el('div', { class: 'agenda-estatisticas' },
                estatistica(dados.aulas.length, 'aulas por semana'),
                estatistica(diasComAula, 'dias com aula'),
                estatistica(Ui.formatarHoras(totalMinutos), 'carga semanal')));

        const resumos = el('div', { class: 'agenda-resumos' },
            cfg.resumos(dados).map(r => el('section', { class: 'cartao' },
                el('div', { class: 'cartao-cabecalho' }, el('h2', {}, r.titulo, ' ', el('span', { class: 'selo selo-cinza', text: String(r.itens.length) }))),
                r.itens.length
                    ? el('ul', { class: 'lista-resumo' }, r.itens.map(i => el('li', {},
                        el('div', {}, el('strong', { text: i.principal }), el('span', { class: 'secundario', text: i.secundario })),
                        el('span', { class: 'secundario', text: i.lateral }))))
                    : Ui.vazio(r.vazio))));

        const semana = el('section', { class: 'cartao' },
            el('div', { class: 'cartao-cabecalho' }, el('h2', { text: 'Dias e horários das aulas' })),
            dados.aulas.length
                ? el('div', { class: 'semana' }, Ui.DIAS.map(dia => {
                    const aulas = dados.aulas.filter(a => a.diaSemana === dia.valor);
                    return el('div', { class: 'dia' },
                        el('h3', { text: dia.curto }),
                        aulas.length
                            ? aulas.map(a => el('article', { class: 'aula' },
                                el('span', { class: 'aula-hora', text: `${a.horaInicio} – ${a.horaFim}` }),
                                el('span', { class: 'aula-titulo', text: a.disciplina }),
                                cfg.detalheAula(a).filter(Boolean).map(t => el('span', { class: 'aula-detalhe', text: t }))))
                            : el('span', { class: 'dia-vazio', text: 'Sem aulas' }));
                }))
                : Ui.vazio('Nenhuma aula cadastrada.'));

        return [topo, resumos, semana];
    }

    function estatistica(valor, rotulo) {
        return el('div', {}, el('strong', { text: String(valor) }), el('span', { text: rotulo }));
    }

    return { montar };
})();
