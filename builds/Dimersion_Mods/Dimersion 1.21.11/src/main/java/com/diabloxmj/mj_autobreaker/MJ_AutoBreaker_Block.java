package com.diabloxmj.mj_autobreaker; // Déclare le dossier (package) où se trouve ce fichier de code

import com.mojang.serialization.MapCodec; // Importe l'outil de sérialisation de Mojang (requis en 1.21+ pour sauvegarder le type de bloc)
import net.minecraft.block.*; // Importe toutes les classes liées aux blocs (BlockWithEntity, BlockState...)
import net.minecraft.block.entity.BlockEntity; // Importe la classe de base des inventaires/données attachés aux blocs
import net.minecraft.item.ItemPlacementContext; // Importe les informations du contexte de pose (direction du regard du joueur, bloc ciblé...)
import net.minecraft.server.world.ServerWorld; // Importe la classe représentant le monde côté serveur (gestion des calculs logiques et ticks)
import net.minecraft.state.StateManager; // Importe l'outil qui gère les variables d'état du bloc (ses propriétés)
import net.minecraft.state.property.BooleanProperty; // Importe le type de propriété Oui/Non (Vrai/Faux)
import net.minecraft.state.property.Properties; // Importe la liste des propriétés standards de Minecraft
import net.minecraft.util.BlockMirror; // Importe l'outil gérant l'effet miroir (pour les structures générées ou les plans)
import net.minecraft.util.BlockRotation; // Importe l'outil gérant la rotation des blocs (ex: pistons, structures)
import net.minecraft.util.math.BlockPos; // Importe l'objet stockant les coordonnées X, Y, Z dans le monde
import net.minecraft.util.math.Direction; // Importe l'énumération des directions (NORTH, SOUTH, EAST, WEST, UP, DOWN)
import net.minecraft.util.math.random.Random; // Importe l'outil de génération de nombres aléatoires de Minecraft
import net.minecraft.world.World; // Importe la classe générale du monde (utilisée pour le client et le serveur)
import net.minecraft.world.block.WireOrientation; // Importe l'outil d'orientation des flux de redstone (1.21+)
import org.jspecify.annotations.Nullable; // Importe l'annotation indiquant qu'une variable ou un retour peut être "null" sans crasher

public class MJ_AutoBreaker_Block extends BlockWithEntity { // Déclare notre bloc personnalisé en héritant de la classe des blocs possédant une entité/un inventaire

    // Déclaration de la propriété FACING (la direction vers laquelle regarde la face avant de notre machine)
    public static final net.minecraft.state.property.EnumProperty<net.minecraft.util.math.Direction> FACING = net.minecraft.state.property.Properties.FACING;
    // Déclaration de la propriété TRIGGERED (Est-ce que le bloc a reçu un signal Redstone ? Vrai/Faux)
    public static final BooleanProperty TRIGGERED = Properties.TRIGGERED;

    // Le CODEC est une obligation en 1.21+. Il explique au moteur de Minecraft comment charger/sauvegarder ce type de bloc depuis les fichiers du jeu
    public static final MapCodec<MJ_AutoBreaker_Block> CODEC = createCodec(MJ_AutoBreaker_Block::new);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() { // Méthode requise par Minecraft pour récupérer le Codec déclaré ci-dessus
        return CODEC;
    }

