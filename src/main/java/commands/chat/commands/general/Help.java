package commands.chat.commands.general;
/*
    Created by nils on 03.01.2018 at 22:03.
    
    (c) nils 2018
*/

import commands.chat.core.Chat;
import commands.chat.core.ChatCommand;
import commands.chat.tools.Message;
import net.dv8tion.jda.client.entities.Group;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

public class Help implements ChatCommand{
    @Override
    public boolean execute(MessageReceivedEvent event, String full, String cmd, String[] args) {
        return cmd.equals("help") || cmd.equals("?");
    }

    @Override
    public void action(MessageReceivedEvent event, String full, String cmd, String[] args) {
        EmbedBuilder builder = Message.INFO(event, "Look at my **[documentation](https://magnet.strukteon.me/documentation)**!");

        builder .addField("FUN",
                        commandDesc("poke", "poke a member to wake him up", 0) +
                                commandDesc("tts", "let this bot send a tts message", 0)
                        , false)

                .addField("GENERAL",
                        commandDesc("bio", "set a bio that will be shown in your profile", 0) +
                                commandDesc("help", "shows this little helpy message", 0) +
                                commandDesc("invite", "gives you the invite link for this bot", 0) +
                                commandDesc("profile", "shows your/someones profile", 0) +
                                commandDesc("server", "gives you some informations about this server", 0) +
                                commandDesc("whoami", "gives you some informations about you", 0) +
                                commandDesc("whois", "gives you some informations about an user", 0)
                        , false)

                /*.addField("MONEY",
                        commandDesc("slots", "spin that machine and win money", 0)
                        , false)*/

                .addField("MUSIC",
                        commandDesc("connect", "connects this bot to with a voicechannel", 3) +
                                commandDesc("info", "shows info about the playing track", 0) +
                                commandDesc("pause", "pauses the media playback", 0) +
                                commandDesc("play", "play a track from an URL or youtube", 0) +
                                commandDesc("playlist", "play a playlist from an URL or youtube", 0) +
                                commandDesc("resume", "resumes the media playback", 0) +
                                commandDesc("skip", "skips the current track", 0) +
                                commandDesc("stop", "stops the media playback", 1) +
                                commandDesc("volume", "changes the volume of the media playback", 0)
                        , true)

                .addField("TESTING",
                        commandDesc("error", "shows a custom error message", 0) +
                                commandDesc("ping", "gives you the connection ping from the bot to discord", 0)
                        , true)

        ;

        event.getTextChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public String premiumPermission() {
        return null;
    }

    private String commandDesc(String command, String desc, int permissionLevel){
        return "**" + command + "** - " + desc + " - *[" + Chat.permLevel(permissionLevel).toUpperCase() + "|Lv." + permissionLevel + "]*\n";
    }

    @Override
    public int permissionLevel() {
        return 0;
    }
}
