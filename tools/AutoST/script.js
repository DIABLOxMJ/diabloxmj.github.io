const dropZone = document.getElementById('drop-zone');
const fileInput = document.getElementById('file-input');
const zipInput = document.getElementById('zip-input');
const videoPlayer = document.getElementById('video-player');
const videoContainer = document.getElementById('video-container');
const fullscreenBtn = document.getElementById('fullscreen-btn');
const audioModeSelect = document.getElementById('audio-mode-select');
const subtitlesDisplay = document.getElementById('subtitles-display');
const downloadBtn = document.getElementById('download-srt');
const changeVideoBtn = document.getElementById('change-video-btn');
const generateIaBtn = document.getElementById('generate-ia-btn');
const loadZipBtn = document.getElementById('load-zip-btn');
const saveProjectBtn = document.getElementById('save-project-btn');
const languageSelect = document.getElementById('language-select');
const statusMessage = document.getElementById('status-message');
const openFolderBtn = document.getElementById('open-folder-btn');
const appTitle = document.getElementById('app-title');

let subtitlesFr = []; 
let subtitlesEn = [];
let currentLanguage = 'fr';
let currentVideoFile = null;

// Déclencheurs clics manuels
dropZone.addEventListener('click', () => fileInput.click());
changeVideoBtn.addEventListener('click', () => fileInput.click());
loadZipBtn.addEventListener('click', () => zipInput.click());

fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) handleVideo(e.target.files[0]);
});

zipInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) traiterFichierZip(e.target.files[0]);
});

languageSelect.addEventListener('change', (e) => {
    currentLanguage = e.target.value;
    subtitlesDisplay.innerText = "";
});

// Déclencheur pour ouvrir le dossier local via Python
openFolderBtn.addEventListener('click', () => {
    fetch('http://localhost:5000/open_folder', {
        method: 'POST'
    })
    .catch(err => {
        statusMessage.innerText = "Erreur : Impossible de joindre le serveur Python.";
        console.error(err);
    });
});

// --- GESTION DU DRAG & DROP INTELLIGENT (Zone de départ + Lecteur Vidéo) ---
const elementsAvecDrop = [dropZone, videoPlayer];

elementsAvecDrop.forEach(element => {
    element.addEventListener('dragover', (e) => e.preventDefault());

    element.addEventListener('drop', (e) => {
        e.preventDefault();
        if (e.dataTransfer.files.length > 0) {
            const fichierRecu = e.dataTransfer.files[0];
            
            if (fichierRecu.name.endsWith('.zip')) {
                traiterFichierZip(fichierRecu);
            } else {
                handleVideo(fichierRecu);
            }
        }
    });
});

// --- CERVEAU DU TRAITEMENT ZIP ---
function traiterFichierZip(file) {
    statusMessage.innerText = "Lecture du pack ZIP...";
    const reader = new FileReader();
    
    reader.onload = function(event) {
        JSZip.loadAsync(event.target.result).then(async (zip) => {
            const fileFr = zip.file("sous-titres_FR.srt");
            const fileEn = zip.file("sous-titres_EN.srt");

            if (!fileFr || !fileEn) {
                statusMessage.innerText = "Erreur : Le ZIP doit contenir 'sous-titres_FR.srt' et 'sous-titres_EN.srt'.";
                return;
            }

            subtitlesFr = parseSRT(await fileFr.async("string"));
            subtitlesEn = parseSRT(await fileEn.async("string"));

            generateIaBtn.style.display = 'none';
            loadZipBtn.style.display = 'none';
			openFolderBtn.style.display = 'block';
			fullscreenBtn.style.display = 'block';
            activationSousTitres("Pack de sous-titres importé avec succès !");
        }).catch(err => {
            statusMessage.innerText = "Fichier ZIP invalide ou corrompu.";
            console.error(err);
        });
    };
    reader.readAsArrayBuffer(file);
}