    // Constructeur de notre bloc : il s'exécute lors du "new MJ_AutoBreaker_Block(...)" dans la classe précédente
    public MJ_AutoBreaker_Block(Settings settings) {
        super(settings); // Transmet les paramètres (dureté, résistance) à la classe mère (BlockWithEntity)
        // Définit l'état par défaut du bloc lorsqu'il apparaît : il regarde vers le NORD et n'est PAS activé par la redstone (triggered=false)
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH).with(TRIGGERED, false));
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) { // Méthode qui crée physiquement l'inventaire dans le monde
        return new MJ_AutoBreaker_BlockEntity(pos, state); // Renvoie une nouvelle instance de notre classe de données (l'inventaire)
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) { // Définit comment le bloc doit s'afficher à l'écran
        return BlockRenderType.MODEL; // Indique au jeu d'utiliser nos fichiers JSON de modèles 3D standards (plutôt qu'un rendu invisible ou d'entité)
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) { // Calcule l'état du bloc au moment précis où le joueur fait un clic droit pour le poser
        // Renvoie l'état par défaut, mais modifie le FACING pour que la face avant regarde à l'opposé du joueur (comme un dispenser ou un observer)
        // Et vérifie immédiatement si le bloc reçoit déjà de la Redstone à l'emplacement où il est posé
        return this.getDefaultState()
                .with(FACING, context.getPlayerLookDirection().getOpposite())
                .with(TRIGGERED, context.getWorld().isReceivingRedstonePower(context.getBlockPos()));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, @Nullable WireOrientation wireOrientation, boolean notify) { // S'exécute dès qu'un bloc voisin change (ex: un fil de redstone s'allume)
        if (!world.isClient()) { // S'assure qu'on fait le calcul uniquement côté Serveur (la logique de jeu) et pas côté Client (l'affichage)
            boolean isBeingPowered = world.isReceivingRedstonePower(pos); // Vérifie si le bloc reçoit actuellement du courant redstone
            boolean wasTriggered = state.get(TRIGGERED); // Récupère l'ancien état de la redstone stocké sur le bloc (était-il allumé ?)

            if (isBeingPowered && !wasTriggered) { // SI le bloc reçoit du courant ET qu'il était éteint (Front montant de Redstone)
                // Planifie un événement (un "tick" de bloc) qui s'exécutera dans 4 ticks de jeu (environ 0.2 secondes)
                world.scheduleBlockTick(pos, this, 4);
                // Met à jour le bloc dans le monde en passant la propriété TRIGGERED à true
                world.setBlockState(pos, state.with(TRIGGERED, true), Block.NOTIFY_LISTENERS);
            } else if (!isBeingPowered && wasTriggered) { // SINON SI le bloc ne reçoit plus de courant mais qu'il était allumé (Front descendant)
                // Met à jour le bloc dans le monde en passant la propriété TRIGGERED à false
                world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_LISTENERS);
            }
        }
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, net.minecraft.util.math.random.Random random) {
        if (!world.isClient()) {
            // SÉCURITÉ : On vérifie si la Redstone alimente TOUJOURS le bloc à cet instant précis
            if (world.isReceivingRedstonePower(pos)) {

                BlockEntity blockEntity = world.getBlockEntity(pos);
                if (blockEntity instanceof MJ_AutoBreaker_BlockEntity autoBreaker) {
                    // On vérifie s'il y a un bloc à casser devant
                    if (!world.getBlockState(pos.offset(state.get(FACING))).isAir()) {
                        autoBreaker.tryBreakBlock(world, pos, state.get(FACING));
                    }
                }

                // Le courant est toujours là, on planifie la prochaine action dans 1 seconde (20 ticks)
                world.scheduleBlockTick(pos, this, 20);

            } else {
                // Si le courant a été coupé entre-temps, on force le bloc à repasser en mode éteint
                if (state.get(TRIGGERED)) {
                    world.setBlockState(pos, state.with(TRIGGERED, false), Block.NOTIFY_LISTENERS);
                }
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) { // Indique à Minecraft quelles variables d'état notre bloc utilise
        builder.add(FACING, TRIGGERED); // Ajoute la direction et le statut Redstone au gestionnaire d'état du bloc
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) { // Gère la rotation du bloc (ex: si déplacé/tourné par une mécanique ou une structure)
        return state.with(FACING, rotation.rotate(state.get(FACING))); // Calcule et applique la nouvelle direction après rotation
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) { // Gère l'inversion symétrique (effet miroir) du bloc
        return state.rotate(mirror.getRotation(state.get(FACING))); // Fait pivoter le bloc pour respecter la symétrie demandée
    }

    @Override
    protected net.minecraft.util.ActionResult onUse(BlockState state, World world, BlockPos pos, net.minecraft.entity.player.PlayerEntity player, net.minecraft.util.hit.BlockHitResult hit) { // S'exécute lorsque le joueur fait un clic droit sur notre bloc
        if (!world.isClient()) { // Exécute la logique uniquement côté serveur (pour éviter les désynchronisations d'interface)
            BlockEntity blockEntity = world.getBlockEntity(pos); // Récupère l'inventaire de la machine
            if (blockEntity instanceof MJ_AutoBreaker_BlockEntity autoBreakerEntity) { // Si c'est bien le nôtre
                player.openHandledScreen(autoBreakerEntity); // Demande à Minecraft d'ouvrir l'interface graphique (GUI) liée à cet inventaire pour le joueur
            }
        }
        return net.minecraft.util.ActionResult.SUCCESS; // Indique au jeu que l'action a réussi (bloque l'animation de coup de main ou la pose d'un autre bloc)
    }
}