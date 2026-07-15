import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
VANILLA_DIR = os.path.join(BASE_DIR, "Vanilla", "assets")
MYPACK_DIR = os.path.join(BASE_DIR, "Pack", "assets") # On garde "Pack" pour correspondre à ton dossier actuel
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")

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

def run_comparison():
    print("\n=== DÉBUT DE L'INDEXATION COMPLÈTE & COMPARAISON ===")
    os.makedirs(OUTPUT_DIR, exist_ok=True)

    # 1. Indexation du dossier Vanilla
    print(f"[SCAN] Analyse du dossier Vanilla : {VANILLA_DIR}...")
    vanilla_files = scan_assets_folder(VANILLA_DIR)
    with open(os.path.join(OUTPUT_DIR, "VanillaIndex.json"), "w", encoding="utf-8") as f:
        json.dump(vanilla_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(vanilla_files)} fichiers trouvés dans Vanilla.")

    # 2. Indexation du dossier MyPack (Pack)
    print(f"[SCAN] Analyse du dossier personnel (Pack) : {MYPACK_DIR}...")
    mypack_files = scan_assets_folder(MYPACK_DIR)
    with open(os.path.join(OUTPUT_DIR, "MyPack.json"), "w", encoding="utf-8") as f:
        json.dump(mypack_files, f, indent=4, ensure_ascii=False)
    print(f" -> {len(mypack_files)} fichiers trouvés dans ton Pack.")

    # 3. Calcul des fichiers manquants
    # On transforme en 'set' pour faire une soustraction mathématique ultra-rapide
    vanilla_set = set(vanilla_files)
    mypack_set = set(mypack_files)
    
    manquants = sorted(list(vanilla_set - mypack_set))
    
    # 4. Génération du rapport de comparaison pour l'interface web
    # On crée une structure claire contenant le statut de chaque fichier Vanilla
    comparison_result = []
    for rel_path in vanilla_files:
        # On extrait une catégorie/dossier pour permettre un filtrage plus tard (ex: textures, models, sounds...)
        parts = rel_path.split('/')
        category = parts[1] if len(parts) > 1 else "root"
        
        comparison_result.append({
            "path": rel_path,
            "filename": parts[-1],
            "category": category,
            "status": "missing" if rel_path in manquants else "present"
        })

    with open(os.path.join(OUTPUT_DIR, "comparison_report.json"), "w", encoding="utf-8") as f:
        json.dump(comparison_result, f, indent=4, ensure_ascii=False)

    print(f" -> Comparaison terminée : {len(manquants)} fichiers manquants détectés.")
    print("=== FIN DE L'INDEXATION COMPLÈTE ===\n")

if __name__ == "__main__":
    run_comparison()