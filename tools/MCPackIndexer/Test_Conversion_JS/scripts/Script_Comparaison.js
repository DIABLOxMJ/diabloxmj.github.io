const fs = require('fs'); //[cite: 40] Importe le module système de fichiers.
const path = require('path'); //[cite: 40] Importe le module de gestion des chemins.
const crypto = require('crypto'); //[cite: 40] Importe le module de cryptographie natif (pour générer des sommes de contrôle MD5).
const config = require('./Script_Config'); //[cite: 40] Importe le module de configuration.

const NEXT_DIR = config.PATHS.Comparaison.PACK_NEXT; //[cite: 40] Chemin absolu du pack Vanilla/Next.
const MAIN_DIR = config.PATHS.Comparaison.PACK_MAIN; //[cite: 40] Chemin absolu du pack Utilisateur/Main.
const OUTPUT_DIR = config.PATHS.IndexData; //[cite: 40] Chemin d'exportation des données d'index.

function scanAssetsFolder(targetDir) { //[cite: 40] Fonction effectuant un parcours récursif pour lister tous les fichiers d'un dossier.
	let fileList = []; //[cite: 40] Tableau accumulant les chemins relatifs découverts.
	if (!fs.existsSync(targetDir)) return fileList; //[cite: 40] Si le dossier n'existe pas, retourne immédiatement un tableau vide.

	function walkSync(currentDir) { //[cite: 40] Fonction interne récursive de balayage.
		const entries = fs.readdirSync(currentDir, { withFileTypes: true }); //[cite: 40] Lit le contenu du dossier avec les types d'entrées (fichiers/dossiers).
		for (const entry of entries) { //[cite: 40] Parcourt chaque entrée du dossier courant.
			const fullPath = path.join(currentDir, entry.name); //[cite: 40] Forge le chemin absolu de l'élément.
			if (entry.isDirectory()) {
				walkSync(fullPath); //[cite: 40] Si c'est un répertoire, relance walkSync sur ce sous-dossier.
			} else if (entry.isFile()) {
				const relPath = path.relative(targetDir, fullPath).replace(/\\/g, "/"); //[cite: 40] Calcule le chemin relatif à la racine du scan et remplace les antislashs Windows par des slashs.
				fileList.push(relPath); //[cite: 40] Ajoute le chemin relatif standardisé au tableau.
			}
		}
	}

	walkSync(targetDir); //[cite: 40] Invoque la première itération du balayage.
	return fileList.sort(); //[cite: 40] Trie le tableau de chemins par ordre alphabétique et le renvoie.
}

function sortObjectKeys(obj) { //[cite: 40] Fonction récursive pour trier alphabétiquement les clés d'un objet JSON.
	if (obj === null || typeof obj !== 'object') return obj; //[cite: 40] Si ce n'est pas un objet ou si c'est null, retourne la valeur telle quelle.
	if (Array.isArray(obj)) return obj.map(sortObjectKeys); //[cite: 40] Si c'est un tableau, applique la fonction à chaque élément.
	return Object.keys(obj)
		.sort() //[cite: 40] Trie les clés de l'objet par ordre alphabétique.
		.reduce((acc, key) => { //[cite: 40] Reconstruit un objet avec les clés triées.
			acc[key] = sortObjectKeys(obj[key]); //[cite: 40] Traite récursivement la valeur associée à la clé.
			return acc;
		}, {});
}

function computeFileHash(filePath) { //[cite: 40] Calcule le hash MD5 d'un fichier pour vérifier son identité de contenu.
	if (!fs.existsSync(filePath)) return null; //[cite: 40] Retourne null si le fichier n'existe pas.

	try {
		if (filePath.endsWith('.json') || filePath.endsWith('.mcmeta')) { //[cite: 40] Traitement spécifique pour les fichiers JSON / MCMETA.
			const rawText = fs.readFileSync(filePath, 'utf-8'); //[cite: 40] Lit le contenu textuel.
			const data = JSON.parse(rawText); //[cite: 40] Parse le texte en objet JS.
			const sortedData = sortObjectKeys(data); //[cite: 40] Trie les clés pour neutraliser l'impact de la mise en forme ou du réordonnancement des clés.
			const cleanStr = JSON.stringify(sortedData); //[cite: 40] Ré-encode en chaîne JSON compacte sans espaces superflus.
			return crypto.createHash('md5').update(cleanStr, 'utf-8').digest('hex'); //[cite: 40] Génère et retourne le hash MD5 sous forme de chaîne hexadécimale.
		} else {
			const fileBuffer = fs.readFileSync(filePath); //[cite: 40] Pour les fichiers binaires (images PNG, sons OGG), lit directement le buffer.
			return crypto.createHash('md5').update(fileBuffer).digest('hex'); //[cite: 40] Calcule et retourne le hash MD5 du buffer binaire.
		}
	} catch (err) {
		try {
			const fileBuffer = fs.readFileSync(filePath); //[cite: 40] Secours : si le JSON est invalide, lit le fichier sous forme de buffer brut.
			return crypto.createHash('md5').update(fileBuffer).digest('hex');
		} catch (fallbackErr) {
			return null; //[cite: 40] Renvoie null en cas d'erreur de lecture.
		}
	}
}

