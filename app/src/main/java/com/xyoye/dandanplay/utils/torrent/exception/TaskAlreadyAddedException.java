package com.xyoye.dandanplay.utils.torrent.exception;

public class TaskAlreadyAddedException extends Exception
{
    public TaskAlreadyAddedException() { }

    public TaskAlreadyAddedException(String message)
    {
        super(message);
    }
}
