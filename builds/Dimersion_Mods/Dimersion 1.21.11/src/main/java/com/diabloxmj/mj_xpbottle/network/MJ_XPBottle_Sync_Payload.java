package com.diabloxmj.mj_xpbottle.network;


import net.minecraft.network.PacketByteBuf;

import com.diabloxmj.mj_xpbottle.MJ_XPBottle_Enter; // Importe la classe maîtresse pour l'accès à la méthode d'ID
import net.minecraft.network.RegistryByteBuf; // Importe le tampon de flux d'octets optimisé pour le registre de Minecraft
import net.minecraft.network.codec.PacketCodec; // Importe le traducteur (Codec) chargé de convertir le code en octets
import net.minecraft.network.packet.CustomPayload; // Importe l'interface mère des paquets personnalisés de Minecraft

// Structure Record : Crée automatiquement un conteneur de données immuable avec ses variables de capacité maximale
public record MJ_XPBottle_Sync_Payload(
        int lvl1Max, int lvl2Max, int lvl3Max, int lvl4Max, int lvl5Max, int lvl6Max
) implements CustomPayload { // Implémente CustomPayload pour être reconnu par les tuyaux réseau de Minecraft

    // Génère l'adresse réseau unique de ce paquet pour que le client sache l'identifier (MJ_XPBottle_Enter:config_sync)
    public static final Id<MJ_XPBottle_Sync_Payload> ID = new Id<>(MJ_XPBottle_Enter.id("config_sync"));

    // LE CODEC : C'est le dictionnaire de traduction. Il dicte dans quel ordre exact écrire et lire les données sur le réseau
    public static final PacketCodec<RegistryByteBuf, MJ_XPBottle_Sync_Payload> CODEC = PacketCodec.tuple(
            // Écrit/Lit la capacité de la petite fiole sous forme d'un entier (Int) sur le réseau
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl1Max,
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl2Max,
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl3Max,
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl4Max,
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl5Max,
            PacketCodec.of((value, buf) -> buf.writeInt(value), PacketByteBuf::readInt), MJ_XPBottle_Sync_Payload::lvl6Max,
            // Constructeur de référence : Rassemble toutes les valeurs lues pour recréer l'objet côté Client
            MJ_XPBottle_Sync_Payload::new
    );

    // Méthode requise par l'interface pour renvoyer l'identifiant d'aiguillage de ce paquet précis
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID; // Renvoie l'ID (MJ_XPBottle_Enter:config_sync)
    }
}