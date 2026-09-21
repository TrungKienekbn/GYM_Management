const sharp=require('C:/Users/anh15/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/sharp');
(async()=>{await sharp('tmp/erd-style-qa/ERD_04.svg').flatten({background:'white'}).resize({width:1500}).png().toFile('tmp/erd-style-qa/preview.png');})();
