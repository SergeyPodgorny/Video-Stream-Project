package ru.streamer.exceptions;

public class ReadFileSystemException extends RuntimeException {

    public ReadFileSystemException(Throwable cause) {
        super("Ошибка чтения файловой системы", cause);
    }

    public ReadFileSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
