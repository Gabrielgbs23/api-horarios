const Api = (() => {
    const CHAVE = 'horarios.credenciais';
    let aoExpirar = () => {};

    class ApiErro extends Error {
        constructor(status, mensagem, campos = {}) {
            super(mensagem);
            this.status = status;
            this.campos = campos;
        }
    }

    let memoria = null;

    function lerCredenciais() {
        try {
            return sessionStorage.getItem(CHAVE) || memoria;
        } catch {
            return memoria;
        }
    }

    function gravarCredenciais(valor) {
        memoria = valor;
        try {
            if (valor) {
                sessionStorage.setItem(CHAVE, valor);
            } else {
                sessionStorage.removeItem(CHAVE);
            }
        } catch {
        }
    }

    function codificar(usuario, senha) {
        const bytes = new TextEncoder().encode(`${usuario}:${senha}`);
        let binario = '';
        bytes.forEach(b => { binario += String.fromCharCode(b); });
        return btoa(binario);
    }

    function mensagemPadrao(status) {
        const mensagens = {
            400: 'Dados inválidos.',
            401: 'Usuário ou senha inválidos.',
            403: 'Seu perfil não tem permissão para esta operação.',
            404: 'Registro não encontrado.',
            409: 'A operação conflita com dados existentes.',
            422: 'Os dados informados não atendem às regras do sistema.'
        };
        return mensagens[status] || 'Ocorreu um erro inesperado. Tente novamente.';
    }

    async function requisitar(metodo, caminho, corpo, token = lerCredenciais()) {
        const cabecalhos = {
            'Accept': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
        };
        if (token) {
            cabecalhos.Authorization = `Basic ${token}`;
        }
        if (corpo !== undefined) {
            cabecalhos['Content-Type'] = 'application/json';
        }

        let resposta;
        try {
            resposta = await fetch(caminho, {
                method: metodo,
                headers: cabecalhos,
                body: corpo === undefined ? undefined : JSON.stringify(corpo)
            });
        } catch {
            throw new ApiErro(0, 'Não foi possível conectar ao servidor.');
        }

        if (resposta.status === 204) {
            return null;
        }

        const texto = await resposta.text();
        let dados = null;
        if (texto) {
            try {
                dados = JSON.parse(texto);
            } catch {
                dados = null;
            }
        }

        if (!resposta.ok) {
            if (resposta.status === 401 && token && token === lerCredenciais()) {
                aoExpirar();
            }
            const mensagem = resposta.status === 401 ? mensagemPadrao(401) : (dados && dados.mensagem) || mensagemPadrao(resposta.status);
            throw new ApiErro(resposta.status, mensagem, (dados && dados.campos) || {});
        }
        return dados;
    }

    async function entrar(usuario, senha) {
        const token = codificar(usuario, senha);
        const usuarioAtual = await requisitar('GET', '/api/usuarios/me', undefined, token);
        gravarCredenciais(token);
        return usuarioAtual;
    }

    return {
        ApiErro,
        entrar,
        sair: () => gravarCredenciais(null),
        autenticado: () => Boolean(lerCredenciais()),
        aoExpirar: callback => { aoExpirar = callback; },
        listar: recurso => requisitar('GET', recurso),
        buscar: (recurso, id) => requisitar('GET', `${recurso}/${id}`),
        criar: (recurso, dados) => requisitar('POST', recurso, dados),
        atualizar: (recurso, id, dados) => requisitar('PUT', `${recurso}/${id}`, dados),
        excluir: (recurso, id) => requisitar('DELETE', `${recurso}/${id}`),
        get: caminho => requisitar('GET', caminho)
    };
})();
