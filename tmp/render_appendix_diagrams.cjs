const fs=require('fs'),path=require('path');
const sharp=require('C:/Users/anh15/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
(async()=>{const dir='output/report/use-case-activity-7-17-7-19';for(const file of fs.readdirSync(dir).filter(x=>x.endsWith('.svg'))){await sharp(path.join(dir,file),{density:144}).png().toFile(path.join(dir,file.replace('.svg','.png')));console.log(file)}})();
