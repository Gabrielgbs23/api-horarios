const Ui = (() => {
    function el(tag, propriedades, ...filhos) {
        const elemento = document.createElement(tag);
        for (const [chave, valor] of Object.entries(propriedades || {})) {
            if (valor === undefined || valor === null || valor === false) {
                continue;
            }
            if (chave === 'class') {
                elemento.className = valor;
            } else if (chave === 'text') {
                elemento.textContent = valor;
            } else if (chave === 'value') {
                elemento.value = valor;
            } else if (chave === 'dataset') {
                Object.assign(elemento.dataset, valor);
            } else if (chave.startsWith('on') && typeof valor === 'function') {
                elemento.addEventListener(chave.slice(2), valor);
            } else if (valor === true) {
                elemento.setAttribute(chave, '');
            } else {
                elemento.setAttribute(chave, valor);
            }
        }
        for (const filho of filhos.flat(Infinity)) {
            if (filho === null || filho === undefined || filho === false) {
                continue;
            }
            elemento.append(filho instanceof Node ? filho : document.createTextNode(String(filho)));
        }
        return elemento;
    }

    function avisar(mensagem, tipo = 'sucesso') {
        const area = document.getElementById('avisos');
        const aviso = el('div', { class: `aviso ${tipo}`, role: tipo === 'erro' ? 'alert' : 'status', text: mensagem });
        area.append(aviso);
        setTimeout(() => aviso.remove(), tipo === 'erro' ? 6000 : 3500);
    }

    function confirmar(titulo, mensagem) {
        const dialogo = document.getElementById('dialogo-confirmacao');
        document.getElementById('dialogo-titulo').textContent = titulo;
        document.getElementById('dialogo-mensagem').textContent = mensagem;
        dialogo.returnValue = '';
        return new Promise(resolver => {
            dialogo.addEventListener('close', () => resolver(dialogo.returnValue === 'confirmar'), { once: true });
            dialogo.showModal();
        });
    }

    function vazio(texto) {
        return el('div', { class: 'vazio', text: texto });
    }

    function carregando(texto = 'Carregando…') {
        return el('div', { class: 'carregando', text: texto });
    }

    function cabecalhoPagina(titulo, descricao, ...acoes) {
        return el('div', { class: 'pagina-topo' },
            el('div', {}, el('h1', { text: titulo }), descricao ? el('p', { text: descricao }) : null),
            acoes.length ? el('div', {}, acoes) : null);
    }

    const TURNOS = { MATUTINO: 'Matutino', VESPERTINO: 'Vespertino', NOTURNO: 'Noturno' };

    const DIAS = [
        { valor: 'SEGUNDA', texto: 'Segunda-feira', curto: 'Segunda' },
        { valor: 'TERCA', texto: 'Terça-feira', curto: 'Terça' },
        { valor: 'QUARTA', texto: 'Quarta-feira', curto: 'Quarta' },
        { valor: 'QUINTA', texto: 'Quinta-feira', curto: 'Quinta' },
        { valor: 'SEXTA', texto: 'Sexta-feira', curto: 'Sexta' },
        { valor: 'SABADO', texto: 'Sábado', curto: 'Sábado' }
    ];

    function minutos(hora) {
        const [h, m] = String(hora).split(':').map(Number);
        return h * 60 + m;
    }

    function formatarHoras(totalMinutos) {
        const horas = Math.floor(totalMinutos / 60);
        const resto = totalMinutos % 60;
        return resto ? `${horas}h${String(resto).padStart(2, '0')}` : `${horas}h`;
    }

    return { el, avisar, confirmar, vazio, carregando, cabecalhoPagina, TURNOS, DIAS, minutos, formatarHoras };
})();
