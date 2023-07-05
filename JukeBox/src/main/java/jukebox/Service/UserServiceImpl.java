package jukebox.Service;

import jukebox.Entities.*;
import jukebox.Exceptions.PlayListNotFoundException;
import jukebox.Exceptions.SongNotFoundException;
import jukebox.Exceptions.UserNotFoundException;
import jukebox.Repository.ActivePlayListRepository;
import jukebox.Repository.PlayListRepository;
import jukebox.Repository.SongRepository;
import jukebox.Repository.UserRepository;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PlayListRepository playListRepository;
    private final SongRepository songRepository;
    private final ActivePlayListRepository activePlayListRepository;
    private Integer artistIncrement = 0;

    public UserServiceImpl(UserRepository userRepository, PlayListRepository playListRepository,
                           SongRepository songRepository,ActivePlayListRepository activePlayListRepository) {
        this.userRepository = userRepository;
        this.playListRepository = playListRepository;
        this.songRepository = songRepository;
        this.activePlayListRepository = activePlayListRepository;
    }

    @Override
    public String loadData(String inputFile) throws IOException {
        String line = "";
        BufferedReader br = new BufferedReader(new FileReader(inputFile));
        while ((line = br.readLine()) != null) // returns a Boolean value
        {
            String[] value = line.split(",");
            String[] str = value[5].split("#");
            List<Artist> artistList = new ArrayList<>();
            for (String s : str) {
                artistIncrement++;
                artistList.add(new Artist(artistIncrement, s));
            }
            songRepository.save(
                    new Song(Integer.parseInt(value[0]), value[1], value[2], value[3], artistList));

        }
        // System.out.println(songMap);
        br.close();
        return "Songs Loaded successfully";
    }

    @Override
    public String createUser(String name) {
        User user = userRepository.save(new User(null, name));
        return user.getId() + " " + user.getName();
    }

    @Override
    public String createPlayList(Integer userId, String name, List<Integer> songIds) {
        PlayList playList = null;
        Optional<User> ouser = userRepository.findById(userId);
        if (ouser.isPresent()) {
            for (Integer i : songIds) {
                Song song = songRepository.findById(i).orElseThrow(()-> new SongNotFoundException("Song Not Found With This UserId " + i));
            }

            PlayList playList1 = new PlayList(userId, null, name, songIds);
            playList = playListRepository.save(playList1);
        } else
            throw new UserNotFoundException("User Not Found with this userId " + userId);
        return "Playlist ID - " + playList.getPlayListId();
    }

    public String deletePlayList(Integer userId, Integer playListId) {
        PlayList playList = playListRepository.findById(playListId)
                .orElseThrow(() -> new PlayListNotFoundException(
                        "No PlayList Found with this PlayList Id " + playListId));

        playListRepository.delete(playList);
        return "Delete Successful";
    }


    @Override
    public String playPlayList(Integer userId, Integer playListId) {
        PlayList playList = playListRepository.findById(playListId).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+playListId));
        userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No User Found with this UserId "+userId));
        List<Integer> songList = playList.getSongList();
        if(songList.size() == 0) throw new PlayListNotFoundException("Playlist is empty.");
        Optional<Song> songs = songRepository.findById(songList.get(0));
        Song song = songs.get();
        activePlayListRepository.save(new ActivePlayList(userId,playListId,song.getId()));
        List<Artist> artist = song.getArtists();
        StringBuilder sb = new StringBuilder();
        for(Artist i:artist){
            sb.append(i.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Current Song Playing\r\n"+"Song - "+song.getName()+"\r\nAlbum - "+ song.getAlbumName()+"\r\nArtists - "+sb;

    }
    @Override
    public String addSong(Integer userId,Integer playListId,List<Integer> songIds){
        PlayList playList = playListRepository.findById(playListId).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+playListId));
        userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No User Found with this UserId "+userId));
        List<Integer> songList = playList.getSongList();
        for(Integer i:songIds){
            songRepository.findById(i).orElseThrow(()-> new SongNotFoundException("Some Requested Songs Not Available. Please try again"));
            if(!songList.contains(i)) songList.add(i);
        }
        playList.setSongList(songList);
        playListRepository.save(playList);
        StringBuilder sb = new StringBuilder();
        for(Integer i:songList){
            sb.append(i).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Playlist ID - "+playListId+"\r\nPlaylist Name - "+playList.getName()+"\r\nSong IDs - "+sb;

    }
    @Override
    public String deleteSong(Integer userId,Integer playListId,List<Integer> songIds){
        PlayList playList = playListRepository.findById(playListId).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+playListId));
        userRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No User Found with this UserId "+userId));
        List<Integer> songList = playList.getSongList();
        for(Integer i:songIds){
            songRepository.findById(i).orElseThrow(()-> new SongNotFoundException("Some Requested Songs Not Available. Please try again"));
            if(songList.contains(i)) songList.remove(i);
            else throw new SongNotFoundException("Some Requested Songs for Deletion are not present in the playlist. Please try again.");
        }
        playList.setSongList(songList);
        playListRepository.save(playList);
        StringBuilder sb = new StringBuilder();
        for(Integer i:songList){
            sb.append(i).append(" ");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Playlist ID - "+playListId+"\r\nPlaylist Name - "+playList.getName()+"\r\nSong IDs - "+sb;

    }

    @Override
    public String playSong(Integer userId, Integer songNo) {
        // TODO Auto-generated method stub
        ActivePlayList activePlayList = activePlayListRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No user found withd this userId "+userId));
        PlayList playList = playListRepository.findById(activePlayList.getPlayListId()).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+activePlayList.getPlayListId()));
        List<Integer> songList = playList.getSongList();

        Song song = null;
        if(songList.contains(songNo)){
            Optional<Song> optSong = songRepository.findById(songNo);
            song = optSong.get();
        }else throw new SongNotFoundException("Given song id is not a part of the active playlist");
        activePlayList.setSongId(songNo);
        activePlayListRepository.save(activePlayList);
        List<Artist> artist = song.getArtists();
        StringBuilder sb = new StringBuilder();
        for(Artist i:artist){
            sb.append(i.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Current Song Playing\r\n"+"Song - "+song.getName()+"\r\nAlbum - "+ song.getAlbumName()+"\r\nArtists - "+sb;
    }

    @Override
    public String playSongNext(Integer userId) {
        ActivePlayList activePlayList = activePlayListRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No user found withd this userId "+userId));
        PlayList playList = playListRepository.findById(activePlayList.getPlayListId()).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+activePlayList.getPlayListId()));
        List<Integer> songList = playList.getSongList();
        int currSong = songList.indexOf(activePlayList.getSongId());
        Integer nextSong = songList.get((currSong+1)%songList.size());
        activePlayList.setSongId(nextSong);
        Optional<Song> optSong = songRepository.findById(nextSong);
        Song song = optSong.get();
        activePlayListRepository.save(activePlayList);
        List<Artist> artist = song.getArtists();
        StringBuilder sb = new StringBuilder();
        for(Artist i:artist){
            sb.append(i.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Current Song Playing\r\n"+"Song - "+song.getName()+"\r\nAlbum - "+ song.getAlbumName()+"\r\nArtists - "+sb;
    }

    @Override
    public String playSongBack(Integer userId) {
        ActivePlayList activePlayList = activePlayListRepository.findById(userId).orElseThrow(()-> new UserNotFoundException("No user found withd this userId "+userId));
        PlayList playList = playListRepository.findById(activePlayList.getPlayListId()).orElseThrow(()-> new PlayListNotFoundException("No PlayList Found With this Given Id "+activePlayList.getPlayListId()));
        List<Integer> songList = playList.getSongList();
        int currSong = songList.indexOf(activePlayList.getSongId());
        Integer nextSong = 0;
        if(currSong == 0){
            nextSong = songList.get(songList.size()-1);
        }else nextSong = songList.get((currSong-1)%songList.size());
        activePlayList.setSongId(nextSong);
        Optional<Song> optSong = songRepository.findById(nextSong);
        Song song = optSong.get();
        activePlayListRepository.save(activePlayList);
        List<Artist> artist = song.getArtists();
        StringBuilder sb = new StringBuilder();
        for(Artist i:artist){
            sb.append(i.getName()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return "Current Song Playing\r\n"+"Song - "+song.getName()+"\r\nAlbum - "+ song.getAlbumName()+"\r\nArtists - "+sb;
    }

}
