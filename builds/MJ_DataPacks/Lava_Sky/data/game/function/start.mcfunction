scoreboard objectives add timer dummy
scoreboard players set #time timer 0
scoreboard players set #running timer 1

time set 1000

# Reset joueurs
effect give @a saturation 1 255 true
effect give @a instant_health 1 255 true

# Bordure normale
worldborder set 512

# Bossbar
bossbar add game:timer "Temps avant l'arrivé de la lave"
bossbar set game:timer max 11000
bossbar set game:timer value 11000
bossbar set game:timer visible true

tellraw @a {"text":"⏳ La partie commence !","color":"gold"}
tellraw @a {"text":"- Vous avez jusqu'à la nuit tomber pour récupérer un maximum de ressources avant que la surface devienne inhabitable, le ciel deviendra de la lave. Bonne chance !","color":"yellow"}