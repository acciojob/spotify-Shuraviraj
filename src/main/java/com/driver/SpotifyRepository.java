package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository(){
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        if (!creatorPlaylistMap.containsKey(user)) {
            creatorPlaylistMap.put(user, null);
        }
        if (!userPlaylistMap.containsKey(user)) {
            userPlaylistMap.put(user, new ArrayList<>());
        }
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        if (!artistAlbumMap.containsKey(artist)) {
            artistAlbumMap.put(artist, new ArrayList<>());
        }
        return artist;
    }

    public Album createAlbum(String title, String artistName) {
        Artist artist = artists.stream()
                .filter(a -> a.getName().equals(artistName)).findFirst().orElse(null);
        if (artist == null){
            artist = createArtist(artistName);
        }
        Album album = new Album(title);
        albums.add(album);
        if(!albumSongMap.containsKey(album)){
            albumSongMap.put(album, new ArrayList<>());
        }
        artistAlbumMap.get(artist).add(album);
        return album;
    }

    public Song createSong(String title, String albumName, int length) throws Exception{
        Album album = albums.stream()
                .filter(a -> a.getTitle().equals(albumName))
                .findFirst()
                .orElseThrow(()->new Exception("Album does not exist"));

        Song song = new Song(title, length);
        songs.add(song);
        if(!songLikeMap.containsKey(song)){
            songLikeMap.put(song, new ArrayList<>());
        }
        albumSongMap.get(album).add(song);
        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = users.stream()
                .filter(a -> a.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("Album does not exist"));
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        if (!playlistListenerMap.containsKey(playlist)) {
            playlistListenerMap.put(playlist, new ArrayList<>());
        }
        if (!playlistSongMap.containsKey(playlist)) {
            playlistSongMap.put(playlist, new ArrayList<>());
        }
        songs.stream()
                .filter(a -> a.getLength() == length)
                .forEach(a -> playlistSongMap.get(playlist).add(a));
        creatorPlaylistMap.put(user, playlist);
        playlistListenerMap.get(playlist).add(user);
        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = users.stream()
                .filter(a -> a.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        Playlist playlist = new Playlist(title);
        playlists.add(playlist);
        if (!playlistListenerMap.containsKey(playlist)) {
            playlistListenerMap.put(playlist, new ArrayList<>());
        }
        if (!playlistSongMap.containsKey(playlist)) {
            playlistSongMap.put(playlist, new ArrayList<>());
        }
        songs.stream()
                .filter(a -> songTitles.contains(a.getTitle()))
                .forEach(a -> playlistSongMap.get(playlist).add(a));
        creatorPlaylistMap.put(user, playlist);
        playlistListenerMap.get(playlist).add(user);
        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        Playlist playlist = playlists.stream()
                .filter(a->a.getTitle().equals(playlistTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Playlist does not exist"));

        User user = users.stream()
                .filter(a -> a.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));

        playlistListenerMap.get(playlist).add(user);
        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = users.stream()
                .filter(a -> a.getMobile().equals(mobile))
                .findFirst()
                .orElseThrow(() -> new Exception("User does not exist"));
        Song song = songs.stream()
                .filter(a -> a.getTitle().equals(songTitle))
                .findFirst()
                .orElseThrow(() -> new Exception("Song does not exist"));
        song.setLikes(song.getLikes()+1);
        if (!songLikeMap.get(song).contains(user)) {
            songLikeMap.get(song).add(user);
        }
        Album album = null;
        for(var a:albumSongMap.keySet() ){
            if (albumSongMap.get(a).contains(song)){
                album = a;
                for(Artist art : artistAlbumMap.keySet()){
                    if (artistAlbumMap.get(art).contains(album)){
                        art.setLikes(art.getLikes()+1);
                    }
                }
            }
        }
        for(Artist a : artistAlbumMap.keySet()){
            if (artistAlbumMap.get(a).contains(album)){
                a.setLikes(a.getLikes()+1);
                break;
            }
        }
        return song;
    }

    public String mostPopularArtist() {
        int like = -1;
        Artist artist = null;
        for (Artist a : artists) {
            if (a.getLikes() > like) {
                like = a.getLikes();
                artist = a;
            }
         }

        return artist.getName();
    }

    public String mostPopularSong() {
        int like = -1;
        Song song = null;
        for (Song s : songs) {
            if (s.getLikes() > like) {
                like = s.getLikes();
                song = s;
            }
        }
        return song.getTitle();
    }
}