// --- ÉTAPE 1 : AJOUT DE LA VIDÉO ---
function handleVideo(file) {
    currentVideoFile = file;
    const fileUrl = URL.createObjectURL(file);
    videoPlayer.src = fileUrl;
    videoContainer.style.display = 'block';
    dropZone.style.display = 'none';
    
    // --- NOUVEAU : DETECTION DU FORMAT AUDIO SEUL ---
    // Si le fichier est un MP3 ou un WAV, on applique le style surélevé
    if (file.name.endsWith('.mp3') || file.name.endsWith('.wav') || file.type.startsWith('audio/')) {
        videoContainer.classList.add('format-audio-seul');
        console.log("Format audio seul détecté : Ajustement de la hauteur des sous-titres.");
    } else {
        videoContainer.classList.remove('format-audio-seul');
    }
    
    // Activer les boutons
    if (subtitlesFr.length > 0) {
        downloadBtn.style.display = 'inline-block';
        saveProjectBtn.style.display = 'inline-block';
    }
    
    statusMessage.innerText = `Fichier chargé : ${file.name}`;
    appTitle.innerText = file.name;
}

// --- ÉTAPE 2 - OPTION A : LANCEMENT DE L'IA ---
generateIaBtn.addEventListener('click', () => {
    if (!currentVideoFile) return;

    generateIaBtn.style.display = 'none';
    loadZipBtn.style.display = 'none';
	openFolderBtn.style.display = 'block';
    statusMessage.innerText = "IA en cours d'analyse et traduction... Veuillez patienter.";

    const formData = new FormData();
    formData.append('video', currentVideoFile);
	formData.append('audio_mode', audioModeSelect.value);

    fetch('http://localhost:5000/transcribe', {
        method: 'POST',
        body: formData
    })
    .then(response => response.json())
    .then(data => {
        subtitlesFr = data.fr; 
        subtitlesEn = data.en; 
        activationSousTitres("Analyse IA terminée !");
    })
    .catch(err => {
        statusMessage.innerText = "Erreur avec le serveur Python local.";
        generateIaBtn.style.display = 'block';
        loadZipBtn.style.display = 'block';
        console.error(err);
    });
});

// --- INTERFACE DE VALIDATION ---
function activationSousTitres(message) {
    statusMessage.innerText = message;
    setTimeout(() => { statusMessage.innerText = ""; }, 3000);
    
    // Les deux boutons s'allument (Fin du mode gris)
    downloadBtn.disabled = false;
    saveProjectBtn.disabled = false; 
	downloadBtn.style.display = 'inline-block';
    saveProjectBtn.style.display = 'inline-block';
    languageSelect.style.display = 'block';
}

// --- EXPORTS (TÉLÉCHARGEMENT ET STOCKAGE) ---
function generateZipContent() {
    const zip = new JSZip();

    let srtFr = "";
    subtitlesFr.forEach((sub, index) => { srtFr += `${index + 1}\n${formatTime(sub.start)} --> ${formatTime(sub.end)}\n${sub.text}\n\n`; });
    zip.file("sous-titres_FR.srt", srtFr);

    let srtEn = "";
    subtitlesEn.forEach((sub, index) => { srtEn += `${index + 1}\n${formatTime(sub.start)} --> ${formatTime(sub.end)}\n${sub.text}\n\n`; });
    zip.file("sous-titres_EN.srt", srtEn);

    return zip.generateAsync({ type: "blob" });
}

