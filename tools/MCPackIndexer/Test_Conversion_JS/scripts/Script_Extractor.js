const fs = require('fs'); //[cite: 38] Importe le module système de fichiers.
const path = require('path'); //[cite: 38] Importe le module de gestion des chemins.
const config = require('./Script_Config'); //[cite: 38] Charge l'objet de configuration globale.

const VANILLA_DIR = config.PATHS.Comparaison.PACK_NEXT; //[cite: 38] Extrait le dossier source des ressources d'origine (Vanilla).
const EXTRACT_DIR = config.PATHS.Extract.EXTRACT_DIR; //[cite: 38] Extrait le dossier de destination pour les fichiers à extraire.
const REPORT_PATH = path.join(config.PATHS.IndexData, config.FILES.COMPARISON_REPORT || "Index_Comparaison_Missing.json"); //[cite: 38] Construit le chemin absolu du rapport de comparaison.

function runExtraction() { //[cite: 38] Fonction principale responsable de la copie des éléments absents du pack utilisateur.
	console.log("\n=== DÉBUT DE L'EXTRACTION DES FICHIERS MANQUANTS ==="); //[cite: 38] Affiche un séparateur dans les logs.

	if (!fs.existsSync(REPORT_PATH)) { //[cite: 38] Vérifie la présence du rapport JSON préalable.
		console.log(" [ERREUR] Aucun rapport de comparaison trouvé."); //[cite: 38] Alerte dans la console si le rapport n'a pas été généré au préalable.
		return; //[cite: 38] Stoppe l'exécution de la fonction.
	}

	const comparaisonData = JSON.parse(fs.readFileSync(REPORT_PATH, 'utf-8')); //[cite: 38] Lit et décode le rapport de comparaison.
	const missingFiles = comparaisonData.filter(item => item.status === 'missing').map(item => item.path); //[cite: 38] Filtre les objets dont le statut est 'missing' et extrait leurs chemins relatifs.

	if (missingFiles.length === 0) { //[cite: 38] Vérifie s'il y a au moins un fichier à traiter.
		console.log(" -> Aucun fichier manquant à extraire !"); //[cite: 38] Informe que tout est déjà présent.
		return; //[cite: 38] Stoppe l'exécution.
	}

	const parentExtractDir = path.dirname(EXTRACT_DIR); //[cite: 38] Détermine le dossier parent du répertoire d'extraction.
	if (fs.existsSync(parentExtractDir)) { //[cite: 38] Si le dossier d'extraction existe déjà...
		try {
			fs.rmSync(parentExtractDir, { recursive: true, force: true }); //[cite: 38] Efface récursivement l'ancien dossier d'extraction pour repartir d'un état propre.
		} catch (e) {
			console.log(` [AVERTISSEMENT] Erreur réinitialisation Extract : ${e.message}`); //[cite: 38] Journalise l'avertissement en cas de problème de droits de suppression.
		}
	}

	fs.mkdirSync(EXTRACT_DIR, { recursive: true }); //[cite: 38] Recrée le dossier d'extraction cible (et ses sous-dossiers).
	let compteur = 0; //[cite: 38] Compteur pour les réussites de copie.
	let erreurs = 0; //[cite: 38] Compteur pour les erreurs de copie.

	for (const relPath of missingFiles) { //[cite: 38] Boucle sur chaque chemin relatif de fichier manquant.
		const srcFile = path.normalize(path.join(VANILLA_DIR, relPath)); //[cite: 38] Reconstruit et normalise le chemin absolu du fichier source.
		const destFile = path.normalize(path.join(EXTRACT_DIR, relPath)); //[cite: 38] Reconstruit et normalise le chemin absolu du fichier destination.

		if (fs.existsSync(srcFile)) { //[cite: 38] S'assure que le fichier existe physiquement dans le dossier source.
			fs.mkdirSync(path.dirname(destFile), { recursive: true }); //[cite: 38] Crée l'arborescence de sous-dossiers intermédiaire dans le répertoire de destination.
			fs.copyFileSync(srcFile, destFile); //[cite: 38] Duplique le fichier vers la destination.
			compteur++; //[cite: 38] Incrémente le total des copies réussies.
		} else {
			erreurs++; //[cite: 38] Incrémente le total des fichiers introuvables.
		}
	}

	console.log(`=== EXTRACTION TERMINÉE : ${compteur} fichiers copiés (${erreurs} introuvables) ===\n`); //[cite: 38] Affiche le bilan de l'opération.
}

if (require.main === module) runExtraction(); //[cite: 38] Exécute runExtraction() uniquement si le fichier est exécuté directement en CLI (`node Script_Extractor.js`).
module.exports = { runExtraction }; //[cite: 38] Exporte la fonction pour qu'elle puisse être invoquée par le serveur Express.