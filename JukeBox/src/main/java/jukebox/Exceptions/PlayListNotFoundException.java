package jukebox.Exceptions;

public class PlayListNotFoundException extends RuntimeException{
    public PlayListNotFoundException(){
        super();
    }
    public PlayListNotFoundException(String msg){
        super(msg);
    }
}