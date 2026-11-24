import { spawn } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { pathToFileURL } from 'node:url';

const [entrada, saida] = process.argv.slice(2);
const CHROME = 'C:/Program Files/Google/Chrome/Application/chrome.exe';
const PORTA = 9334;
const perfil = fs.mkdtempSync(path.join(os.tmpdir(), 'chrome-pdf-'));
const chrome = spawn(CHROME, ['--headless=new', `--remote-debugging-port=${PORTA}`, `--user-data-dir=${perfil}`, '--no-first-run', '--allow-file-access-from-files', 'about:blank'], { stdio: 'ignore' });
const dormir = ms => new Promise(r => setTimeout(r, ms));

let alvo;
for (let i = 0; i < 50 && !alvo; i++) {
    try { alvo = (await (await fetch(`http://127.0.0.1:${PORTA}/json/list`)).json()).find(t => t.type === 'page'); } catch { await dormir(200); }
}
const ws = new WebSocket(alvo.webSocketDebuggerUrl);
await new Promise(r => ws.addEventListener('open', r, { once: true }));
let seq = 0;
const pendentes = new Map();
ws.addEventListener('message', m => {
    const d = JSON.parse(m.data);
    if (d.id && pendentes.has(d.id)) { const p = pendentes.get(d.id); pendentes.delete(d.id); d.error ? p.falha(new Error(JSON.stringify(d.error))) : p.ok(d.result); }
});
const enviar = (method, params = {}) => new Promise((ok, falha) => { const id = ++seq; pendentes.set(id, { ok, falha }); ws.send(JSON.stringify({ id, method, params })); });

try {
    await enviar('Page.enable');
    await enviar('Page.navigate', { url: pathToFileURL(path.resolve(entrada)).href });
    for (let i = 0; i < 100; i++) {
        const r = await enviar('Runtime.evaluate', { expression: `document.readyState === 'complete' && [...document.images].every(i => i.complete && i.naturalWidth > 0)`, returnByValue: true });
        if (r.result.value) break;
        await dormir(100);
    }
    const cm = v => v / 2.54;
    const pdf = await enviar('Page.printToPDF', {
        paperWidth: cm(21), paperHeight: cm(29.7),
        marginTop: cm(3), marginLeft: cm(3), marginRight: cm(2), marginBottom: cm(2),
        printBackground: true,
        displayHeaderFooter: true,
        headerTemplate: `<div style="width:100%;font-family:Arial,Helvetica,sans-serif;font-size:10pt;text-align:right;padding:0 2cm 0 0;margin-top:1.2cm;-webkit-print-color-adjust:exact"><span class="pageNumber"></span></div>`,
        footerTemplate: '<div></div>',
        preferCSSPageSize: false
    });
    fs.writeFileSync(saida, Buffer.from(pdf.data, 'base64'));
    console.log(`PDF gerado: ${saida}`);
} finally {
    ws.close();
    chrome.kill();
}
