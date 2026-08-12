# On fait apparaître temporairement un coffre invisible ou un item pour lire le résultat de la loot table
# Mais plus simple : on invoque un item au niveau du bloc, on regarde ce qu'il donne, et on remplace le bloc.
# Pour faire ultra propre et performant, on utilise un Shulker Box temporaire qu'on détruit ou dont on copie le contenu :

setblock ~ ~-500 ~ minecraft:dropper{LootTable:"mj_craft:ore_generator"}
# On récupère le contenu du dropper virtuel et on l'applique au bloc de cobble
execute if data block ~ ~-500 ~ Items[{id:"minecraft:cobblestone"}] run setblock ~ ~ ~ minecraft:cobblestone replace
execute if data block ~ ~-500 ~ Items[{id:"minecraft:coal_ore"}] run setblock ~ ~ ~ minecraft:coal_ore replace
execute if data block ~ ~-500 ~ Items[{id:"minecraft:iron_ore"}] run setblock ~ ~ ~ minecraft:iron_ore replace
execute if data block ~ ~-500 ~ Items[{id:"minecraft:gold_ore"}] run setblock ~ ~ ~ minecraft:gold_ore replace
execute if data block ~ ~-500 ~ Items[{id:"minecraft:diamond_ore"}] run setblock ~ ~ ~ minecraft:diamond_ore replace

# Nettoyage du dropper virtuel sous la map
setblock ~ ~-500 ~ minecraft:air