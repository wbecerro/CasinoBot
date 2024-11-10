package casinoBot.listeners;

import casinoBot.util.Utilities;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SlashCommandListener extends ListenerAdapter {

    private Utilities utilities = new Utilities();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch(event.getName()) {
            case "cartera":
                User user = event.getUser();
                utilities.seeBalance(user, event);
                break;
            case "fichas":
                switch(event.getSubcommandName()) {
                    case "ver":
                        user = event.getOption("usuario").getAsUser();
                        utilities.seeBalance(user, event);
                        break;
                    case "añadir":
                        user = event.getOption("usuario").getAsUser();
                        int chips = event.getOption("fichas").getAsInt();
                        utilities.addBalance(user, chips, event);
                        break;
                    case "quitar":
                        user = event.getOption("usuario").getAsUser();
                        chips = event.getOption("fichas").getAsInt();
                        utilities.removeBalance(user, chips, event);
                        break;
                }
                break;
            case "coinflip":
                utilities.coinflip(event.getOption("cara").getAsInt(), event.getOption("fichas").getAsInt(), event);
                break;
            case "ruleta":
                utilities.roulette(event.getOption("color").getAsInt(), event.getOption("fichas").getAsInt(), event);
                break;
            case "slots":
                utilities.slots(event.getOption("fichas").getAsInt(), event);
                break;
            case "blackjack":
                utilities.blackjack(event.getOption("fichas").getAsInt(), event);
                break;
            default:
                return;
        }
    }
}
