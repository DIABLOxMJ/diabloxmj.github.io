const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const { exec } = require('child_process');

// Importation directe des scripts JS ! Plus besoin de lancer des processus "py" !
const { indexBlocks } = require('./Script_Block_Indexer');
const { indexItems } = require('./Script_Item_Indexer');
const { extractSounds } = require('./Script_Sound_Extractor');
const { indexCategories } = require('./Script_Category_Indexer');
const { runExtraction } = require('./Script_Extractor');
const { runComparaison } = require('./Script_Comparaison');

const app = express();
app.use(cors());
app.use(express.json());

const BASE_DIR = path.dirname(__dirname);
const OUTPUT_DIR = path.join(BASE_DIR, "IndexData");

// Servant les fichiers statiques de IndexData
app.use('/IndexData', express.static(OUTPUT_DIR));

// Exécution directe des fonctions de scripts
app.post('/run/:scriptType', (req, res) => {
	const { scriptType } = req.params;
	try {
		if (scriptType === 'blocks') indexBlocks();
		else if (scriptType === 'items') indexItems();
		else if (scriptType === 'sounds') extractSounds(req.body?.version);
		else if (scriptType === 'categories') indexCategories();
		else if (scriptType === 'comparaison') runComparaison();
		else if (scriptType === 'extract') runExtraction();
		else return res.status(400).json({ status: "error", message: "Script inconnu" });

		res.json({ status: "success", message: `Script ${scriptType} exécuté avec succès !` });
	} catch (e) {
		res.status(500).json({ status: "error", message: e.message });
	}
});

// Routes de lecture JSON
app.get('/get-index/:indexType', (req, res) => {
	const files = { blocks: 'Index_Details_Blocks.json', items: 'Index_Details_Items.json' };
	const file = files[req.params.indexType];
	if (!file) return res.status(400).json({ error: "Index inconnu" });

	const filePath = path.join(OUTPUT_DIR, file);
	if (!fs.existsSync(filePath)) return res.json([]);
	res.json(JSON.parse(fs.readFileSync(filePath, 'utf-8')));
});

app.get('/get-categories', (req, res) => {
	const filePath = path.join(OUTPUT_DIR, 'Index_Details_Category.json');
	if (!fs.existsSync(filePath)) return res.json({});
	res.json(JSON.parse(fs.readFileSync(filePath, 'utf-8')));
});

app.get('/get-Comparaison', (req, res) => {
	const filePath = path.join(OUTPUT_DIR, 'Index_Comparaison_Missing.json');
	if (!fs.existsSync(filePath)) return res.json([]);
	res.json(JSON.parse(fs.readFileSync(filePath, 'utf-8')));
});

// Interactions système OS (Ouvrir fichier/dossier)
app.post('/open-file', (req, res) => {
	const relPath = req.body.path;
	if (!relPath) return res.status(400).json({ status: "error" });
	const fullPath = path.normalize(path.join(BASE_DIR, relPath));

	const cmd = process.platform === 'win32' ? `start "" "${fullPath}"` :
				process.platform === 'darwin' ? `open "${fullPath}"` : `xdg-open "${fullPath}"`;
	exec(cmd, (err) => err ? res.status(500).json({ message: err.message }) : res.json({ status: "success" }));
});

app.post('/open-folder', (req, res) => {
	const relPath = req.body.path;
	if (!relPath) return res.status(400).json({ status: "error" });
	const fullPath = path.normalize(path.join(BASE_DIR, relPath));

	const cmd = process.platform === 'win32' ? `explorer.exe /select,"${fullPath}"` : `open "${path.dirname(fullPath)}"`;
	exec(cmd, (err) => err ? res.status(500).json({ message: err.message }) : res.json({ status: "success" }));
});

app.post('/read-file', (req, res) => {
	const relPath = req.body.path;
	if (!relPath) return res.status(400).json({ status: "error" });
	const fullPath = path.normalize(path.join(BASE_DIR, relPath));

	if (!fs.existsSync(fullPath)) return res.status(404).json({ status: "error" });
	res.json({ status: "success", content: fs.readFileSync(fullPath, 'utf-8') });
});

// Extraction unitaire et par lot
app.post('/extract-single-file', (req, res) => {
	const relPath = req.body.path;
	if (!relPath) return res.status(400).json({ status: "error" });

	const cleanPath = relPath.replace("Pack (Next)/assets/", "").replace("Pack (Main)/assets/", "");
	const srcFile = path.normalize(path.join(BASE_DIR, "Pack (Next)", "assets", cleanPath));
	const destFile = path.normalize(path.join(BASE_DIR, "Extract", "assets", cleanPath));

	if (!fs.existsSync(srcFile)) return res.status(404).json({ status: "error" });

	fs.mkdirSync(path.dirname(destFile), { recursive: true });
	fs.copyFileSync(srcFile, destFile);
	res.json({ status: "success" });
});

app.post('/extract-batch-files', (req, res) => {
	const paths = req.body.paths || [];
	let extractedCount = 0;

	for (const relPath of paths) {
		const cleanPath = relPath.replace("Pack (Next)/assets/", "").replace("Pack (Main)/assets/", "");
		const srcFile = path.normalize(path.join(BASE_DIR, "Pack (Next)", "assets", cleanPath));
		const destFile = path.normalize(path.join(BASE_DIR, "Extract", "assets", cleanPath));

		if (fs.existsSync(srcFile)) {
			fs.mkdirSync(path.dirname(destFile), { recursive: true });
			fs.copyFileSync(srcFile, destFile);
			extractedCount++;
		}
	}
	res.json({ status: "success", extracted_count: extractedCount });
});

app.listen(5000, () => console.log('Serveur Node.js à l’écoute sur http://localhost:5000'));