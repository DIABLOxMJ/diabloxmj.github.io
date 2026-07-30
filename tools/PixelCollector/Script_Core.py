import http.server
import socketserver
import json
import base64
import os
from pathlib import Path

PORT = 8000
SAVE_DIR = Path(__file__).parent / "Save"

# S'assurer que le dossier Save existe
SAVE_DIR.mkdir(exist_ok=True)

class CoreHTTPRequestHandler(http.server.SimpleHTTPRequestHandler):
    def _send_cors_headers(self):
        """Ajoute les en-têtes CORS pour autoriser l'accès depuis n'importe quelle origine (file:// ou localhost)."""
        self.send_header('Access-Control-Allow-Origin', '*')
        self.send_header('Access-Control-Allow-Methods', 'POST, GET, OPTIONS')
        self.send_header('Access-Control-Allow-Headers', 'Content-Type')

    def do_OPTIONS(self):
        """Répond aux requêtes pré-vol CORS (Preflight)."""
        self.send_response(200)
        self._send_cors_headers()
        self.end_headers()

    def do_POST(self):
        if self.path == '/api/save':
            content_length = int(self.headers.get('Content-Length', 0))
            post_data = self.rfile.read(content_length)
            
            try:
                data = json.loads(post_data.decode('utf-8'))
                image_data = data.get('image', '')
                filename = data.get('filename', 'export_palette.png')
                
                # Extraire le Base64
                if ',' in image_data:
                    header, image_data = image_data.split(',', 1)
                
                # Décodage et enregistrement dans /Save
                binary_data = base64.b64decode(image_data)
                save_path = SAVE_DIR / filename
                
                with open(save_path, 'wb') as f:
                    f.write(binary_data)
                
                # Réponse succès avec CORS
                self.send_response(200)
                self._send_cors_headers()
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = json.dumps({"status": "success", "path": str(save_path)})
                self.wfile.write(response.encode('utf-8'))
                print(f" Image sauvegardée avec succès : {save_path}")

            except Exception as e:
                self.send_response(500)
                self._send_cors_headers()
                self.send_header('Content-Type', 'application/json')
                self.end_headers()
                response = json.dumps({"status": "error", "message": str(e)})
                self.wfile.write(response.encode('utf-8'))
                print(f" Erreur lors de la sauvegarde : {e}")
        else:
            self.send_error(404, "Endpoint introuvable")

if __name__ == "__main__":
    os.chdir(Path(__file__).parent)
    with socketserver.TCPServer(("", PORT), CoreHTTPRequestHandler) as httpd:
        print(f"==================================================")
        print(f" MCPackIndexer - Core Server actif")
        print(f" Accès Web : http://localhost:{PORT}")
        print(f" Dossier de sauvegarde : {SAVE_DIR.resolve()}")
        print(f"==================================================")
        try:
            httpd.serve_forever()
        except KeyboardInterrupt:
            print("\n Arrêt du serveur Core.")