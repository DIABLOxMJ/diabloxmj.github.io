// La fonction reçoit en paramètre le chemin pour retourner à la racine (ex: './' ou '../')
function injecterHeader(cheminRacine) {
    const headerContainer = document.getElementById('global-header');
    
    if (!headerContainer) return; // Sécurité si la balise n'existe pas

    // On écrit le HTML du menu en adaptant les liens grâce à "cheminRacine"
    headerContainer.innerHTML = `
        <header class="bg-[#d4d4d4]/25 py-6 backdrop-blur-sm border-gray-300 text-white text-ombre">
            <nav class="flex justify-center items-center space-x-2 text-2xl font-bold uppercase tracking-wide">
                <a href="../${cheminRacine}index.html" class="hover:text-gray-300 underline decoration-2 underline-offset-4">ACCUEIL</a>
                <span class="px-2">|</span>
                <a href="../${cheminRacine}pages/faithful.html" class="hover:text-gray-300 underline decoration-2 underline-offset-4">FAITHFUL</a>
                <span class="px-2">|</span>
                <a href="../${cheminRacine}pages/beta.html" class="hover:text-gray-300 underline decoration-2 underline-offset-4">BETA</a>
                <span class="px-2">|</span>
                <a href="../${cheminRacine}pages/alpha.html" class="hover:text-gray-300 underline decoration-2 underline-offset-4">ALPHA</a>
            </nav>
        </header>
    `;
}

// La fonction reçoit en paramètre le chemin pour retourner à la racine (ex: './' ou '../')
function injecterFooter(cheminRacine) {
    const FooterContainer = document.getElementById('global-footer');
    
    if (!FooterContainer) return; // Sécurité si la balise n'existe pas

    // On écrit le HTML du menu en adaptant les liens grâce à "cheminRacine"
    // <a href="#" class="hover:text-gray-300 underline decoration-2 underline-offset-4">Contact</a>
    FooterContainer.innerHTML = `
        <footer class="bg-[#b3b3b3]/25 backdrop-blur-sm py-5 px-10 text-xl font-bold text-white text-ombre flex flex-col sm:flex-row justify-between items-center gap-4">
            <p>2026 © DIABLOxMJ. Tous droits réservés.</p>
            <div class="flex space-x-6">
                <a href="https://bio.link/diabloxmj" class="hover:text-gray-300 underline decoration-2 underline-offset-4">Réseau</a>
            </div>
        </footer>
    `;
}