function runComparaison() { //[cite: 40] Fonction principale effectuant l'indexation complète et la comparaison des deux packs.
	console.log("\n=== DÉBUT DE L'INDEXATION COMPLÈTE & COMPARAISON ==="); //[cite: 40] Affichage dans les logs console.
	fs.mkdirSync(OUTPUT_DIR, { recursive: true }); //[cite: 40] S'assure de la présence du dossier de sortie des données d'index.

	console.log(`[SCAN] Analyse du dossier Vanilla : ${NEXT_DIR}...`); //[cite: 40] Log de progression.
	const nextFiles = scanAssetsFolder(NEXT_DIR); //[cite: 40] Scanne et récupère tous les chemins du pack Vanilla.
	fs.writeFileSync(path.join(OUTPUT_DIR, "Index_NextPack.json"), JSON.stringify(nextFiles, null, 4), 'utf-8'); //[cite: 40] Écrit la liste brute au format JSON.

	console.log(`[SCAN] Analyse du dossier personnel (Pack) : ${MAIN_DIR}...`); //[cite: 40] Log de progression.
	const mainFiles = scanAssetsFolder(MAIN_DIR); //[cite: 40] Scanne et récupère tous les chemins du pack Utilisateur.
	fs.writeFileSync(path.join(OUTPUT_DIR, "Index_OldPack.json"), JSON.stringify(mainFiles, null, 4), 'utf-8'); //[cite: 40] Écrit la liste brute au format JSON.

	const nextSet = new Set(nextFiles); //[cite: 40] Convertit le tableau en ensemble Set pour accélérer la recherche.
	const mainSet = new Set(mainFiles); //[cite: 40] Convertit le tableau en Set.
	const manquants = Array.from(nextSet).filter(file => !mainSet.has(file)).sort(); //[cite: 40] Extrait les fichiers présents dans Next mais absents de Main.

	const comparaisonResult = []; //[cite: 40] Initialise le tableau récapitulatif du rapport.
	for (const relPath of nextFiles) { //[cite: 40] Boucle sur chaque fichier répertorié dans le pack Vanilla.
		const parts = relPath.split('/'); //[cite: 40] Découpe le chemin relatif.
		const category = parts.length > 1 ? parts[1] : "root"; //[cite: 40] Identifie la sous-catégorie principale (ex: 'textures', 'models').
		const isMissing = manquants.includes(relPath); //[cite: 40] Détermine si le fichier est manquant dans le pack utilisateur.
		let hasContentDiff = false; //[cite: 40] Indicateur de différence de contenu.

		if (!isMissing) { //[cite: 40] Si le fichier est présent dans les deux packs...
			const pathMain = path.join(MAIN_DIR, relPath); //[cite: 40] Chemin absolu dans le pack Main.
			const pathNext = path.join(NEXT_DIR, relPath); //[cite: 40] Chemin absolu dans le pack Next.

			const hashMain = computeFileHash(pathMain); //[cite: 40] Empreinte MD5 du fichier dans Main.
			const hashNext = computeFileHash(pathNext); //[cite: 40] Empreinte MD5 du fichier dans Next.

			if (hashMain && hashNext && hashMain !== hashNext) { //[cite: 40] Si les deux empreintes diffèrent...
				hasContentDiff = true; //[cite: 40] Marque le fichier comme ayant du contenu modifié.
			}
		}

		comparaisonResult.push({ //[cite: 40] Ajoute la fiche détaillée du fichier au rapport.
			path: relPath,
			filename: parts[parts.length - 1],
			category: category,
			status: isMissing ? "missing" : "present",
			has_content_diff: hasContentDiff
		});
	}

	const outputFile = path.join(OUTPUT_DIR, config.FILES.COMPARISON_REPORT || "Index_Comparaison_Missing.json"); //[cite: 40] Chemin complet du rapport JSON final.
	fs.writeFileSync(outputFile, JSON.stringify(comparaisonResult, null, 4), 'utf-8'); //[cite: 40] Écrit le fichier rapport sur le disque.
	console.log("=== FIN DE L'INDEXATION COMPLÈTE ===\n"); //[cite: 40] Log de fin d'opération.
}

if (require.main === module) { //[cite: 40] Exécute l'analyse uniquement si le script est lancé de manière autonome en ligne de commande.
	runComparaison();
}

module.exports = { runComparaison }; //[cite: 40] Exporte la fonction pour permettre l'appel à la demande via l'API Express.