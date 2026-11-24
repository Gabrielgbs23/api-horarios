const Crud = (() => {
    const { el } = Ui;

    function mensagemValidacao(entrada, campo) {
        const v = entrada.validity;
        if (v.customError) return entrada.validationMessage;
        if (v.valueMissing) return 'Campo obrigatório.';
        if (v.badInput) return 'Valor inválido.';
        if (v.typeMismatch) return campo.tipo === 'email' ? 'Informe um e-mail válido.' : 'Valor inválido.';
        if (v.tooLong) return `Use no máximo ${entrada.maxLength} caracteres.`;
        if (v.rangeUnderflow) return `O valor mínimo é ${entrada.min}.`;
        if (v.rangeOverflow) return `O valor máximo é ${entrada.max}.`;
        if (v.stepMismatch) return 'Informe um número inteiro.';
        if (v.patternMismatch) return campo.mensagemFormato || 'Formato inválido.';
        return '';
    }

    function criarEntrada(campo) {
        const atributos = {
            id: `campo-${campo.nome}`,
            name: campo.nome,
            required: campo.obrigatorio,
            'aria-describedby': `erro-${campo.nome}`
        };
        if (campo.tipo === 'select') {
            return el('select', atributos);
        }
        return el('input', {
            ...atributos,
            type: campo.tipo || 'text',
            maxlength: campo.maxLength,
            min: campo.min,
            max: campo.max,
            step: campo.tipo === 'number' ? 1 : undefined,
            pattern: campo.padrao,
            placeholder: campo.placeholder,
            list: campo.sugestoes ? `lista-${campo.nome}` : undefined,
            autocomplete: 'off',
            inputmode: campo.inputmode
        });
    }

    function preencherOpcoes(select, opcoes, rotuloVazio) {
        const atual = select.value;
        select.replaceChildren(
            el('option', { value: '', text: rotuloVazio || 'Selecione…' }),
            ...opcoes.map(o => el('option', { value: String(o.valor), text: o.texto })));
        if (opcoes.some(o => String(o.valor) === atual)) {
            select.value = atual;
        }
    }

    async function montar(raiz, config, contexto) {
        const estado = { itens: [], editandoId: null, filtro: '', opcoes: {} };
        const podeEditar = contexto.podeEditar;

        const contador = el('span', { class: 'selo selo-cinza', text: '…' });
        const busca = el('input', {
            class: 'busca',
            type: 'search',
            placeholder: 'Filtrar…',
            'aria-label': `Filtrar ${config.titulo.toLowerCase()}`,
            oninput: evento => { estado.filtro = evento.target.value.trim().toLowerCase(); desenharTabela(); }
        });
        const corpoLista = el('div', {}, Ui.carregando());

        const cartaoLista = el('section', { class: 'cartao' },
            el('div', { class: 'cartao-cabecalho' },
                el('h2', {}, `${config.titulo} cadastrados `, contador),
                busca),
            corpoLista);

        const entradas = {};
        const mensagens = {};
        const blocos = {};
        let formulario = null;
        let tituloFormulario = null;
        let botaoSalvar = null;
        let botaoCancelar = null;
        let avisoFormulario = null;

        if (podeEditar) {
            const elementosCampos = [];
            const linhas = config.linhas || config.campos.map(c => [c.nome]);
            for (const linha of linhas) {
                const grupo = linha.map(nome => {
                    const campo = config.campos.find(c => c.nome === nome);
                    const entrada = criarEntrada(campo);
                    const mensagem = el('p', { class: 'mensagem-erro', id: `erro-${campo.nome}` });
                    entradas[campo.nome] = entrada;
                    mensagens[campo.nome] = mensagem;
                    entrada.addEventListener('input', () => { if (blocos[campo.nome].classList.contains('invalido')) validarCampo(campo); });
                    entrada.addEventListener('blur', () => { if (entrada.value !== '') validarCampo(campo); });
                    if (campo.tipo === 'select' && campo.opcoes) {
                        preencherOpcoes(entrada, campo.opcoes);
                    }
                    const bloco = el('label', { class: 'campo', for: entrada.id },
                        el('span', {}, campo.rotulo, campo.obrigatorio ? el('span', { class: 'obrigatorio', 'aria-hidden': 'true', text: ' *' }) : null),
                        entrada,
                        campo.sugestoes ? el('datalist', { id: `lista-${campo.nome}` }, campo.sugestoes.map(s => el('option', { value: s }))) : null,
                        campo.ajuda ? el('p', { class: 'ajuda', text: campo.ajuda }) : null,
                        mensagem);
                    blocos[campo.nome] = bloco;
                    return bloco;
                });
                elementosCampos.push(grupo.length > 1 ? el('div', { class: 'linha-campos' }, grupo) : grupo[0]);
            }

            tituloFormulario = el('h2', { text: config.novo });
            avisoFormulario = el('p', { class: 'aviso-form', role: 'alert', hidden: true });
            botaoSalvar = el('button', { class: 'btn btn-primario', type: 'submit', text: 'Salvar' });
            botaoCancelar = el('button', { class: 'btn', type: 'button', text: 'Cancelar', onclick: () => limparFormulario() });
            formulario = el('form', { class: 'formulario cartao-corpo', novalidate: true, onsubmit: salvar },
                avisoFormulario,
                elementosCampos,
                el('div', { class: 'form-acoes' }, botaoCancelar, botaoSalvar));

            raiz.replaceChildren(
                Ui.cabecalhoPagina(config.titulo, config.descricao),
                el('div', { class: 'grade-crud' },
                    el('section', { class: 'cartao' }, el('div', { class: 'cartao-cabecalho' }, tituloFormulario), formulario),
                    cartaoLista));
        } else {
            raiz.replaceChildren(
                Ui.cabecalhoPagina(config.titulo, config.descricao),
                el('p', { class: 'aviso-info', text: 'Seu perfil é somente de consulta: cadastro, edição e exclusão estão disponíveis apenas para administradores.' }),
                el('div', { class: 'grade-crud somente-lista' }, cartaoLista));
        }

        await carregarOpcoes();
        await carregarItens();

        async function carregarOpcoes() {
            if (!podeEditar) return;
            const dinamicos = config.campos.filter(c => c.carregarOpcoes);
            await Promise.all(dinamicos.map(async campo => {
                try {
                    const opcoes = await campo.carregarOpcoes();
                    estado.opcoes[campo.nome] = opcoes;
                    preencherOpcoes(entradas[campo.nome], opcoes, opcoes.length ? 'Selecione…' : campo.semOpcoes);
                } catch (erro) {
                    Ui.avisar(erro.message, 'erro');
                }
            }));
        }

        async function carregarItens() {
            try {
                estado.itens = await Api.listar(config.recurso);
                desenharTabela();
            } catch (erro) {
                corpoLista.replaceChildren(Ui.vazio(erro.message));
            }
        }

        function desenharTabela() {
            contador.textContent = String(estado.itens.length);
            const visiveis = estado.filtro
                ? estado.itens.filter(item => config.colunas.some(c => String(c.texto(item) ?? '').toLowerCase().includes(estado.filtro)))
                : estado.itens;

            if (!estado.itens.length) {
                corpoLista.replaceChildren(Ui.vazio(`Nenhum registro cadastrado. ${podeEditar ? 'Use o formulário para cadastrar.' : ''}`));
                return;
            }
            if (!visiveis.length) {
                corpoLista.replaceChildren(Ui.vazio('Nenhum registro corresponde ao filtro.'));
                return;
            }

            const tabela = el('table', {},
                el('thead', {}, el('tr', {},
                    config.colunas.map(c => el('th', { text: c.titulo })),
                    podeEditar ? el('th', { class: 'acoes-linha' }, el('span', { class: 'sr-only', text: 'Ações' })) : null)),
                el('tbody', {}, visiveis.map(item => el('tr', { class: item.id === estado.editandoId ? 'editando' : '' },
                    config.colunas.map(c => el('td', { class: c.classe }, c.celula ? c.celula(item) : c.texto(item) ?? '—')),
                    podeEditar ? el('td', { class: 'acoes-linha' },
                        el('button', { class: 'btn btn-pequeno', type: 'button', text: 'Editar', onclick: () => editar(item) }),
                        el('button', { class: 'btn btn-pequeno btn-texto-perigo', type: 'button', text: 'Excluir', onclick: () => excluir(item) })) : null))));
            corpoLista.replaceChildren(el('div', { class: 'tabela-rolagem' }, tabela));
        }

        function marcarErro(nome, mensagem) {
            blocos[nome].classList.toggle('invalido', Boolean(mensagem));
            entradas[nome].setAttribute('aria-invalid', mensagem ? 'true' : 'false');
            mensagens[nome].textContent = mensagem || '';
        }

        function valoresAtuais() {
            const valores = {};
            for (const campo of config.campos) {
                valores[campo.nome] = entradas[campo.nome].value.trim();
            }
            return valores;
        }

        function validarCampo(campo) {
            const entrada = entradas[campo.nome];
            entrada.setCustomValidity('');
            if (campo.obrigatorio && entrada.value.trim() === '' && entrada.value !== '') {
                entrada.setCustomValidity('Campo obrigatório.');
            }
            if (config.validar && entrada.validity.valid) {
                const erros = config.validar(valoresAtuais());
                if (erros[campo.nome]) entrada.setCustomValidity(erros[campo.nome]);
            }
            const mensagem = entrada.validity.valid ? '' : mensagemValidacao(entrada, campo);
            marcarErro(campo.nome, mensagem);
            return !mensagem;
        }

        function validarTudo() {
            let primeiroInvalido = null;
            for (const campo of config.campos) {
                if (!validarCampo(campo) && !primeiroInvalido) {
                    primeiroInvalido = entradas[campo.nome];
                }
            }
            if (primeiroInvalido) primeiroInvalido.focus();
            return !primeiroInvalido;
        }

        function montarEnvio() {
            const dados = {};
            for (const campo of config.campos) {
                const bruto = entradas[campo.nome].value.trim();
                if (bruto === '') {
                    dados[campo.nome] = null;
                } else if (campo.numerico) {
                    dados[campo.nome] = Number(bruto);
                } else {
                    dados[campo.nome] = campo.transformar ? campo.transformar(bruto) : bruto;
                }
            }
            return dados;
        }

        async function salvar(evento) {
            evento.preventDefault();
            avisoFormulario.hidden = true;
            if (!validarTudo()) {
                avisoFormulario.textContent = 'Corrija os campos destacados.';
                avisoFormulario.hidden = false;
                return;
            }
            botaoSalvar.disabled = true;
            try {
                const dados = montarEnvio();
                if (estado.editandoId) {
                    await Api.atualizar(config.recurso, estado.editandoId, dados);
                    Ui.avisar(`${config.singular} atualizad${config.feminino ? "a" : "o"} com sucesso.`);
                } else {
                    await Api.criar(config.recurso, dados);
                    Ui.avisar(`${config.singular} cadastrad${config.feminino ? "a" : "o"} com sucesso.`);
                }
                limparFormulario();
                await carregarItens();
            } catch (erro) {
                const campos = Object.entries(erro.campos || {});
                campos.forEach(([nome, mensagem]) => { if (blocos[nome]) marcarErro(nome, mensagem); });
                avisoFormulario.textContent = campos.length ? 'Corrija os campos destacados.' : erro.message;
                avisoFormulario.hidden = false;
            } finally {
                botaoSalvar.disabled = false;
            }
        }

        function limparFormulario() {
            formulario.reset();
            estado.editandoId = null;
            tituloFormulario.textContent = config.novo;
            botaoSalvar.textContent = 'Salvar';
            avisoFormulario.hidden = true;
            config.campos.forEach(c => { entradas[c.nome].setCustomValidity(''); marcarErro(c.nome, ''); });
            desenharTabela();
        }

        function editar(item) {
            limparFormulario();
            estado.editandoId = item.id;
            const valores = config.paraFormulario ? config.paraFormulario(item) : item;
            for (const campo of config.campos) {
                const valor = valores[campo.nome];
                entradas[campo.nome].value = valor === null || valor === undefined ? '' : String(valor);
            }
            tituloFormulario.textContent = `Editando ${config.singular.toLowerCase()} #${item.id}`;
            botaoSalvar.textContent = 'Salvar alterações';
            desenharTabela();
            formulario.closest('.cartao').scrollIntoView({ behavior: 'smooth', block: 'start' });
            entradas[config.campos[0].nome].focus({ preventScroll: true });
        }

        async function excluir(item) {
            const confirmado = await Ui.confirmar(`Excluir ${config.singular.toLowerCase()}?`,
                `${config.descrever(item)} será excluíd${config.feminino ? "a" : "o"} permanentemente. Esta ação não pode ser desfeita.`);
            if (!confirmado) return;
            try {
                await Api.excluir(config.recurso, item.id);
                Ui.avisar(`${config.singular} excluíd${config.feminino ? "a" : "o"} com sucesso.`);
                if (estado.editandoId === item.id) limparFormulario();
                await carregarItens();
            } catch (erro) {
                Ui.avisar(erro.message, 'erro');
            }
        }
    }

    return { montar };
})();
