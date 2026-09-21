const sharp=require('C:/Users/anh15/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
(async()=>{for(let i=1;i<=8;i++){const b='tmp/erd-sections-qa/ERD_0'+i;const m=await sharp(b+'.svg').metadata();console.log(i,m.width,m.height);await sharp(b+'.svg').flatten({background:'white'}).png().toFile(b+'.png')}})();
