const fs = require('fs');
const path = require('path');
const config = require('./Script_Config');

const PACK_DIR = config.PATHS.Indexer.PACK_NEXT;
const PACK_DIR_SOURCE = config.PATHS.Indexer.PACK_MAIN;
const OUTPUT_DIR = config.PATHS.IndexData;

const ITEMS_DEF_DIR = path.join(PACK_DIR, "items");
const ALL_MODELS_DIR = path.join(PACK_DIR, "models");

function titleCase(str) {
	return str.replace(/_/g, ' ').replace(/\w\S*/g, (txt) => txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase());
}

function findTexturesInItemModel(modelPathOrName) {
	const cleanName = modelPathOrName.includes(":") ? modelPathOrName.split(":").pop() : modelPathOrName;
	const fullPath = cleanName.endsWith(".json") ? cleanName : path.join(ALL_MODELS_DIR, `${cleanName}.json`);

	if (!fs.existsSync(fullPath)) return {};

	try {
		const data = JSON.parse(fs.readFileSync(fullPath, 'utf-8'));
		let textures = {};

		if (data.textures) Object.assign(textures, data.textures);

		if (data.parent) {
			const parentTextures = findTexturesInItemModel(data.parent);
			for (const [k, v] of Object.entries(parentTextures)) {
				if (!(k in textures)) textures[k] = v;
			}
		}
		return textures;
	} catch {
		return {};
	}
}

function extractModelsFromItemDef(data) {
	const models = [];
	function searchModel(obj) {
		if (typeof obj === 'object' && obj !== null) {
			if (typeof obj.model === 'string') models.push(obj.model);
			for (const v of Object.values(obj)) searchModel(v);
		} else if (Array.isArray(obj)) {
			for (const item of obj) searchModel(item);
		}
	}
	searchModel(data);
	return [...new Set(models)];
}

function indexItems() {
	console.log("\n=== DÉBUT DE L'INDEXATION DES ITEMS ===");
	fs.mkdirSync(OUTPUT_DIR, { recursive: true });
	const indexResult = [];

	if (fs.existsSync(ITEMS_DEF_DIR)) {
		const files = fs.readdirSync(ITEMS_DEF_DIR).filter(f => f.endsWith('.json'));

		for (const filename of files) {
			const itemId = filename.replace(".json", "");
			const itemDefPath = path.join(ITEMS_DEF_DIR, filename);

			try {
				const data = JSON.parse(fs.readFileSync(itemDefPath, 'utf-8'));
				const rawModels = extractModelsFromItemDef(data);

				let isBlock = false;
				const filteredModels = [];

				for (const m of rawModels) {
					const cleanRef = m.includes(":") ? m.split(":").pop() : m;
					if (cleanRef.startsWith("block/") || cleanRef.startsWith("models/block/")) {
						isBlock = true;
					} else {
						filteredModels.push(m);
					}
				}

				if (isBlock) continue;

				const combinedTextures = {};
				const modelPaths = [];

				for (const mRef of filteredModels) {
					const cleanM = mRef.includes(":") ? mRef.split(":").pop() : mRef;
					modelPaths.push(`${PACK_DIR_SOURCE}/assets/minecraft/models/${cleanM}.json`);
					Object.assign(combinedTextures, findTexturesInItemModel(mRef));
				}

				const cleanTextures = {};
				for (const [key, val] of Object.entries(combinedTextures)) {
					if (typeof val === 'string' && val) {
						if (val.startsWith("#")) {
							cleanTextures[key] = val;
						} else {
							const cleanPath = val.includes(":") ? val.split(":").pop() : val;
							cleanTextures[key] = `${PACK_DIR_SOURCE}/assets/minecraft/textures/${cleanPath}.png`;
						}
					}
				}

				indexResult.push({
					id: itemId,
					buttonName: titleCase(itemId),
					item_definition: `${PACK_DIR_SOURCE}/assets/minecraft/items/${filename}`,
					models: modelPaths,
					textures: cleanTextures
				});
			} catch (e) {
				console.log(`[ERROR] ${filename}: ${e.message}`);
			}
		}
	}

	const outputFile = path.join(OUTPUT_DIR, config.FILES.ITEMS_INDEX || "Index_Details_Items.json");
	fs.writeFileSync(outputFile, JSON.stringify(indexResult, null, 4), 'utf-8');
	console.log(`=== FIN DE L'INDEXATION : ${indexResult.length} items enregistrés ===\n`);
}

if (require.main === module) indexItems();
module.exports = { indexItems };