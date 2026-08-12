const fs = require('fs');
const path = require('path');

const BASE_DIR_SCRIPT = __dirname;
const BASE_DIR = path.dirname(BASE_DIR_SCRIPT);
const VANILLA_DIR = path.join(BASE_DIR, "Pack (Next)", "assets");
const EXTRACT_DIR = path.join(BASE_DIR, "Extract", "assets");
const REPORT_PATH = path.join(BASE_DIR, "IndexData", "Index_Comparaison_Missing.json");

function runExtraction() {
	console.log("\n=== DÉBUT DE L'EXTRACTION DES FICHIERS MANQUANTS ===");

	if (!fs.existsSync(REPORT_PATH)) {
		console.log(" [ERREUR] Aucun rapport de comparaison trouvé.");
		return;
	}

	const comparaisonData = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf-8'));
	const missingFiles = comparaisonData.filter(item => item.status === 'missing').map(item => item.path);

	if (missingFiles.length === 0) {
		console.log(" -> Aucun fichier manquant à extraire !");
		return;
	}

	const parentExtractDir = path.dirname(EXTRACT_DIR);
	if (fs.existsSync(parentExtractDir)) {
		try {
			fs.rmSync(parentExtractDir, { recursive: true, force: true });
		} catch (e) {
			console.log(` [AVERTISSEMENT] Erreur réinitialisation Extract : ${e.message}`);
		}
	}

	fs.mkdirSync(EXTRACT_DIR, { recursive: true });
	let compteur = 0;
	let erreurs = 0;

	for (const relPath of missingFiles) {
		const srcFile = path.normalize(path.join(VANILLA_DIR, relPath));
		const destFile = path.normalize(path.join(EXTRACT_DIR, relPath));

		if (fs.existsSync(srcFile)) {
			fs.mkdirSync(path.dirname(destFile), { recursive: true });
			fs.copyFileSync(srcFile, destFile);
			compteur++;
		} else {
			erreurs++;
		}
	}

	console.log(`=== EXTRACTION TERMINÉE : ${compteur} fichiers copiés (${erreurs} introuvables) ===\n`);
}

if (require.main === module) runExtraction();
module.exports = { runExtraction };