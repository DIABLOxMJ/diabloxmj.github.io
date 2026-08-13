const fs = require('fs'); // Importe le module natif 'fs' pour gérer la lecture et la création de fichiers/dossiers.
const path = require('path'); // Importe le module 'path' pour construire et formater des chemins de fichiers.
const config = require('./Script_Config'); // Importe la configuration globale de l'application.

const PACK_DIR = config.PATHS.Indexer.PACK_NEXT; // Extrait le chemin du dossier racine contenant les assets Minecraft à analyser.
const PACK_DIR_SOURCE = config.PATHS.Indexer.PACK_MAIN; // Extrait le chemin source du pack principal pour les liens de sortie.
const OUTPUT_DIR = config.PATHS.IndexData; // Extrait le dossier de destination pour enregistrer le fichier JSON final.

const BLOCKSTATES_DIR = path.join(PACK_DIR, "blockstates"); // Forge le chemin vers le dossier contenant les fichiers blockstates.
const MODELS_DIR = path.join(PACK_DIR, "models"); // Forge le chemin vers le dossier contenant les modèles 3D.

/**
 * Convertit un identifiant (ex: "stone_bricks") en titre lisible (ex: "Stone Bricks")
 */
function titleCase(str) { // Déclarateur de la fonction de formatage des noms de boutons.
	return str.replace(/_/g, ' ').replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase()); // Remplace les tirets bas par des espaces et met une majuscule à chaque mot.
}

/**
 * Cherche de manière récursive toutes les textures associées à un modèle (en gérant l'héritage 'parent')
 */
function findTexturesInModel(modelName) { // Déclaration de la fonction de recherche de textures.
	const cleanName = modelName.includes(":") ? modelName.split(":").pop() : modelName; // Supprime le namespace (ex: "minecraft:") du nom de modèle.
	const fullPath = cleanName.endsWith(".json") ? cleanName : path.join(MODELS_DIR, `${cleanName}.json`); // Construit le chemin absolu vers le fichier JSON du modèle.

	if (!fs.existsSync(fullPath)) return {}; // Si le fichier de modèle n'existe pas, renvoie un dictionnaire vide.

	try {
		const data = JSON.parse(fs.readFileSync(fullPath, 'utf-8')); // Lit et parse le fichier JSON du modèle.
		let textures = {}; // Initialise un dictionnaire pour stocker les clés et chemins de textures.

		if (data.textures) Object.assign(textures, data.textures); // Si le modèle possède une section "textures", la fusionne dans l'objet.

		if (data.parent) { // Si le modèle hérite d'un modèle parent...
			const parentTextures = findTexturesInModel(data.parent); // Appel récursif pour récupérer les textures du parent.
			for (const [k, v] of Object.entries(parentTextures)) { // Parcourt les textures héritées.
				if (!(k in textures)) textures[k] = v; // Conserve la texture parent uniquement si elle n'a pas été surchargée par l'enfant.
			}
		}
		return textures; // Renvoie l'ensemble des textures trouvées.
	} catch {
		return {}; // Retourne un objet vide en cas d'erreur de lecture ou de parsing JSON.
	}
}

/**
 * Extrait la liste des identifiants de modèles depuis un bloc de variante (objet ou tableau)
 */
function extractModelsFromVariant(variantData) { // Déclarateur de la fonction d'extraction.
	const models = []; // Initialise le tableau de résultats.
	if (Array.isArray(variantData)) { // Si la variante est une liste d'options (ex: multiples rotations)...
		for (const item of variantData) { // Parcourt chaque élément du tableau.
			if (typeof item === 'object' && item !== null && item.model) { // Vérifie qu'il s'agit d'un objet possédant la propriété "model".
				models.push(item.model); // Ajoute le nom du modèle à la liste.
			}
		}
	} else if (typeof variantData === 'object' && variantData !== null && variantData.model) { // Si c'est un objet simple unique...
		models.push(variantData.model); // Ajoute le nom du modèle à la liste.
	}
	return models; // Renvoie la liste des chemins/noms de modèles collectés.
}

/**
 * Fonction principale d'indexation des blockstates et de leurs dépendances
 */
