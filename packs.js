const DATA_PACKS = [
    { 
        id: 'faithful', 
        titre: 'Faithful',
        imageSrc: 'sources/banner_1.png',
        folder: 'packs/Faithful_Legacy',
        versions: ["1.21.11", "1.21", "1.20.1", "1.18.2"]
    },
    { 
        id: 'beta', 
        titre: 'Beta',
        imageSrc: 'sources/banner_2.png',
        folder: 'packs/Beta_Legacy',
        versions: ["1.21.11", "1.20.1", "1.18.2"]
    },
    { 
        id: 'alpha', 
        titre: 'Alpha',
        imageSrc: 'sources/banner_3.png',
        folder: 'packs/Alpha_Legacy',
        versions: ["1.21.11", "1.20.1", "1.18.2"]
    }
];

// FONCTION COMMUNE POUR JOUER UN SON SANS LATENCE
function PlaySound(nomFichier, basePath = '') {
    const audio = new Audio(`${basePath}sources/${nomFichier}`);
    audio.volume = 0.25; // Volume à 25% pour rester discret et agréable
    audio.play().catch(err => console.log("Audio bloqué par le navigateur avant interaction."));
}

function initialiserTelechargements(containerId, packIdFilter = null, basePath = '') {
    
    if (!document.getElementById('changelog-modal')) {
        const modal = document.createElement('div');
        modal.id = 'changelog-modal';
        modal.className = 'fixed inset-0 bg-black/80 z-50 flex items-center justify-center opacity-0 pointer-events-none transition-opacity duration-300 backdrop-blur-sm px-4';
        modal.innerHTML = `
            <div id="changelog-box" class="bg-[#1a1a1a] border-2 border-neutral-700 w-full max-w-6xl p-6 rounded shadow-2xl relative text-white text-ombre max-h-[80vh] flex flex-col transform scale-95 transition-transform duration-300">
                <button id="close-changelog" class="absolute top-4 right-4 text-gray-400 hover:text-white font-bold text-2xl cursor-pointer leading-none">&times;</button>
                <h3 id="changelog-title" class="text-2xl font-bold mb-4 text-cyan-400 border-b border-neutral-700 pb-2 uppercase tracking-wide"></h3>
                <div id="changelog-content" class="text-lg leading-relaxed overflow-y-auto scroll-list text-justify pr-2 flex-grow font-medium whitespace-pre-wrap"></div>
            </div>
        `;
        document.body.appendChild(modal);

        const fermerModal = () => {
            const box = document.getElementById('changelog-box');
            // SON : Clic quand on ferme la pop-up
            PlaySound('click.mp3', basePath);
            
            modal.classList.remove('opacity-100', 'pointer-events-auto');
            modal.classList.add('opacity-0', 'pointer-events-none');
            box.classList.remove('scale-100');
            box.classList.add('scale-95');
        };
        modal.querySelector('#close-changelog').addEventListener('click', fermerModal);
        modal.addEventListener('click', (e) => { if (e.target === modal) fermerModal(); });
    }

    const container = document.getElementById(containerId);
    if (!container) return;

    container.innerHTML = ''; 

    const viewer = packIdFilter ? DATA_PACKS.filter(p => p.id === packIdFilter) : DATA_PACKS;

    viewer.forEach(col => {
        const divColonne = document.createElement('div');
        divColonne.className = 'flex flex-col items-center w-full max-w-[260px] mx-auto';

        const Banner = document.createElement('img');
        Banner.src = basePath + col.imageSrc;
        Banner.alt = col.titre;
        Banner.className = 'border border-black w-full h-[100px] object-cover shadow-sm bg-gray-200';

        const VarList = document.createElement('div');
        const hauteurClass = packIdFilter ? 'h-[435px]' : 'h-[300px]';
        VarList.className = `bg-white border border-gray-400 shadow-inner w-full ${hauteurClass} overflow-y-auto scroll-list flex flex-col mb-3`;
        
        let verSelect = col.versions[0]; 

        col.versions.forEach((version, index) => {
            const item = document.createElement('div');
            item.className = `py-2 text-center text-[2.5rem] leading-none font-bold underline decoration-[3px] underline-offset-[6px] cursor-pointer hover:bg-gray-100 transition-colors ${index === 0 ? 'item-selection' : ''}`;
            item.textContent = version;
            
            item.addEventListener('click', () => {
                // SON : Petit clic quand on change de version dans la liste
                PlaySound('click.mp3', basePath);
                
                Array.from(VarList.children).forEach(enfant => enfant.classList.remove('item-selection'));
                item.classList.add('item-selection');
                verSelect = version;
            });

            VarList.appendChild(item);
        });

        const DownloadButton = document.createElement('button');
        DownloadButton.className = 'bg-[#00ff00] border-2 border-black w-full py-2.5 mb-1.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
        DownloadButton.textContent = 'Télécharger';
        DownloadButton.style.backgroundImage = `url('${basePath}sources/download.png')`;
        DownloadButton.style.backgroundRepeat = "repeat-x";
        DownloadButton.style.backgroundSize = "auto 100%";
        DownloadButton.addEventListener('click', () => {
            // SON : Clic au téléchargement
            PlaySound('click.mp3', basePath);
            
            if (typeof afficherNotification === 'function') {
                afficherNotification(`Téléchargement de la version ${verSelect} en cours...`);
            }
            const lien = document.createElement('a');
            lien.href = `${basePath}${col.folder}/${col.titre.replace(' ', '')}_Legacy_${verSelect}.zip`;
            document.body.appendChild(lien);
            lien.click();
            document.body.removeChild(lien);
        });

        const ChangelogButton = document.createElement('button');
        ChangelogButton.className = 'bg-[#00ffff] border-2 border-black w-full py-2.5 font-bold text-white text-ombre hover:brightness-125 hover:border-white cursor-pointer transition-all duration-200 shadow-sm texte-ombre';
        ChangelogButton.textContent = 'Changelog';
        ChangelogButton.style.backgroundImage = `url('${basePath}sources/changelog.png')`;
        ChangelogButton.style.backgroundRepeat = "repeat-x";
        ChangelogButton.style.backgroundSize = "auto 100%";
        ChangelogButton.addEventListener('click', () => {
            // SON : Clic à l'ouverture du Changelog
            PlaySound('click.mp3', basePath);
            
            const modal = document.getElementById('changelog-modal');
            const box = document.getElementById('changelog-box');
            const titleZone = document.getElementById('changelog-title');
            const contentZone = document.getElementById('changelog-content');

            const cheminTxt = `${basePath}${col.folder}/changelogs/${verSelect}.txt`;

            fetch(cheminTxt)
                .then(response => {
                    if (!response.ok) throw new Error("Fichier introuvable");
                    return response.text();
                })
                .then(texte => {
                    titleZone.textContent = `Changelog - ${col.titre} Legacy ${verSelect}`;
                    contentZone.textContent = texte;
                    
                    modal.classList.remove('opacity-0', 'pointer-events-none');
                    modal.classList.add('opacity-100', 'pointer-events-auto');
                    box.classList.remove('scale-95');
                    box.classList.add('scale-100');
                })
                .catch(() => {
                    if (typeof afficherNotification === 'function') {
                        afficherNotification(`⚠ Aucun changelog disponible pour la version ${verSelect}`);
                    }
                });
        });

        divColonne.appendChild(Banner);
        divColonne.appendChild(VarList);
        divColonne.appendChild(DownloadButton);
        divColonne.appendChild(ChangelogButton);

        container.appendChild(divColonne);
    });
}