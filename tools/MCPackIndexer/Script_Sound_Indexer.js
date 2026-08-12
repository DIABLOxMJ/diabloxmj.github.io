const fs = require('fs');
const path = require('path');

const BASE_DIR_SCRIPT = __dirname;
const BASE_DIR = path.dirname(BASE_DIR_SCRIPT);

// Pour Windows %APPDATA% ou chemin fixe, sinon personnalisable
const MINECRAFT_PATH = process.env.MINECRAFT_PATH || "G:\\Minecraft"; 
const VERSION = process.argv[2] || '1.18';

const INDEX_PATH = path.join(MINECRAFT_PATH, 'assets', 'indexes', `${VERSION}.json`);
const OBJECTS_DIR = path.join(MINECRAFT_PATH, 'assets', 'objects');
const OUTPUT_DIR = path.join(BASE_DIR, 'Pack (Sound)');

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
			const destFile = path.join(OUTPUT_DIR, filePath);

			if (fs.existsSync(sourceFile)) {
				fs.mkdirSync(path.dirname(destFile), { recursive: true });
				fs.copyFileSync(sourceFile, destFile);
				compteur++;
			}
		}
	}

	console.log(`Terminé ! ${compteur} fichiers audio extraits dans '${OUTPUT_DIR}' !`);
}

if (require.main === module) extractSounds();
module.exports = { extractSounds };