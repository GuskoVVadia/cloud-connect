/**
 * Тригеры работы с каналом у сервера.
 */
package CloudShare;

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
    DISCONNECT;
}

