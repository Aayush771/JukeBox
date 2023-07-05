package jukebox.appConfig;


import jukebox.Commands.*;
import jukebox.Repository.ActivePlayListRepository;
import jukebox.Repository.PlayListRepository;
import jukebox.Repository.SongRepository;
import jukebox.Repository.UserRepository;
import jukebox.Service.UserService;
import jukebox.Service.UserServiceImpl;

public class ApplicationConfig {

    private final UserRepository userRepository = new UserRepository();
    private final PlayListRepository playListRepository = new PlayListRepository();
    private final SongRepository songRepository = new SongRepository();
    private final ActivePlayListRepository activePlayListRepository = new ActivePlayListRepository();

    private final UserService userService = new UserServiceImpl(userRepository,playListRepository,songRepository,activePlayListRepository);

    private final LoadDetaCommand loadDetaCommand = new LoadDetaCommand(userService);
    private final CreateUserCommand createUserCommand = new CreateUserCommand(userService);
    private final CreatePlayListCommand createPlayListCommand = new CreatePlayListCommand(userService);
    private final DeletePlayListCommand deletePlayListCommand = new DeletePlayListCommand(userService);
    private final PlayPlayListCommand playListCommand = new PlayPlayListCommand(userService);
    private final ModifyPlayListCommand modifyPlayListCommand = new ModifyPlayListCommand(userService);
    private final PlaySongCommand playSongCommand = new PlaySongCommand(userService);
    private final CommandInvoker commandInvoker = new CommandInvoker();

    public CommandInvoker getCommandInvoker(){
        commandInvoker.register("LOAD-DATA",loadDetaCommand);
        commandInvoker.register("CREATE-USER",createUserCommand);
        commandInvoker.register("CREATE-PLAYLIST",createPlayListCommand);
        commandInvoker.register("DELETE-PLAYLIST",deletePlayListCommand);
        commandInvoker.register("PLAY-PLAYLIST",playListCommand);
        commandInvoker.register("MODIFY-PLAYLIST",modifyPlayListCommand);
        commandInvoker.register("PLAY-SONG",playSongCommand);

        return commandInvoker;
    }
}