function indexBlocks() { // Déclarateur de la fonction principale exportée.
	console.log("\n=== DÉBUT DE L'INDEXATION DES BLOCS ==="); // Affiche l'entête de démarrage dans les logs.

	if (!fs.existsSync(BLOCKSTATES_DIR)) { // Vérifie si le dossier 'blockstates' existe dans les assets.
		console.log(`[ERROR] Le dossier ${BLOCKSTATES_DIR} n'existe pas !`); // Affiche un message d'erreur explicatif.
		return; // Stoppe l'exécution si le dossier n'existe pas.
	}

	fs.mkdirSync(OUTPUT_DIR, { recursive: true }); // Crée le dossier d'index s'il n'existe pas déjà sur le disque.
	const indexResult = []; // Initialise le tableau devant contenir le résultat final d'indexation.

	const files = fs.readdirSync(BLOCKSTATES_DIR).filter(f => f.endsWith('.json')); // Récupère la liste de tous les fichiers JSON du dossier blockstates.

	for (const filename of files) { // Parcourt chaque fichier blockstate JSON.
		const blockId = filename.replace(".json", ""); // Extrait l'identifiant du bloc en retirant l'extension.
		const blockstatePath = path.join(BLOCKSTATES_DIR, filename); // Forge le chemin absolu du fichier blockstate.

		try {
			const data = JSON.parse(fs.readFileSync(blockstatePath, 'utf-8')); // Lit et parse le fichier blockstate.
			const rawModels = []; // Tableau temporaire pour accumuler les références de modèles.

			if (data.variants && typeof data.variants === 'object') { // Traite la section "variants" si elle existe...
				for (const variantContent of Object.values(data.variants)) { // Parcourt toutes les variantes définies.
					rawModels.push(...extractModelsFromVariant(variantContent)); // Extrait et accumule les modèles associés.
				}
			}

			if (data.multipart && Array.isArray(data.multipart)) { // Traite la section "multipart" si elle existe...
				for (const part of data.multipart) { // Parcourt chaque condition/partie multipart.
					if (typeof part === 'object' && part !== null && part.apply) { // S'assure que la clé "apply" existe.
						rawModels.push(...extractModelsFromVariant(part.apply)); // Extrait et accumule les modèles associés.
					}
				}
			}

			const uniqueModels = [...new Set(rawModels)]; // Élimine les modèles doublons en conservant un tableau unique.
			const combinedTextures = {}; // Objet qui contiendra la fusion de toutes les textures du bloc.
			const modelPaths = []; // Tableau qui retiendra les chemins d'accès vers les fichiers modèles JSON.

			for (const mRef of uniqueModels) { // Parcourt chaque modèle unique référencé.
				const cleanM = mRef.includes(":") ? mRef.split(":").pop() : mRef; // Supprime le préfixe de namespace s'il existe.
				modelPaths.push(`${PACK_DIR_SOURCE}/assets/minecraft/models/${cleanM}.json`); // Forge le chemin relatif standardisé du modèle.

				const texFound = findTexturesInModel(mRef); // Recherche l'arborescence complète de textures de ce modèle.
				Object.assign(combinedTextures, texFound); // Fusionne les textures trouvées dans l'objet principal.
			}

			const cleanTextures = {}; // Objet final recevant les chemins de textures nettoyés.
			for (let [key, val] of Object.entries(combinedTextures)) { // Parcourt chaque texture identifiée.
				if (typeof val === 'object' && val !== null) { // Si la valeur est un sous-objet (ex: textures animées ou complexes)...
					val = val.texture || val.image || ""; // Extrait la propriété sous forme de chaîne de caractères.
				}

				if (typeof val === 'string' && val) { // S'assure que la valeur est une chaîne non vide.
					if (val.startsWith("#")) { // Si la valeur est une variable/alias de texture (ex: "#all")...
						cleanTextures[key] = val; // Conserve la variable telle quelle.
					} else {
						const cleanPath = val.includes(":") ? val.split(":").pop() : val; // Supprime le namespace si présent.
						cleanTextures[key] = `${PACK_DIR_SOURCE}/assets/minecraft/textures/${cleanPath}.png`; // Reconstruit le chemin vers le fichier PNG.
					}
				}
			}

			const blockEntry = { // Construit l'objet d'indexation complet pour le bloc courant.
				id: blockId, // Identifiant du bloc (ex: "oak_log").
				buttonName: titleCase(blockId), // Nom d'affichage mis en forme (ex: "Oak Log").
				blockstate: `${PACK_DIR_SOURCE}/assets/minecraft/blockstates/${filename}`, // Chemin du fichier blockstate source.
				models: modelPaths, // Liste des chemins vers les modèles JSON.
				textures: cleanTextures // Dictionnaire des clés/chemins de textures PNG associées.
			};

			indexResult.push(blockEntry); // Ajoute la fiche du bloc à la liste globale des résultats.
		} catch (e) {
			console.log(`[ERROR] ${filename}: ${e.message}`); // Journalise une erreur si un fichier blockstate échoue lors du parsing.
		}
	}

	const outputFile = path.join(OUTPUT_DIR, config.FILES.BLOCKS_INDEX || "Index_Details_Blocks.json"); // Forge le chemin absolu du fichier rapport final.
	fs.writeFileSync(outputFile, JSON.stringify(indexResult, null, 4), 'utf-8'); // Écrit le tableau d'indexation au format JSON indenté sur le disque.
	console.log(`=== FIN DE L'INDEXATION : ${indexResult.length} blocs enregistrés ===\n`); // Affiche le bilan d'indexation dans la console.
}

if (require.main === module) indexBlocks(); // Permet l'exécution directe du script via la commande `node Script_Block_Indexer.js`.
module.exports = { indexBlocks }; // Exporte la fonction `indexBlocks` pour pouvoir être appelée depuis `Script_Core.js`.