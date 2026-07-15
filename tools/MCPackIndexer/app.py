import os
import json
import subprocess
import platform
from flask import Flask, jsonify, request
from flask_cors import CORS

app = Flask(__name__)
CORS(app, resources={r"/*": {"origins": "*"}})

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_DIR = os.path.join(BASE_DIR, "finish")

@app.route('/run/<script_type>', methods=['POST'])
def run_script(script_type):
    scripts = {
        'blocks': 'blockindexer.py',
        'items': 'itemindexer.py',
        'sounds': 'soundextractor.py',
        'categories': 'categoryindexer.py',
        'comparison': 'Full_Index_Folder.py',
        'extract': 'extract.py'
    }
    
    if script_type not in scripts:
        return jsonify({"status": "error", "message": "Script inconnu"}), 400
        
    script_name = scripts[script_type]
    script_path = os.path.join(BASE_DIR, script_name)
    
    if not os.path.exists(script_path):
        return jsonify({"status": "error", "message": f"Le fichier {script_name} est introuvable."}), 404

    try:
        result = subprocess.run(['py', script_path], capture_output=True, text=True, check=True)
        return jsonify({
            "status": "success", 
            "message": f"Le script {script_name} s'est exécuté avec succès !\n" + result.stdout
        })
    except subprocess.CalledProcessError as e:
        return jsonify({
            "status": "error", 
            "message": f"Erreur lors de l'exécution de {script_name} : {e.stderr if e.stderr else e.output}"
        }), 500

@app.route('/get-index/<index_type>', methods=['GET'])
def get_index(index_type):
    files = {
        'blocks': 'blocks.json',
        'items': 'items.json'
    }
    if index_type not in files:
        return jsonify({"error": "Index inconnu"}), 400
        
    file_path = os.path.join(OUTPUT_DIR, files[index_type])
    if not os.path.exists(file_path):
        return jsonify([]), 200
        
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

@app.route('/get-categories', methods=['GET'])
def get_categories():
    file_path = os.path.join(OUTPUT_DIR, 'categories.json')
    if not os.path.exists(file_path):
        return jsonify({}), 200
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

# Nouvelle route pour récupérer le rapport de comparaison complet
@app.route('/get-comparison', methods=['GET'])
def get_comparison():
    file_path = os.path.join(OUTPUT_DIR, 'comparison_report.json')
    if not os.path.exists(file_path):
        return jsonify([]), 200
    with open(file_path, 'r', encoding='utf-8') as f:
        return jsonify(json.load(f))

@app.route('/open-file', methods=['POST'])
def open_file():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable : {full_path}"}), 404
        
    try:
        if platform.system() == "Windows":
            os.startfile(full_path)
        elif platform.system() == "Darwin":
            subprocess.run(["open", full_path])
        else:
            subprocess.run(["xdg-open", full_path])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/open-folder', methods=['POST'])
def open_folder():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable : {full_path}"}), 404
        
    try:
        if platform.system() == "Windows":
            subprocess.run([f'explorer.exe', '/select,', full_path])
        else:
            parent_dir = os.path.dirname(full_path)
            if platform.system() == "Darwin":
                subprocess.run(["open", parent_dir])
            else:
                subprocess.run(["xdg-open", parent_dir])
        return jsonify({"status": "success"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/read-file', methods=['POST'])
def read_file():
    rel_path = request.json.get('path')
    if not rel_path:
        return jsonify({"status": "error", "message": "Chemin manquant"}), 400
        
    full_path = os.path.normpath(os.path.join(BASE_DIR, rel_path))
    
    if not os.path.exists(full_path):
        return jsonify({"status": "error", "message": f"Fichier introuvable"}), 404
        
    try:
        with open(full_path, 'r', encoding='utf-8') as f:
            content = f.read()
        return jsonify({"status": "success", "content": content})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)