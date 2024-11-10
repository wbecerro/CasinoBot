package casinoBot;

import casinoBot.listeners.ButtonInteractionListeners;
import casinoBot.listeners.SlashCommandListener;
import casinoBot.util.Utilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.Collections;

public class CasinoBot {

    private static Utilities utilities = new Utilities();

    public static void main(String[] args) {
        JDA jda = JDABuilder.createLight(utilities.getToken(), Collections.emptyList())
                .addEventListeners(new SlashCommandListener())
                .addEventListeners(new ButtonInteractionListeners())
                .build();

        CommandListUpdateAction commands = jda.updateCommands();

        commands.addCommands(
                Commands.slash("fichas", "Comando para gestionar las fichas")
                        .addSubcommands(new SubcommandData("añadir", "Añade fichas a un usuario")
                                .addOptions(new OptionData(OptionType.USER, "usuario",
                                        "El usuario al que añadir fichas", true),
                                        new OptionData(OptionType.INTEGER, "fichas",
                                                "Cantidad de fichas a añadir al usuario", true)),
                                new SubcommandData("quitar", "Quita fichas a un usuario")
                                        .addOptions(new OptionData(OptionType.USER, "usuario",
                                                "El usuario al que quitar fichas", true),
                                                new OptionData(OptionType.INTEGER, "fichas",
                                                        "Cantidad de fichas a quitar al usuario", true)),
                                new SubcommandData("ver", "Ver las fichas de un usuario")
                                        .addOptions(new OptionData(OptionType.USER, "usuario",
                                                "El usuario al que ver las fichas", true)))
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.DISABLED),
                Commands.slash("coinflip", "Prueba tu suerte tirando una moneda")
                        .addOptions(new OptionData(OptionType.INTEGER, "cara",
                                "La cara de la moneda por la que quieres apostar", true)
                                .addChoice("Cara", 0)
                                .addChoice("Cruz", 1),
                                new OptionData(OptionType.INTEGER, "fichas", "Cantidad de fichas a apostar", true)
                                        .setMinValue(5000)
                                        .setMaxValue(500000))
                        .setGuildOnly(true),
                Commands.slash("ruleta", "Prueba tu suerte en la ruleta")
                        .addOptions(new OptionData(OptionType.INTEGER, "color",
                                        "El color en el que crees que caerá la moneda", true)
                                        .addChoice("Rojo", 0)
                                        .addChoice("Negro", 1),
                                new OptionData(OptionType.INTEGER, "fichas", "Cantidad de fichas a apostar", true)
                                        .setMinValue(5000)
                                        .setMaxValue(500000))
                        .setGuildOnly(true),
                Commands.slash("slots", "Prueba tu suerte en las tragaperras")
                        .addOptions(new OptionData(OptionType.INTEGER, "fichas", "Cantidad de fichas a apostar", true)
                                .setMinValue(5000)
                                .setMaxValue(500000))
                        .setGuildOnly(true),
                Commands.slash("blackjack", "Prueba tu suerte en el blackjack")
                        .addOptions(new OptionData(OptionType.INTEGER, "fichas", "Cantidad de fichas a apostar", true)
                                .setMinValue(5000)
                                .setMaxValue(500000))
                        .setGuildOnly(true)
        );

        commands.addCommands(
                Commands.slash("cartera", "Comprueba tu cantidad de fichas")
                        .setGuildOnly(true)
                        .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
        );

        commands.queue();
    }
}