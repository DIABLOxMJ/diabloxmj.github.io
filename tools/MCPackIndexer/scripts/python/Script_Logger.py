import os
import logging
from datetime import datetime

# Chemin du fichier log dans le dossier IndexData ou à la racine
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LOG_DIR = os.path.join(BASE_DIR, "logs")
os.makedirs(LOG_DIR, exist_ok=True)

# Nom de fichier avec la date du jour (ex: 2026-08-05.log)
LOG_FILE = os.path.join(LOG_DIR, f"{datetime.now().strftime('%Y-%m-%d')}.log")

def setup_logger(name_script):
    """Initialise et renvoie un logger configuré pour écrire dans la console et un fichier .log"""
    logger = logging.getLogger(name_script)
    logger.setLevel(logging.DEBUG)

    # Éviter la duplication des handlers si appelé plusieurs fois
    if logger.hasHandlers():
        return logger

    # Format personnalisé : [Date Heure] [Nom Script] [Niveau] Message
    formatter = logging.Formatter(
        '[%(asctime)s] [%(name)s] [%(levelname)s] %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S'
    )

    # 1. Handler pour écrire dans le fichier .log
    file_handler = logging.FileHandler(LOG_FILE, encoding='utf-8')
    file_handler.setLevel(logging.DEBUG)
    file_handler.setFormatter(formatter)

    # 2. Handler pour afficher dans le terminal/console
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_handler.setFormatter(formatter)

    # Ajout des handlers
    logger.addHandler(file_handler)
    logger.addHandler(console_handler)

    return logger