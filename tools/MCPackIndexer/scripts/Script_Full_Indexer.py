import os
import json

BASE_DIR_SCRIPT = os.path.dirname(os.path.abspath(__file__))
BASE_DIR = os.path.dirname(BASE_DIR_SCRIPT)
NEXT_DIR = os.path.join(BASE_DIR, "Pack (Next)", "assets")
MAIN_DIR = os.path.join(BASE_DIR, "Pack (Main)", "assets")
OUTPUT_DIR = os.path.join(BASE_DIR, "IndexData")

def scan_assets_folder(target_dir):
    """Scanne récursivement 100% des fichiers d'un dossier assets et retourne une liste de chemins relatifs"""
    file_list = []
    if not os.path.exists(target_dir):
        return file_list
        
    for root, dirs, files in os.walk(target_dir):
        for file in files:
            # On calcule le chemin à partir de 'assets/' pour pouvoir comparer équitablement
            # Ex: 'minecraft/textures/block/stone.png'
            full_path = os.path.join(root, file)
            rel_path = os.path.relpath(full_path, target_dir).replace("\\", "/")
            file_list.append(rel_path)
    return sorted(file_list)

def run_comparaison():
    print("\n=== DÉBUT DE L'INDEXATION COMPLÈTE & COMPARAISON ===")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 1. Indexation du dossier Vanilla
    print(f"[SCAN] Analyse du dossier Vanilla : {NEXT_DIR}...")
    next_files = scan_assets_folder(NEXT_DIR)
    with open(os.path.join(OUTPUT_DIR, "Index_NextPack.json"), "w", encoding="utf-8") as f:
        json.dump(next_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(next_files)} fichiers trouvés dans Vanilla.")

    # 2. Indexation du dossier MyPack (Pack)
    print(f"[SCAN] Analyse du dossier personnel (Pack) : {MAIN_DIR}...")
    main_files = scan_assets_folder(MAIN_DIR)
    with open(os.path.join(OUTPUT_DIR, "Index_OldPack.json"), "w", encoding="utf-8") as f:
        json.dump(main_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(main_files)} fichiers trouvés dans ton Pack.")

    # 3. Calcul des fichiers manquants
    # On transforme en 'set' pour faire une soustraction mathématique ultra-rapide
    next_set = set(next_files)
    main_set = set(main_files)
    
    manquants = sorted(list(next_set - main_set))
    
    # 4. Génération du rapport de comparaison pour l'interface web
    # On crée une structure claire contenant le statut de chaque fichier Vanilla
    comparaison_result = []
    for rel_path in next_files:
        # On extrait une catégorie/dossier pour permettre un filtrage plus tard (ex: textures, models, sounds...)
        parts = rel_path.split('/')
        category = parts[1] if len(parts) > 1 else "root"
        
        comparaison_result.append({
            "path": rel_path,
            "filename": parts[-1],
            "category": category,
            "status": "missing" if rel_path in manquants else "present"
        })

    with open(os.path.join(OUTPUT_DIR, "Index_Comparaison_Missing.json"), "w", encoding="utf-8") as f:
        json.dump(comparaison_result, f, indent=4, ensure_ascii=False)

    print(f" -> Comparaison terminée : {len(manquants)} fichiers manquants détectés.")
    print("=== FIN DE L'INDEXATION COMPLÈTE ===\n")

if __name__ == "__main__":
    run_comparaison()