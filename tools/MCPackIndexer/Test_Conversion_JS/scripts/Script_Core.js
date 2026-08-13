const express = require('express'); //[cite: 37] Importe le framework web Express pour instancier un serveur HTTP.
const cors = require('cors'); //[cite: 37] Importe le middleware CORS pour autoriser les requêtes cross-origin (depuis un navigateur ou une UI externe).
const fs = require('fs'); //[cite: 37] Importe le module 'fs' pour interagir avec le système de fichiers.
const path = require('path'); //[cite: 37] Importe 'path' pour construire et normaliser les chemins.
const { exec } = require('child_process'); //[cite: 37] Extrait la fonction 'exec' permettant d'exécuter des commandes système shell.

let config = require('./Script_Config'); //[cite: 37] Charge la configuration. Utilise 'let' pour autoriser la réassignation lors du rechargement à chaud.

const { indexBlocks } = require('./Script_Block_Indexer'); //[cite: 37] Extrait la fonction 'indexBlocks' du module d'indexation des blocs.
const { indexItems } = require('./Script_Item_Indexer'); //[cite: 37] Extrait la fonction 'indexItems' du module d'indexation des objets.
const { extractSounds } = require('./Script_Sound_Extractor'); //[cite: 37] Extrait la fonction 'extractSounds' du module d'extraction audio.
const { indexCategories } = require('./Script_Category_Indexer'); //[cite: 37] Extrait la fonction 'indexCategories' du module d'indexation par dossiers.
const { runExtraction } = require('./Script_Extractor'); //[cite: 37] Extrait la fonction 'runExtraction' du module de copie des éléments manquants.
const { runComparaison } = require('./Script_Comparaison'); //[cite: 37] Extrait la fonction 'runComparaison' du module d'analyse différentielle.

const app = express(); //[cite: 37] Instancie l'application Express.
app.use(cors()); //[cite: 37] Applique le middleware CORS à l'ensemble des routes HTTP du serveur.
app.use(express.json()); //[cite: 37] Active le décodage automatique des corps de requêtes entrantes au format JSON (accessible via req.body).

// Accès statique aux fichiers d'indexation générés[cite: 37]
app.use('/IndexData', (req, res, next) => { //[cite: 37] Intercepte les requêtes envoyées sur le préfixe d'URL '/IndexData'.
	express.static(config.PATHS.IndexData)(req, res, next); //[cite: 37] Sert statiquement les fichiers contenus dans le dossier IndexData configuré.
});

app.post('/reload-config', (req, res) => { //[cite: 37] Déclare une route POST '/reload-config' pour recharger la configuration JSON sans redémarrer le serveur.
	try {
		config = config.reload(); //[cite: 37] Réexécute le chargement du fichier JSON et réassigne la variable 'config'.
		console.log('🔄 Configuration JSON rechargée avec succès !'); //[cite: 37] Journalise la réussite dans la console serveur.
		res.json({ status: "success", message: "Configuration rechargée !" }); //[cite: 37] Envoie une réponse JSON de confirmation au client.
	} catch (e) {
		res.status(500).json({ status: "error", message: e.message }); //[cite: 37] Renvoie un code d'erreur HTTP 500 avec le message d'erreur en cas d'échec.
	}
});

app.get('/get-config', (req, res) => { //[cite: 37] Déclare une route GET '/get-config' restituant la configuration courante.
	res.json({ //[cite: 37] Renvoie un objet JSON structuré au client web.
		port: config.PORT,
		paths: config.PATHS,
		files: config.FILES,
		ui: config.UI
	});
});

app.post('/run/:scriptType', (req, res) => { //[cite: 37] Route POST dynamique accepting un paramètre ':scriptType' dans l'URL.
	const { scriptType } = req.params; //[cite: 37] Extrait la valeur du paramètre 'scriptType' via déstructuration d'objet.
	try {
		if (scriptType === 'blocks') indexBlocks(); //[cite: 37] Si le paramètre vaut 'blocks', exécute l'indexer de blocs.
		else if (scriptType === 'items') indexItems(); //[cite: 37] Si 'items', exécute l'indexer d'items.
		else if (scriptType === 'sounds') extractSounds(req.body?.version); //[cite: 37] Si 'sounds', exécute l'extracteur audio en lui passant la version du body si renseignée.
		else if (scriptType === 'categories') indexCategories(); //[cite: 37] Si 'categories', exécute l'indexation par catégories.
		else if (scriptType === 'comparaison') runComparaison(); //[cite: 37] Si 'comparaison', lance l'analyse MD5.
		else if (scriptType === 'extract') runExtraction(); //[cite: 37] Si 'extract', lance l'extraction physique des fichiers manquants.
		else return res.status(400).json({ status: "error", message: "Script inconnu" }); //[cite: 37] Si la valeur ne correspond à aucun script, renvoie une erreur HTTP 400 (Bad Request).

		res.json({ status: "success", message: `Script ${scriptType} exécuté avec succès !` }); //[cite: 37] Confirme la fin de l'exécution au client.
	} catch (e) {
		res.status(500).json({ status: "error", message: e.message }); //[cite: 37] Capture et renvoie toute erreur survenue durant l'exécution.
	}
});

app.get('/get-Comparaison', (req, res) => { //[cite: 37] Route GET '/get-Comparaison' permettant de récupérer le dernier rapport d'analyse.
	const filePath = path.join(config.PATHS.IndexData, config.FILES.COMPARISON_REPORT); //[cite: 37] Génère le chemin absolu du fichier rapport JSON.
	if (!fs.existsSync(filePath)) return res.json([]); //[cite: 37] Si le fichier n'a pas encore été généré, retourne un tableau vide.
	res.json(JSON.parse(fs.readFileSync(filePath, 'utf-8'))); //[cite: 37] Lit le fichier, parse le contenu JSON et l'envoie au client.
});

app.listen(config.PORT, () => console.log(`Serveur Node.js à l'écoute sur http://localhost:${config.PORT}`)); //[cite: 37] Lance l'écoute HTTP sur le port configuré et affiche l'URL de démarrage.