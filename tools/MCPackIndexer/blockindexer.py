import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
PACK_DIR = os.path.join(BASE_DIR, "Pack", "assets", "minecraft")
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")
BLOCKSTATES_DIR = os.path.join(PACK_DIR, "blockstates")
MODELS_DIR = os.path.join(PACK_DIR, "models")

def find_textures_in_model(model_name):
    """Fonction récursive pour trouver les textures en suivant l'héritage"""
    if ":" in model_name:
        model_name = model_name.split(":")[1]
        
    model_path = os.path.join(MODELS_DIR, f"{model_name}.json")
    
    if not os.path.exists(model_path):
        print(f"  [INFO] Modèle parent introuvable : {model_name}.json (Peut être un modèle de base du jeu non inclus)")
        return {}

    with open(model_path, 'r', encoding='utf-8') as f:
        try:
            data = json.load(f)
        except json.JSONDecodeError:
            print(f"  [ERROR] Impossible de lire le JSON du modèle : {model_name}.json")
            return {}

    textures = {}
    if "textures" in data:
        textures.update(data["textures"])
        
    if "parent" in data:
        parent_textures = find_textures_in_model(data["parent"])
        for k, v in parent_textures.items():
            if k not in textures:
                textures[k] = v
                
    return textures

def index_blocks():
    print("\n=== DÉBUT DE L'INDEXATION DES BLOCS ===")
    print(f"[LOG] Vérification du dossier Pack : {BLOCKSTATES_DIR}")
    
    if not os.path.exists(BLOCKSTATES_DIR):
        print(f"[ERROR] Le dossier {BLOCKSTATES_DIR} n'existe pas !")
        return

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    index_result = []
    
    files = [f for f in os.listdir(BLOCKSTATES_DIR) if f.endswith(".json")]
    print(f"[LOG] {len(files)} fichiers trouvés dans blockstates. Début de l'analyse...")

    for filename in files:
        block_id = filename.replace(".json", "")
        blockstate_path = os.path.join(BLOCKSTATES_DIR, filename)
        
        print(f"[ANALYSE] Bloc : {block_id} ...", end="")

        try:
            with open(blockstate_path, 'r', encoding='utf-8') as f:
                data = json.load(f)
        except Exception as e:
            print(f"\n  [ERROR] Erreur de lecture sur le fichier {filename} : {e}")
            continue

        model_ref = None
        try:
            if "variants" in data:
                if not data["variants"]: # Si le dictionnaire de variantes est vide
                    print(" [Passé: variantes vides]")
                    continue
                first_variant = list(data["variants"].values())[0]
                if isinstance(first_variant, list):
                    if len(first_variant) > 0:
                        model_ref = first_variant[0].get("model")
                else:
                    model_ref = first_variant.get("model")
            elif "multipart" in data:
                if len(data["multipart"]) > 0 and "apply" in data["multipart"][0]:
                    apply_data = data["multipart"][0]["apply"]
                    if isinstance(apply_data, list) and len(apply_data) > 0:
                        model_ref = apply_data[0].get("model")
                    else:
                        model_ref = apply_data.get("model")
        except Exception as e:
            print(f"\n  [ERROR] Structure de blockstate imprévue pour {filename} : {e}")
            continue

        textures = {}
        if model_ref:
            try:
                textures = find_textures_in_model(model_ref)
            except Exception as e:
                print(f"\n  [ERROR] Erreur lors de la recherche des textures pour le modèle {model_ref} : {e}")

        clean_textures = {}
        for key, val in textures.items():
            if val.startswith("#"):
                clean_textures[key] = val
            else:
                clean_path = val.split(":")[-1] if ":" in val else val
                clean_textures[key] = f"Pack/assets/minecraft/textures/{clean_path}.png"

        block_entry = {
            "id": block_id,
            "buttonName": block_id.replace("_", " ").title(),
            "blockstate": f"Pack/assets/minecraft/blockstates/{filename}",
            "model": f"Pack/assets/minecraft/models/{model_ref.split(':')[-1]}.json" if model_ref else None,
            "textures": clean_textures
        }
        index_result.append(block_entry)
        print(" OK") # Tout s'est bien passé pour ce bloc

    output_path = os.path.join(OUTPUT_DIR, "blocks.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(index_result, f, indent=4, ensure_ascii=False)
        
    print(f"=== FIN DE L'INDEXATION : {len(index_result)} blocs enregistrés ===\n")

if __name__ == "__main__":
    index_blocks()