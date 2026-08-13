const fs = require('fs');
const path = require('path');
const crypto = require('crypto');

// Définition des chemins (identique au script Python)
const BASE_DIR_SCRIPT = __dirname;
const BASE_DIR = path.dirname(BASE_DIR_SCRIPT);
const NEXT_DIR = path.join(BASE_DIR, "Pack (Next)", "assets");
const MAIN_DIR = path.join(BASE_DIR, "Pack (Main)", "assets");
const OUTPUT_DIR = path.join(BASE_DIR, "IndexData");

/**
 * Scanne récursivement 100% des fichiers d'un dossier assets
 * et retourne une liste de chemins relatifs.
 */
function scanAssetsFolder(targetDir) {
	let fileList = [];
	if (!fs.existsSync(targetDir)) {
		return fileList;
	}

	function walkSync(currentDir) {
		const entries = fs.readdirSync(currentDir, { withFileTypes: true });
		for (const entry of entries) {
			const fullPath = path.join(currentDir, entry.name);
			if (entry.isDirectory()) {
				walkSync(fullPath);
			} else if (entry.isFile()) {
				const relPath = path.relative(targetDir, fullPath).replace(/\\/g, "/");
				fileList.push(relPath);
			}
		}
	}

	walkSync(targetDir);
	return fileList.sort();
}

/**
 * Trie récursivement les clés d'un objet JS pour garantir
 * la reproductibilité de JSON.stringify (équivalent sort_keys=True)
 */
function sortObjectKeys(obj) {
	if (obj === null || typeof obj !== 'object') {
		return obj;
	}
	if (Array.isArray(obj)) {
		return obj.map(sortObjectKeys);
	}
	return Object.keys(obj)
		.sort()
		.reduce((acc, key) => {
			acc[key] = sortObjectKeys(obj[key]);
			return acc;
		}, {});
}

/**
 * Calcule le hash MD5 d'un fichier.
 * Pour le JSON/.mcmeta, reformatte en compact sans espaces avec clés triées.
 */
function computeFileHash(filePath) {
	if (!fs.existsSync(filePath)) {
		return null;
	}

	try {
		if (filePath.endsWith('.json') || filePath.endsWith('.mcmeta')) {
			const rawText = fs.readFileSync(filePath, 'utf-8');
			const data = JSON.parse(rawText);
			const sortedData = sortObjectKeys(data);
			
			// Reformatage compact sans espaces
			const cleanStr = JSON.stringify(sortedData);
			return crypto.createHash('md5').update(cleanStr, 'utf-8').digest('hex');
		} else {
			// Pour les images PNG, OGG, etc., calcul binaire direct
			const fileBuffer = fs.readFileSync(filePath);
			return crypto.createHash('md5').update(fileBuffer).digest('hex');
		}
	} catch (err) {
		// Fallback en lecture binaire directe si le JSON est malformé
		try {
			const fileBuffer = fs.readFileSync(filePath);
			return crypto.createHash('md5').update(fileBuffer).digest('hex');
		} catch (fallbackErr) {
			return null;
		}
	}
}

/**
 * Fonction principale exécutant l'indexation et la comparaison
 */
function runComparaison() {
	console.log("\n=== DÉBUT DE L'INDEXATION COMPLÈTE & COMPARAISON ===");
	fs.mkdirSync(OUTPUT_DIR, { recursive: true });

	console.log(`[SCAN] Analyse du dossier Vanilla : ${NEXT_DIR}...`);
	const nextFiles = scanAssetsFolder(NEXT_DIR);
	fs.writeFileSync(
		path.join(OUTPUT_DIR, "Index_NextPack.json"),
		JSON.stringify(nextFiles, null, 4),
		'utf-8'
	);
	console.log(` -> ${nextFiles.length} fichiers trouvés dans Vanilla.`);

	console.log(`[SCAN] Analyse du dossier personnel (Pack) : ${MAIN_DIR}...`);
	const mainFiles = scanAssetsFolder(MAIN_DIR);
	fs.writeFileSync(
		path.join(OUTPUT_DIR, "Index_OldPack.json"),
		JSON.stringify(mainFiles, null, 4),
		'utf-8'
	);
	console.log(` -> ${mainFiles.length} fichiers trouvés dans ton Pack.`);

	const nextSet = new Set(nextFiles);
	const mainSet = new Set(mainFiles);

	// Calcul des fichiers manquants dans le pack utilisateur
	const manquants = Array.from(nextSet).filter(file => !mainSet.has(file)).sort();

	const comparaisonResult = [];
	console.log("[COMPARAISON] Analyse des différences de contenu via Hash MD5...");

	for (const relPath of nextFiles) {
		const parts = relPath.split('/');
		const category = parts.length > 1 ? parts[1] : "root";
		const isMissing = manquants.includes(relPath);
		let hasContentDiff = false;

		// Si le fichier est présent dans les deux packs, on compare les MD5
		if (!isMissing) {
			const pathMain = path.join(MAIN_DIR, relPath);
			const pathNext = path.join(NEXT_DIR, relPath);

			const hashMain = computeFileHash(pathMain);
			hashNext = computeFileHash(pathNext);

			if (hashMain && hashNext && hashMain !== hashNext) {
				hasContentDiff = true;
			}
		}

		comparaisonResult.push({
			path: relPath,
			filename: parts[parts.length - 1],
			category: category,
			status: isMissing ? "missing" : "present",
			has_content_diff: hasContentDiff
		});
	}

	fs.writeFileSync(
		path.join(OUTPUT_DIR, "Index_Comparaison_Missing.json"),
		JSON.stringify(comparaisonResult, null, 4),
		'utf-8'
	);

	const diffCount = comparaisonResult.filter(e => e.has_content_diff).length;
	console.log(` -> Comparaison terminée : ${manquants.length} fichiers manquants, ${diffCount} fichiers avec du contenu différent.`);
	console.log("=== FIN DE L'INDEXATION COMPLÈTE ===\n");
}

// Exécution
runComparaison();