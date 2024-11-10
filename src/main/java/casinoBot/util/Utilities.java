package casinoBot.util;

import casinoBot.CasinoBot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utilities {

    private final static ArrayList<Integer> reds = new ArrayList<>(Arrays.asList(1,3,5,7,9,12,14,16,18,19,21,23,25,27,30,32,34,36));
    private final int redsSize = reds.size();
    private final static ArrayList<Integer> blacks = new ArrayList<>(Arrays.asList(2,4,6,8,10,11,13,15,17,20,22,24,26,28,29,31,33,35));
    private final int blacksSize = blacks.size();
    private final static ArrayList<String> slots = new ArrayList<>(Arrays.asList("🍎", "🍐", "🍊", "🍌", "🍉", "🍇", "🍓", "🍒", "🍑", "🥭", "`🍍", "🥥", "🥝", "🍋"));
    private final int slotsSize = slots.size();
    public final static Map<String, Integer> cards = Stream.of(new Object[][] {
            { "As", 1 },
            { "Dos", 2 },
            { "Tres", 3 },
            { "Cuatro", 4 },
            { "Cinco", 5 },
            { "Seis", 6 },
            { "Siete", 7 },
            { "Ocho", 8 },
            { "Nueve", 9 },
            { "Diez", 10 },
            { "Jota", 10 },
            { "Reina", 10 },
            { "Rey", 10 },
    }).collect(Collectors.toMap(data -> (String) data[0], data -> (Integer) data[1]));
    private final int cardsSize = cards.size();
    private final Logger logger = Logger.getLogger("Casino.Bot");
    private final int winChance = 30;
    public final int winMultiplier = 2;

    /**
     * Método que consigue el token del bot desde un archivo externo por asuntos de seguridad.
     *
     * @return el token del bot en forma de String.
     */
    public String getToken() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(CasinoBot.class.getClassLoader().getResource("token.json"));
            String token = node.get("token").asText();
            return token;
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "Ha ocurrido un error al leer el token del bot: ", exception);
            System.exit(1);
        }

        return null;
    }

    /**
     * Método para añadir a los usuarios a los archivos de datos si no lo están ya.
     *
     * @param id El identificador único del usuario en forma de String.
     * @return true si se ha insertado correctamente o false en caso contrario.
     */
    public boolean addUserToData(String id) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("chips", 0);
            objectMapper.writeValue(new File("src/main/resources/users/" + id + ".json"), jsonNode);
            return true;
        } catch(IOException exception) {
            logger.log(Level.SEVERE, "Ha ocurrido un error al registrar al usuario en la base de datos: ", exception);
            return false;
        }
    }

    /**
     * Método para saber si un usuario está añadido a los archivos de datos.
     *
     * @param id El identificador único del usuario en forma de String.
     * @return true si ya está añadido o false en caso contrario.
     */
    private boolean isUserAdded(String id) {
        File file = new File("src/main/resources/users/" + id + ".json");
        return file.isFile();
    }

    /**
     * Método para conseguir las fichas de un usuario.
     *
     * @param id El identificador único del usuario en forma de String.
     * @return la cantidad de fichas del jugador o bien -1 en caso de error.
     */
    private int getBalance(String id) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode node = mapper.readTree(new File("src/main/resources/users/" + id + ".json"));
            int chips = node.get("chips").asInt();
            return chips;
        } catch (IOException exception) {
            logger.log(Level.WARNING, "Ha ocurrido un error al leer el saldo del usuario: ", exception);
        }

        return -1;
    }

    /**
     * Método para quitar fichas a un usuario.
     *
     * @param id El identificador único del usuario en forma de String.
     * @param chips Cantidad de fichas a quitar al usuario.
     * @return true si se han quitado las fichas correctamente o false en caso de error.
     */
    public boolean removeBalance(String id, int chips) {
        try {
            int balance = getBalance(id);
            if(balance < 0) {
                return false;
            }
            balance = balance - chips;
            if(balance < 0) {
                balance = 0;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("chips", balance);
            objectMapper.writeValue(new File("src/main/resources/users/" + id + ".json"), jsonNode);
            return true;
        } catch(IOException exception) {
            return false;
        }
    }

    /**
     * Método para añadir fichas a un usuario.
     *
     * @param id El identificador único del usuario en forma de String.
     * @param chips Cantidad de fichas a añadir al usuario.
     * @return true si se han añadido las fichas correctamente o false en caso de error.
     */
    public boolean addBalance(String id, int chips) {
        try {
            int balance = getBalance(id);
            if(balance < 0) {
                return false;
            }
            ObjectMapper objectMapper = new ObjectMapper();
            ObjectNode jsonNode = objectMapper.createObjectNode();
            jsonNode.put("chips", balance + chips);
            objectMapper.writeValue(new File("src/main/resources/users/" + id + ".json"), jsonNode);
            return true;
        } catch(IOException exception) {
            return false;
        }
    }

    /**
     * Método para mostrar las fichas a los usuarios mediante embeds.
     *
     * @param user Usuario del que mostrar las fichas.
     * @param event Evento de comando.
     */
    public void seeBalance(User user, SlashCommandInteractionEvent event) {
        if(!isUserAdded(user.getId())) {
            if(!addUserToData(user.getId())) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Error", null);
                embedBuilder.setColor(Color.RED);
                embedBuilder.setDescription("Ha ocurrido un error al recuperar tu saldo, contacta con un administrador.");
                event.replyEmbeds(embedBuilder.build()).queue();
                return;
            }
        }

        int balance = getBalance(user.getId());

        if(balance == -1) {
            EmbedBuilder embedBuilder = new EmbedBuilder();
            embedBuilder.setTitle("Error", null);
            embedBuilder.setColor(Color.RED);
            embedBuilder.setDescription("Ha ocurrido un error al recuperar tu saldo, contacta con un administrador.");
            event.replyEmbeds(embedBuilder.build()).queue();
            return;
        }

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Cartera de " + user.getName(), null);
        embedBuilder.setColor(new Color(49, 226, 120));
        embedBuilder.setDescription("💳 || **Saldo:** " + balance + " fichas");
        embedBuilder.setFooter("Para realizar un retiro por favor abra ticket, recuerde contar con su Nombre_Apellido #HASH en el aplicativo.");
        event.replyEmbeds(embedBuilder.build()).queue();
    }

    /**
     * Método para mostrar el añadido de fichas a los usuarios.
     *
     * @param user Usuario al que añadir las fichas.
     * @param chips Cantidad de fichas a añadir.
     * @param event Evento de comando.
     */
    public void addBalance(User user, int chips, SlashCommandInteractionEvent event) {
        boolean ok = addBalance(user.getId(), chips);
        if(ok) {
            event.reply("Se han añadido " + chips + " fichas a " + user.getName() + ".").queue();
            return;
        }
        event.reply("Ha ocurrido un error al añadir fichas al usuario.").queue();
    }

    /**
     * Método para mostrar la resta de fichas a los usuarios.
     *
     * @param user Usuario al que quitar fichas.
     * @param chips Cantidad de fichas a quitar.
     * @param event Evento de comando.
     */
    public void removeBalance(User user, int chips, SlashCommandInteractionEvent event) {
        boolean ok = removeBalance(user.getId(), chips);
        if(ok) {
            event.reply("Se han quitado " + chips + " fichas a " + user.getName() + ".").queue();
            return;
        }
        event.reply("Ha ocurrido un error al quitar fichas al usuario.").queue();
    }

    /**
     * Método para hacer todos los cálculos necesarios para el coinflip y mostrar los resultados.
     *
     * @param side Cara de la monead por la que se apuesta (0 = Cara, 1 = Cruz).
     * @param chips Cantidad de fichas a apostar.
     * @param event Evento de comando.
     */
    public void coinflip(int side, int chips, SlashCommandInteractionEvent event) {
        int userBalance = getBalance(event.getUser().getId());
        if(userBalance < chips) {
            EmbedBuilder notEnoughEmbed = new EmbedBuilder();
            notEnoughEmbed.setTitle("Error");
            notEnoughEmbed.setColor(Color.RED);
            notEnoughEmbed.setDescription("No tienes suficientes fichas como para apostar esa cantidad.");
            event.replyEmbeds(notEnoughEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String coinflip;
        if(side == 0) {
            coinflip = "cara";
        } else {
            coinflip = "cruz";
        }

        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("Coinflip");
        infoEmbed.setColor(new Color(49, 226, 120));
        infoEmbed.setDescription(event.getUser().getName() + " ha lanzado una moneda apostando **" + chips +
                " fichas** a que sale **" + coinflip + "**.");

        Random random = new Random();
        int coin = random.nextInt(100);
        EmbedBuilder randomEmbed = new EmbedBuilder();
        randomEmbed.setTitle("Lanzando moneda...");
        randomEmbed.setColor(Color.YELLOW);
        randomEmbed.setDescription("Se acaba de lanzar la moneda, espera el resultado con ansia.");
        randomEmbed.setImage("https://i.ibb.co/v4zc4R5/gif.gif");
        // Caso ganador
        if(coin < winChance) {
            EmbedBuilder winEmbed = new EmbedBuilder();
            winEmbed.setTitle("Resultado del coinflip");
            winEmbed.setColor(Color.GREEN);
            winEmbed.setDescription("🎉📈 ¡Enhorabuena " + event.getUser().getName() + "! Ha salido **" + coinflip +
                    "** y has ganado **" + chips * winMultiplier + " fichas**.");
            addBalance(event.getUser().getId(), chips * winMultiplier);
            event.replyEmbeds(infoEmbed.build(), randomEmbed.build(), winEmbed.build()).queue();
            return;
        }

        // Caso perdedor
        EmbedBuilder loseEmbed = new EmbedBuilder();
        loseEmbed.setTitle("Resultado del coinflip");
        loseEmbed.setColor(Color.RED);
        loseEmbed.setDescription("😢📉 ¡Qúe pena " + event.getUser().getName() + "! Ha salido **" + coinflip +
                "** y has perdido **" + chips + " fichas**.");
        removeBalance(event.getUser().getId(), chips);
        event.replyEmbeds(infoEmbed.build(), randomEmbed.build(), loseEmbed.build()).queue();
    }

    /**
     * Método para hacer todos los cálculos necesarios para la ruleta y mostrar los resultados.
     *
     * @param color Color de la casilla por la que se apuesta (0 = Rojo, 1 = Negro).
     * @param chips Cantidad de fichas a apostar.
     * @param event Evento de comando.
     */
    public void roulette(int color, int chips, SlashCommandInteractionEvent event) {
        int userBalance = getBalance(event.getUser().getId());
        if(userBalance < chips) {
            EmbedBuilder notEnoughEmbed = new EmbedBuilder();
            notEnoughEmbed.setTitle("Error");
            notEnoughEmbed.setColor(Color.RED);
            notEnoughEmbed.setDescription("No tienes suficientes fichas como para apostar esa cantidad.");
            event.replyEmbeds(notEnoughEmbed.build()).setEphemeral(true).queue();
            return;
        }

        String ballColor;
        if(color == 0) {
            ballColor = "rojo";
        } else {
            ballColor = "negro";
        }

        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("Ruleta");
        infoEmbed.setColor(new Color(49, 226, 120));
        infoEmbed.setDescription(event.getUser().getName() + " ha apostado **" + chips + " fichas** a que la bola cae en **" + ballColor + "**.");

        Random random = new Random();
        int ballLocation = random.nextInt(100);
        EmbedBuilder randomEmbed = new EmbedBuilder();
        randomEmbed.setTitle("Ruleta girando...");
        randomEmbed.setColor(Color.YELLOW);
        randomEmbed.setDescription("La ruleta ha comenzado a girar. ¿Qué saldrá?");
        randomEmbed.setImage("https://cdn.discordapp.com/attachments/1302713729443696640/1304266192256761938/giphy-1--unscreen.gif?ex=6730be4c&is=672f6ccc&hm=8b179c5a908a53c2b552980894085dd2763b41627e3759b5efa95a6c5c41ba2e&");
        // Caso ganador
        if(ballLocation < winChance) {
            int location;
            if(color == 0) {
                location = reds.get(random.nextInt(redsSize));
            } else {
                location = blacks.get(random.nextInt(blacksSize));
            }
            EmbedBuilder winEmbed = new EmbedBuilder();
            winEmbed.setTitle("Resultado de la ruleta");
            winEmbed.setColor(Color.GREEN);
            winEmbed.setDescription("🎉📈 ¡Enhorabuena " + event.getUser().getName() + "! La bola ha caído en **" +
                    location + "(" + ballColor + ")** y has ganado **" + chips * winMultiplier + " fichas**.");
            addBalance(event.getUser().getId(), chips * winMultiplier);
            event.replyEmbeds(infoEmbed.build(), randomEmbed.build(), winEmbed.build()).queue();
            return;
        }

        // Caso perdedor
        if(color == 0) {
            ballColor = "negro";
        } else {
            ballColor = "rojo";
        }

        int location;
        if(color == 0) {
            location = blacks.get(random.nextInt(redsSize));
        } else {
            location = reds.get(random.nextInt(blacksSize));
        }
        EmbedBuilder loseEmbed = new EmbedBuilder();
        loseEmbed.setTitle("Resultado de la ruleta");
        loseEmbed.setColor(Color.RED);
        loseEmbed.setDescription("😢📉 ¡Qúe pena " + event.getUser().getName() + "! La bola ha caído en **" +
                location + "(" + ballColor + ")** y has perdido **" + chips + " fichas**.");
        removeBalance(event.getUser().getId(), chips);
        event.replyEmbeds(infoEmbed.build(), randomEmbed.build(), loseEmbed.build()).queue();
    }

    /**
     * Método para conseguir las figuras que han salido en la tragaperras.
     *
     * @param equal Cantidad de figuras iguales que deben salir.
     * @return las figuras que han salido.
     */
    private String getRandomSlots(int equal) {
        Random random = new Random();
        ArrayList<String> figures = new ArrayList<>();
        String figure = slots.get(random.nextInt(slotsSize));
        for(int i=0;i<equal;i++) {
            figures.add(figure);
        }

        while(figures.size() != 3) {
            figure = slots.get(random.nextInt(slotsSize));
            if(!figures.contains(figure)) {
                figures.add(figure);
            }
        }

        Collections.shuffle(figures);
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : figures) {
            stringBuilder.append(string);
        }
        return stringBuilder.toString();
    }

    /**
     * Método para hacer todos los cálculos necesarios para las tragaperras y mostrar los resultados.
     *
     * @param chips Cantidad de fichas a apostar.
     * @param event Evento de comando.
     */
    public void slots(int chips, SlashCommandInteractionEvent event) {
        int userBalance = getBalance(event.getUser().getId());
        if(userBalance < chips) {
            EmbedBuilder notEnoughEmbed = new EmbedBuilder();
            notEnoughEmbed.setTitle("Error");
            notEnoughEmbed.setColor(Color.RED);
            notEnoughEmbed.setDescription("No tienes suficientes fichas como para apostar esa cantidad.");
            event.replyEmbeds(notEnoughEmbed.build()).setEphemeral(true).queue();
            return;
        }

        Random random = new Random();
        int slot = random.nextInt(100);
        EmbedBuilder randomEmbed = new EmbedBuilder();
        randomEmbed.setTitle("Slots girando...");
        randomEmbed.setColor(Color.YELLOW);
        randomEmbed.setDescription("Los slots están girando. Si salen 2 iguales ganarás un x2, si salen 3 iguales ganarás un x4.");
        randomEmbed.setImage("https://media.discordapp.net/attachments/1302713729443696640/1304260011442044990/giphy.gif?ex=6730b88a&is=672f670a&hm=f1d4dcc3143228b839024d1263a287cc3bc380d4e001abbc27e5fae354a5fd2f&=");
        // Caso ganador
        if(slot < winChance) {
            int newWinMultiplier = 2;
            int figures = 2;
            // Caso ganador
            if(random.nextInt(100) < winChance) {
                figures = 3;
                newWinMultiplier = 4;
            }

            String slotFigures = getRandomSlots(figures);
            EmbedBuilder winEmbed = new EmbedBuilder();
            winEmbed.setTitle("Resultado de los slots");
            winEmbed.setColor(Color.GREEN);
            winEmbed.setDescription("🎉📈 ¡Enhorabuena " + event.getUser().getName() + "! Han salido las figuras " +
                    slotFigures + " y has ganado **" + chips * newWinMultiplier + " fichas**.");
            addBalance(event.getUser().getId(), chips * newWinMultiplier);
            event.replyEmbeds(randomEmbed.build(), winEmbed.build()).queue();
            return;
        }

        // Caso perdedor
        String slotFigures = getRandomSlots(0);
        EmbedBuilder loseEmbed = new EmbedBuilder();
        loseEmbed.setTitle("Resultado de los slots");
        loseEmbed.setColor(Color.RED);
        loseEmbed.setDescription("😢📉 ¡Qúe pena " + event.getUser().getName() + "! Han salido las figuras " +
                slotFigures + " y has perdido **" + chips + " fichas**.");
        removeBalance(event.getUser().getId(), chips);
        event.replyEmbeds(randomEmbed.build(), loseEmbed.build()).queue();
    }

    /**
     * Método para hacer todos los cálculos necesarios para el blackjack y mostrar los resultados.
     *
     * @param chips Cantidad de fichas a apostar.
     * @param event Evento de comando.
     */
    public void blackjack(int chips, SlashCommandInteractionEvent event) {
        EmbedBuilder infoEmbed = new EmbedBuilder();
        infoEmbed.setTitle("Blackjack");
        infoEmbed.setDescription("__Turno de **" + event.getUser().getName() + "**__");
        infoEmbed.setColor(new Color(49, 226, 120));
        String crupierCard = getRandomCard();
        infoEmbed.addField("Cartas del crupier", crupierCard + "\nOculta", true);
        infoEmbed.addBlankField(true);
        String userCard1 = getRandomCard();
        String userCard2 = getRandomCard();
        String cardList = userCard1 + "\n" + userCard2 + "\nTotal: " + (cards.get(userCard1) + cards.get(userCard2));
        infoEmbed.addField("Cartas de **" + event.getUser().getName() + "**", cardList, true);
        String id = "card:" + event.getUser().getId() + ":" + userCard1 + ";" + userCard2 + ":" + crupierCard + ";Oculta:" + chips;
        String idGiveUp = "giveup:" + event.getUser().getId() + ":" + userCard1 + ";" + userCard2 + ":" + crupierCard + ";Oculta:" + chips;
        event.replyEmbeds(infoEmbed.build())
                .addActionRow(
                        Button.success(id, "Pedir carta"),
                        Button.danger(idGiveUp, "Plantarse")
                ).queue();
    }

    /**
     * Método para conseguir una carta aleatoria.
     *
     * @return valor de la carta escogida.
     */
    public String getRandomCard() {
        Random random = new Random();
        int randomNumber = random.nextInt(cardsSize);
        Set<String> keys = cards.keySet();
        int iteration = 0;
        for(String key : keys) {
            if(iteration == randomNumber) {
                return key;
            }
            iteration++;
        }

        return "";
    }

    /**
     * Método para contar el valor de las cartas.
     *
     * @param cardArray Array con las cartas.
     * @return valor del conteo de las cartas.
     */
    public int getCardCount(List<String> cardArray) {
        int count = 0;
        for(String card : cardArray) {
            int cardValue = cards.get(card);
            if(cardValue == 1 && count + 11 <= 21) {
                cardValue = 11;
            }
            count += cardValue;
        }

        return count;
    }
}
