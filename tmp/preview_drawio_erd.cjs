const sharp=require('C:/Users/anh15/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
(async()=>{
const p='output/report/drawio-erd/routing-preview.svg';
await sharp(p).resize({width:1400}).png().toFile('tmp/drawio-erd-overview.png');
await sharp(p).extract({left:1600,top:120,width:1400,height:1400}).png().toFile('tmp/drawio-erd-detail.png');
})();
