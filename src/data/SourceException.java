package data;

public class SourceException extends Exception{
    public SourceException(String message, Throwable innerExecption){
        super(message, innerExecption);
    }
}
