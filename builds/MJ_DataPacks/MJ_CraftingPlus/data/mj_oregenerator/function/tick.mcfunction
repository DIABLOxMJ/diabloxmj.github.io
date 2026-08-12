# 1. On cherche la cobblestone qui vient de se former (en contact avec de l'eau et de la lave immobile ou courante) autour des joueurs dans un rayon de 10 blocs
execute if block ~1 ~ ~ minecraft:water run execute if block ~-1 ~ ~ minecraft:lava run function mj_oregenerator:roll_ore

execute if block ~-1 ~ ~ minecraft:water run execute if block ~1 ~ ~ minecraft:lava run function mj_oregenerator:roll_ore

execute if block ~ ~ ~1 minecraft:water run execute if block ~ ~ ~-1 minecraft:lava run function mj_oregenerator:roll_ore

execute if block ~ ~ ~-1 minecraft:water run execute if block ~ ~ ~1 minecraft:lava run function mj_oregenerator:roll_ore

execute if block ~1 ~ ~ minecraft:water run execute if block ~ ~1 ~ minecraft:lava run function mj_oregenerator:roll_ore
execute if block ~-1 ~ ~ minecraft:water run execute if block ~ ~1 ~ minecraft:lava run function mj_oregenerator:roll_ore
execute if block ~ ~ ~1 minecraft:water run execute if block ~ ~1 ~ minecraft:lava run function mj_oregenerator:roll_ore
execute if block ~ ~ ~-1 minecraft:water run execute if block ~ ~1 ~ minecraft:lava run function mj_oregenerator:roll_ore