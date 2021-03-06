package audio;

import audio.youtube.YouTubeAPI;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import commands.chat.tools.Message;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.managers.AudioManager;
import utils.Static;

import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AudioCore extends ListenerAdapter {

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    public AudioCore() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public boolean connectToVoiceChannel(AudioManager audioManager, VoiceChannel voiceChannel) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            if (audioManager.getGuild().getVoiceChannels().contains(voiceChannel)){
                audioManager.openAudioConnection(voiceChannel);
                return true;
            }
        }
        return false;
    }


    public void load(MessageReceivedEvent event, String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                play(event, musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                playPlaylist(event, musicManager, playlist);
            }

            @Override
            public void noMatches() {
                event.getTextChannel().sendMessage(noMatchesMessage(event, trackUrl)).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                event.getTextChannel().sendMessage(loadFailedMessage(event, trackUrl, exception)).queue();
            }
        });
    }

    private MessageEmbed noMatchesMessage(MessageReceivedEvent event, String trackUrl){
        return Message.ERROR(event, "No matches were found for: " + trackUrl).build();
    }

    private MessageEmbed loadFailedMessage(MessageReceivedEvent event, String trackUrl, FriendlyException exception){
        return Message.ERROR(event, "Could not play: " + exception.getMessage()).build();
    }

    private MessageEmbed skipTrackMessage(MessageReceivedEvent event){
        return Message.INFO(event, "Skipped to next track.").build();
    }


    public void play(MessageReceivedEvent event, GuildMusicManager musicManager, AudioTrack track) {
        if (event.getMember().getVoiceState().getChannel() == null)
            event.getTextChannel().sendMessage(Message.ERROR(event, "You have to be connected to a VoiceChannel!").build()).queue();
        else {
            if(connectToVoiceChannel(event.getGuild().getAudioManager(), event.getMember().getVoiceState().getChannel()) || event.getGuild().getAudioManager().isConnected())
                musicManager.scheduler.queue(track, event);
        }
    }

    public void playPlaylist(MessageReceivedEvent event, GuildMusicManager musicManager, AudioPlaylist playlist) {
        if (event.getMember().getVoiceState().getChannel() == null)
            event.getTextChannel().sendMessage(Message.ERROR(event, "You have to be connected to a VoiceChannel!").build()).queue();
        else {
            if(connectToVoiceChannel(event.getGuild().getAudioManager(), event.getMember().getVoiceState().getChannel()) || event.getGuild().getAudioManager().isConnected())
                musicManager.scheduler.queuePlaylist(playlist, event);
        }
    }

    public void skipTrack(MessageReceivedEvent event) {
        GuildMusicManager musicManager = getGuildAudioPlayer(event.getGuild());
        event.getTextChannel().sendMessage(skipTrackMessage(event)).queue();

        if (!musicManager.scheduler.nextTrack(false))
            if (event.getGuild().getAudioManager().isConnected())
                event.getGuild().getAudioManager().closeAudioConnection();
    }


    public boolean stopPlaying(Guild guild){
        if (guild.getAudioManager().isConnected()){
            GuildMusicManager manager = getGuildAudioPlayer(guild);

            manager.scheduler.stop();
            manager.scheduler.purgeQueue();
            guild.getAudioManager().closeAudioConnection();
            return true;
        } else {
            return false;
        }
    }




}
