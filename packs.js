// 1. CENTRALISATION DES DONNÉES (Le seul endroit à modifier à l'avenir !)
const DATA_PACKS = [
    { 
        id: 'faithful', 
        titre: 'Faithful',
        imageSrc: 'sources/banner_1.png',
        folder: 'packs/Faithful_Legacy',
        versions: [
            "1.21.11",
            "1.21",
            "1.20.1",
            "1.18.2"
        ]
    },
    { 
        id: 'beta', 
        titre: 'Beta',
        imageSrc: 'sources/banner_2.png',
        folder: 'packs/Beta_Legacy',
        versions: [
            "1.21.11",
            "1.20.1",
            "1.18.2"
        ]
    },
    { 
        id: 'alpha', 
        titre: 'Alpha',
        imageSrc: 'sources/banner_3.png',
        folder: 'packs/Alpha_Legacy',
        versions: [
            "1.21.11",
            "1.20.1",
            "1.18.2"
        ]
    }
];

// 2. LE MOTEUR DE GÉNÉRATION DYNAMIQUE
// - containerId : l'id HTML où injecter la colonne
// - packIdFilter : l'id du pack si on veut n'afficher qu'une colonne (ex: 'faithful'), ou null pour tout afficher (accueil)
// - basePath : gère le recul des dossiers ('../' pour les sous-pages, '' pour la racine)
function initialiserTelechargements(containerId, packIdFilter = null, basePath = '') {
    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = ''; // On vide le conteneur par sécurité

    // On filtre : si on demande un pack précis, on ne prend que lui, sinon on prend tout
    const viewer = packIdFilter 
        ? DATA_PACKS.filter(p => p.id === packIdFilter)
        : DATA_PACKS;

    viewer.forEach(col => {
        // Création de la colonne
        const divColonne = document.createElement('div');
        divColonne.className = 'flex flex-col items-center w-full max-w-[260px] mx-auto';

        // 1. Bannière
        const Banner = document.createElement('img');
        Banner.src = basePath + col.imageSrc;
        Banner.alt = col.titre;
        Banner.className = 'border border-black w-full h-[100px] object-cover shadow-sm bg-gray-200';

        // 2. Liste déroulante
        const VarList = document.createElement('div');
        // On adapte la hauteur : 435px sur une page dédiée, 300px sur l'accueil
        const hauteurClass = packIdFilter ? 'h-[435px]' : 'h-[300px]';
        VarList.className = `bg-white border border-gray-400 shadow-inner w-full ${hauteurClass} overflow-y-auto scroll-list flex flex-col mb-3`;
        
        let verSelect = col.versions[0]; 

        col.versions.forEach((version, index) => {
            const item = document.createElement('div');
            item.className = `py-2 text-center text-[2.5rem] leading-none font-bold underline decoration-[3px] underline-offset-[6px] cursor-pointer hover:bg-gray-100 transition-colors ${index === 0 ? 'item-selection' : ''}`;
            item.textContent = version;
            
            item.addEventListener('click', () => {
                Array.from(VarList.children).forEach(enfant => enfant.classList.remove('item-selection'));
                item.classList.add('item-selection');
                verSelect = version;
            });

            VarList.appendChild(item);
        });

        // 3. Bouton Télécharger
        const DownloadButton = document.createElement('button');
        DownloadButton.className = 'bg-[#00ff00] border-2 border-black w-full py-2.5 mb-1.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
        DownloadButton.textContent = 'Télécharger';
        DownloadButton.style.backgroundImage = `url('${basePath}sources/download.png')`;
        DownloadButton.style.backgroundRepeat = "repeat-x";
        DownloadButton.style.backgroundSize = "auto 100%";
        DownloadButton.addEventListener('click', () => {
            if (typeof afficherNotification === 'function') {
                afficherNotification(`Téléchargement de la version ${verSelect} en cours...`);
            }
            const lien = document.createElement('a');
            lien.href = `${basePath}/${col.folder}/${col.titre.replace(' ', '')}_Legacy_${verSelect}.zip`;
            document.body.appendChild(lien);
            lien.click();
            document.body.removeChild(lien);
        });

        // 4. Bouton Galerie / Redirection
        const ScreenButton = document.createElement('button');
        ScreenButton.className = 'bg-[#00ffff] border-2 border-black w-full py-2.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
        ScreenButton.textContent = 'Galerie';
        ScreenButton.style.backgroundImage = `url('${basePath}sources/gallery.png')`;
        ScreenButton.style.backgroundRepeat = "repeat-x";
        ScreenButton.style.backgroundSize = "auto 100%";
        ScreenButton.addEventListener('click', () => {
            if (packIdFilter) {
                if (typeof afficherNotification === 'function') {
                    afficherNotification(`Vous visualisez déjà la galerie ${col.titre}`);
                }
            } else {
                if (typeof afficherNotification === 'function') {
                    afficherNotification(`Redirection vers la page Galerie de la ${col.titre}...`);
                }
                // Redirection automatique vers le sous-dossier pages/
                window.location.href = `pages/${col.id}.html`;
            }
        });

        // Assemblage
        divColonne.appendChild(Banner);
        divColonne.appendChild(VarList);
        divColonne.appendChild(DownloadButton);
        // divColonne.appendChild(ScreenButton);

        container.appendChild(divColonne);
    });
}