downloadBtn.addEventListener('click', () => {
    generateZipContent().then((content) => {
        const url = URL.createObjectURL(content);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${currentVideoFile.name.split('.')[0]}_sous-titres.zip`;
        a.click();
    });
});

saveProjectBtn.addEventListener('click', () => {
    statusMessage.innerText = "Sauvegarde dans le dossier stock_srt...";
    
    generateZipContent().then((content) => {
        const formData = new FormData();
        const zipName = `${currentVideoFile.name.split('.')[0]}_sous-titres.zip`;
        formData.append('zip', content, zipName);

        fetch('http://localhost:5000/save_zip', {
            method: 'POST',
            body: formData
        })
        .then(response => response.json())
        .then(data => {
            statusMessage.innerText = data.status;
            setTimeout(() => { statusMessage.innerText = ""; }, 4000);
        })
        .catch(err => {
            statusMessage.innerText = "Erreur lors de la sauvegarde locale.";
            console.error(err);
        });
    });
});

// --- OUTILS DE TRAITEMENT TEXTE (PARSER SRT / TIMING) ---
function parseSRT(data) {
    const segments = [];
    const blocks = data.replace(/\r\n/g, '\n').split('\n\n');

    blocks.forEach(block => {
        const lines = block.split('\n');
        if (lines.length >= 3) {
            const timeMatch = lines[1].match(/(\d+:\d+:\d+,\d+) --> (\d+:\d+:\d+,\d+)/);
            if (timeMatch) {
                const start = convertSrtTimeToSeconds(timeMatch[1]);
                const end = convertSrtTimeToSeconds(timeMatch[2]);
                const text = lines.slice(2).join(' '); 
                segments.push({ start, end, text }); 
            }
        }
    });
    return segments;
}

function convertSrtTimeToSeconds(timeString) {
    const parts = timeString.replace(',', '.').split(':');
    return parseFloat(parts[0]) * 3600 + parseFloat(parts[1]) * 60 + parseFloat(parts[2]);
}

function formatTime(seconds) {
    const date = new Date(null);
    date.setSeconds(seconds);
    const ms = Math.floor((seconds % 1) * 1000).toString().padStart(3, '0');
    const timeString = date.toISOString().substr(11, 8);
    return `${timeString},${ms}`;
}

// --- SYNCHRONISATION VIDEO ---
videoPlayer.addEventListener('timeupdate', () => {
    const currentTime = videoPlayer.currentTime;
    const activeSubtitles = (currentLanguage === 'fr') ? subtitlesFr : subtitlesEn;
    const currentSub = activeSubtitles.find(sub => currentTime >= sub.start && currentTime <= sub.end);
    
    if (currentSub) {
        subtitlesDisplay.innerText = currentSub.text;
    } else {
        subtitlesDisplay.innerText = ""; 
    }
});

// --- CONFIGURATION DU MODE CINÉMA FLUIDE AVEC EFFET F11 ---
function basculerPleinEcran() {
    const exitFullscreenBtn = document.getElementById('exit-fullscreen-btn');
    
    if (!videoContainer.classList.contains('mode-cinema-total')) {
        // 1. On applique notre superbe mode cinéma
        videoContainer.classList.add('mode-cinema-total');
        if (exitFullscreenBtn) exitFullscreenBtn.style.display = 'block';
        
        // 2. SIMULATION F11 : On demande au navigateur de passer en plein écran total
        if (document.documentElement.requestFullscreen) {
            document.documentElement.requestFullscreen().catch(err => console.log("Lancement plein écran bloqué ou déjà actif"));
        }
    } else {
        // 1. On retire le mode cinéma
        videoContainer.classList.remove('mode-cinema-total');
        if (exitFullscreenBtn) exitFullscreenBtn.style.display = 'none';
        
        // 2. SORTIE F11 : On demande au navigateur de quitter le plein écran total
        if (document.fullscreenElement) {
            document.exitFullscreen().catch(err => console.log("Erreur lors de la sortie"));
        }
    }
}

// Clic sur le bouton "➕" au coin du lecteur
fullscreenBtn.addEventListener('click', basculerPleinEcran);

// Double-clic direct sur la vidéo
videoPlayer.addEventListener('dblclick', basculerPleinEcran);

// Clic sur le bouton "❌" de sortie
const exitFullscreenBtn = document.getElementById('exit-fullscreen-btn');
if (exitFullscreenBtn) {
    exitFullscreenBtn.addEventListener('click', basculerPleinEcran);
}

// Touche "Échap" ou "Escape" : On s'assure de nettoyer le style si l'utilisateur quitte
document.addEventListener('keydown', (e) => {
    if (e.key === "Escape" && videoContainer.classList.contains('mode-cinema-total')) {
        basculerPleinEcran();
    }
});

// Écouteur de sécurité : Si l'utilisateur quitte le mode F11 avec son clavier, on synchronise le lecteur
document.addEventListener('fullscreenchange', () => {
    if (!document.fullscreenElement && videoContainer.classList.contains('mode-cinema-total')) {
        videoContainer.classList.remove('mode-cinema-total');
        if (exitFullscreenBtn) exitFullscreenBtn.style.display = 'none';
    }
});