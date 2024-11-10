package casinoBot.listeners;

import casinoBot.util.Utilities;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ButtonInteractionListeners extends ListenerAdapter {

    private Utilities utilities = new Utilities();

    /**
     * Método para escuchar el evento de interactuar con un botón para el juego de blackjack.
     *
     * @param event Evento de interacción.
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String[] id = event.getComponentId().split(":");
        String type = id[0];
        String userId = id[1];
        if(!userId.equals(event.getUser().getId())) {
            return;
        }
        event.deferEdit().queue();
        List<String> userCards = new ArrayList<>(List.of(id[2].split(";")));
        List<String> crupierCards = new ArrayList<>(List.of(id[3].split(";")));
        String chips = id[4];

        int cardCount = utilities.getCardCount(userCards);

        switch(type) {
            case "card":
                // Sacamos la nueva carta y comprobamos si nos pasamos.
                String nextCard = utilities.getRandomCard();
                userCards.add(nextCard);
                int cardValue = Utilities.cards.get(nextCard);
                if(cardValue == 1 && cardCount + 11 <= 21) {
                    cardValue = 11;
                }
                cardCount += cardValue;

                StringBuilder cardList = new StringBuilder();
                StringBuilder cardButton = new StringBuilder();
                for(String card : userCards) {
                    cardList.append(card).append("\n");
                    cardButton.append(card).append(";");
                }
                String cardsButton = cardButton.substring(0, cardButton.length() - 1);

                StringBuilder crupierCardList = new StringBuilder();
                StringBuilder crupierCardButton = new StringBuilder();
                for(String card : crupierCards) {
                    crupierCardList.append(card).append("\n");
                    crupierCardButton.append(card).append(";");
                }
                String crupierCardsButton = crupierCardButton.substring(0, crupierCardButton.length() - 1);

                if(cardCount > 21) {
                    // Si nos pasamos le decimos que ha perdido.
                    EmbedBuilder loseEmbed = new EmbedBuilder();
                    loseEmbed.setTitle("Blackjack");
                    loseEmbed.setDescription("Has superado la puntuación de **21** con **" + cardCount +
                            "** puntos por lo que has perdido. ¡Más suerte la próxima vez!");
                    loseEmbed.setColor(Color.RED);
                    loseEmbed.addField("Cartas del crupier", crupierCardList.toString(), true);
                    loseEmbed.addBlankField(true);
                    loseEmbed.addField("Tus cartas", cardList.toString(), true);
                    utilities.removeBalance(userId, Integer.parseInt(chips));
                    event.getHook().editOriginalEmbeds(loseEmbed.build()).setComponents().queue();
                    return;
                }

                // Si no nos hemos pasado seguimos dando la oportunidad de pedir más cartas o plantarse.
                EmbedBuilder infoEmbed = new EmbedBuilder();
                infoEmbed.setTitle("Blackjack");
                infoEmbed.setDescription("__Turno de **" + event.getUser().getName() + "**__");
                infoEmbed.setColor(new Color(49, 226, 120));
                infoEmbed.addField("Cartas del crupier", crupierCardList.toString(), true);
                infoEmbed.addBlankField(true);
                infoEmbed.addField("Cartas de **" + event.getUser().getName() + "**", cardList + "\nTotal: " + cardCount, true);
                String idCard = "card:" + event.getUser().getId() + ":" + cardsButton + ":" + crupierCardsButton + ":" + chips;
                String idGiveUp = "giveup:" + event.getUser().getId() + ":" + cardsButton + ":" + crupierCardsButton + ":" + chips;
                event.getHook().editOriginalEmbeds(infoEmbed.build())
                        .setActionRow(
                                Button.success(idCard, "Pedir carta"),
                                Button.danger(idGiveUp, "Plantarse")
                        ).queue();
                break;
            case "giveup":
                EmbedBuilder crupierTurnEmbed = new EmbedBuilder();
                crupierTurnEmbed.setTitle("Blackjack");
                String hiddenCard = utilities.getRandomCard();
                crupierCards.set(1, hiddenCard);
                int crupierCount = utilities.getCardCount(crupierCards);
                if(crupierCount >= 17) {
                    if(crupierCount > cardCount) {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has perdido. ¡Más suerte la próxima vez!");
                        crupierTurnEmbed.setColor(Color.RED);
                        utilities.removeBalance(userId, Integer.parseInt(chips));
                    } else if(crupierCount == cardCount) {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has empatado. ¡Más suerte la próxima vez!");
                        crupierTurnEmbed.setColor(Color.YELLOW);
                    } else {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has ganado. ¡Enhorabuena!");
                        crupierTurnEmbed.setColor(Color.GREEN);
                        utilities.addBalance(userId, Integer.parseInt(chips) * utilities.winMultiplier);
                    }
                } else {
                    while(crupierCount < 17) {
                        String newCard = utilities.getRandomCard();
                        crupierCards.add(newCard);
                        crupierCount = utilities.getCardCount(crupierCards);
                    }

                    if(crupierCount > 21) {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has ganado. ¡Enhorabuena!");
                        crupierTurnEmbed.setColor(Color.GREEN);
                        utilities.addBalance(userId, Integer.parseInt(chips) * utilities.winMultiplier);
                    } else if(crupierCount > cardCount) {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has perdido. ¡Más suerte la próxima vez!");
                        crupierTurnEmbed.setColor(Color.RED);
                        utilities.removeBalance(userId, Integer.parseInt(chips));
                    } else if(crupierCount == cardCount) {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has empatado. ¡Más suerte la próxima vez!");
                        crupierTurnEmbed.setColor(Color.YELLOW);
                    } else {
                        crupierTurnEmbed.setDescription("La puntuación del crupier es de **" + crupierCount + "** y la de **" +
                                event.getUser().getName() + "** es de **" + cardCount + "** por lo que has ganado. ¡Enhorabuena!");
                        crupierTurnEmbed.setColor(Color.GREEN);
                        utilities.addBalance(userId, Integer.parseInt(chips) * utilities.winMultiplier);
                    }
                }

                crupierCardList = new StringBuilder();
                for(String card : crupierCards) {
                    crupierCardList.append(card).append("\n");
                }

                crupierTurnEmbed.addField("Cartas del crupier", crupierCardList.toString(), true);
                crupierTurnEmbed.addBlankField(true);

                cardList = new StringBuilder();
                for(String card : userCards) {
                    cardList.append(card).append("\n");
                }

                crupierTurnEmbed.addField("Cartas de **" + event.getUser().getName() + "**", cardList.toString(), true);
                event.getHook().editOriginalEmbeds(crupierTurnEmbed.build()).setComponents().queue();
                break;
        }
    }
}
