execute if score #running timer matches 1 run scoreboard players add #time timer 1

# Calcul temps restant
execute if score #running timer matches 1 run scoreboard players set #temp timer 11000
execute if score #running timer matches 1 run scoreboard players operation #temp timer -= #time timer
execute if score #running timer matches 1 run execute store result bossbar game:timer value run scoreboard players get #temp timer

# Déclenchement une seule fois
execute if score #time timer matches 11000 if score #running timer matches 1 run function game:lava_start

# Boucle lave
execute if score #running timer matches 2 run function game:lava_loop