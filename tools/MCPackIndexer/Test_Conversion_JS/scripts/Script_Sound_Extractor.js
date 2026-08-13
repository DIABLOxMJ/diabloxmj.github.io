const fs = require('fs');
const path = require('path');
const config = require('./Script_Config');

const SOUND_OUTPUT = config.PATHS.Sound.SOUND_OUTPUT_DIR;
const MINECRAFT_PATH = config.MINECRAFT_PATH;
const VERSION = process.argv[2] || '1.18';

const OBJECTS_DIR = path.join(MINECRAFT_PATH, 'assets', 'objects');

function extractSounds(version = VERSION) {
	const indexPath = path.join(MINECRAFT_PATH, 'assets', 'indexes', `${version}.json`);
	console.log(`Analyse de l'index de Minecraft (Version : ${version})...`);
	
	if (!fs.existsSync(indexPath)) {
		console.log(`[ERREUR] Index introuvable : ${indexPath}`);
		return;
	}

	const data = JSON.parse(fs.readFileSync(indexPath, 'utf-8'));
	let compteur = 0;

	for (const [filePath, info] of Object.entries(data.objects)) {
		if (filePath.includes('sounds/') || filePath.endsWith('sounds.json')) {
			const hashVal = info.hash;
			const sourceFile = path.join(OBJECTS_DIR, hashVal.substring(0, 2), hashVal);
			const destFile = path.join(SOUND_OUTPUT, filePath);

			if (fs.existsSync(sourceFile)) {
				fs.mkdirSync(path.dirname(destFile), { recursive: true });
				fs.copyFileSync(sourceFile, destFile);
				compteur++;
			}
		}
	}

	console.log(`Terminé ! ${compteur} fichiers audio extraits dans '${SOUND_OUTPUT}' !`);
}

if (require.main === module) extractSounds();
module.exports = { extractSounds };