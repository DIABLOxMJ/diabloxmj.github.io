import os
import json

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
TEXTURES_DIR = os.path.join(BASE_DIR, "Pack", "assets", "minecraft", "textures")
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")

def index_categories():
    print("\n=== DÉBUT DE L'INDEXATION DES CATÉGORIES (PAR DOSSIER) ===")
    
    if not os.path.exists(TEXTURES_DIR):
        print(f"[ERROR] Le dossier {TEXTURES_DIR} n'existe pas !")
        return

    os.makedirs(OUTPUT_DIR, exist_ok=True)
    categories_data = {}
    ignored_folders = ["block", "item"]

    for entry in os.listdir(TEXTURES_DIR):
        entry_path = os.path.join(TEXTURES_DIR, entry)
        
        if os.path.isdir(entry_path) and entry not in ignored_folders:
            print(f"[SCAN] Racine détectée : {entry}")
            
            # On va stocker temporairement les dossiers de cette catégorie
            folder_groups = {}

            for root, dirs, files in os.walk(entry_path):
                # On ne prend que les fichiers PNG
                png_files = [f for f in files if f.endswith(".png")]
                
                if png_files:
                    # On calcule le nom du dossier par rapport à la racine des textures
                    # Ex: Pack/assets/.../textures/trims/color_palettes -> "Trims/Color Palettes"
                    rel_folder_path = os.path.relpath(root, TEXTURES_DIR).replace("\\", "/")
                    display_name = rel_folder_path.replace("_", " ").title()
                    
                    # Id unique basé sur le chemin du dossier
                    folder_id = rel_folder_path.lower().replace("/", "-")

                    if folder_id not in folder_groups:
                        folder_groups[folder_id] = {
                            "id": folder_id,
                            "buttonName": display_name,
                            "folder_path": os.path.relpath(root, BASE_DIR).replace("\\", "/"),
                            "textures": []
                        }

                    for file in png_files:
                        full_file_path = os.path.join(root, file)
                        rel_file_path = os.path.relpath(full_file_path, BASE_DIR).replace("\\", "/")
                        
                        folder_groups[folder_id]["textures"].append({
                            "name": file.replace(".png", ""),
                            "filename": file,
                            "path": rel_file_path
                        })

            # On transfère les groupes dans notre dictionnaire final
            categories_data[entry] = list(folder_groups.values())

    output_path = os.path.join(OUTPUT_DIR, "categories.json")
    with open(output_path, 'w', encoding='utf-8') as f:
        json.dump(categories_data, f, indent=4, ensure_ascii=False)
        
    print(f"=== FIN DE L'INDEXATION : Groupement par dossier réussi ===\n")

if __name__ == "__main__":
    index_categories()