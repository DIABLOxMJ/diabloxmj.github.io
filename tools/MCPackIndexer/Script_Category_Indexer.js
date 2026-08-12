const fs = require('fs');
const path = require('path');

const BASE_DIR_SCRIPT = __dirname;
const BASE_DIR = path.dirname(BASE_DIR_SCRIPT);
const TEXTURES_DIR = path.join(BASE_DIR, "Pack (Main)", "assets", "minecraft", "textures");
const OUTPUT_DIR = path.join(BASE_DIR, "IndexData");

function titleCase(str) {
	return str.replace(/_/g, ' ').replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());
}

function indexCategories() {
	console.log("\n=== DÉBUT DE L'INDEXATION DES CATÉGORIES (PAR DOSSIER) ===");

	if (!fs.existsSync(TEXTURES_DIR)) {
		console.log(`[ERROR] Le dossier ${TEXTURES_DIR} n'existe pas !`);
		return;
	}

	fs.mkdirSync(OUTPUT_DIR, { recursive: true });
	const categoriesData = {};
	const ignoredFolders = ["block", "item"];

	const entries = fs.readdirSync(TEXTURES_DIR, { withFileTypes: true });

	for (const entry of entries) {
		if (entry.isDirectory() && !ignoredFolders.includes(entry.name)) {
			console.log(`[SCAN] Racine détectée : ${entry.name}`);
			const entryPath = path.join(TEXTURES_DIR, entry.name);
			const folderGroups = {};

			function walkSync(currentDir) {
				const files = fs.readdirSync(currentDir, { withFileTypes: true });
				const pngFiles = files.filter(f => f.isFile() && f.name.endsWith(".png"));

				if (pngFiles.length > 0) {
					const relFolderPath = path.relative(TEXTURES_DIR, currentDir).replace(/\\/g, "/");
					const displayName = titleCase(relFolderPath);
					const folderId = relFolderPath.toLowerCase().replace(/\//g, "-");

					if (!folderGroups[folderId]) {
						folderGroups[folderId] = {
							id: folderId,
							buttonName: displayName,
							folder_path: path.relative(BASE_DIR, currentDir).replace(/\\/g, "/"),
							textures: []
						};
					}

					for (const file of pngFiles) {
						const fullFilePath = path.join(currentDir, file.name);
						const relFilePath = path.relative(BASE_DIR, fullFilePath).replace(/\\/g, "/");

						folderGroups[folderId].textures.push({
							name: file.name.replace(".png", ""),
							filename: file.name,
							path: relFilePath
						});
					}
				}

				for (const subItem of files) {
					if (subItem.isDirectory()) {
						walkSync(path.join(currentDir, subItem.name));
					}
				}
			}

			walkSync(entryPath);
			categoriesData[entry.name] = Object.values(folderGroups);
		}
	}

	const outputPath = path.join(OUTPUT_DIR, "Index_Details_Category.json");
	fs.writeFileSync(outputPath, JSON.stringify(categoriesData, null, 4), 'utf-8');
	console.log("=== FIN DE L'INDEXATION : Groupement par dossier réussi ===\n");
}

if (require.main === module) indexCategories();
module.exports = { indexCategories };