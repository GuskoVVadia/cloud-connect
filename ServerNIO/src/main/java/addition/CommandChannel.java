package addition;

/**
 * Тригеры работы с каналом у сервера.
 */

public enum CommandChannel {
    LISTFILES,
    SYNCHFILES,
    INFILES,    //в этом состоянии клиент отправляет файл, сервер - принимает
    OUTFILES,
    EXIT,
    IDLE,
    READCHANNEL,
    WRITECHANNEL,
    IDENTIFI,
    START,
    PATHNONE,
    ERROR;

}

