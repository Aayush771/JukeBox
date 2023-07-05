package jukebox.Exceptions;

public class SongNotFoundException extends RuntimeException{
    public SongNotFoundException(){
        super();
    };
    public SongNotFoundException(String msg){
        super(msg);
    };
}
