const sharp=require('C:/Users/anh15/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
(async()=>{const p='tmp/erd-routing/ERD_43_bang_doc_theo_nhom.svg'; const m=await sharp(p).metadata();console.log(m.width,m.height);await sharp(p).flatten({background:'white'}).resize({width:1800}).png().toFile('tmp/erd-routing/grouped.png')})();
