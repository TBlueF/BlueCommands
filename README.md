## Usage

### Adding to project
```kotlin
repositories {
    maven ( "https://jitpack.io" )
}

dependencies {
    implementation("com.github.TBlueF.BlueCommands:bluecommands-core:v1.0.0")
    implementation("com.github.TBlueF.BlueCommands:bluecommands-brigadier:v1.0.0") // Optional
}
```

### Creating and executing Commands
```java
class MyCommands {
    
    private Command<CommandSender, Object> commands;
    
    public MyCommands() {
        BlueCommands<CommandSender> commandFactory = new BlueCommands<>();
        this.commands = commandFactory.createCommand(this);
    }
    
    public void runCommand(CommandSender sender, String input) {
        
        // parse the command
        ParseResult<CommandSender, Object> parseResult = this.commands.parse(sender, new InputReader(input));
        
        // get the match with the highest priority
        ParseMatch<CommandSender, Object> match = parseResult.getMatches().stream()
                .max(Comparator.comparing(ParseMatch::getPriority))
                .orElse(null);
        
        if (match == null) {
            // handle no command found
            return;
        }

        // execute the command (the result is whatever the command-method returns)
        Object executionResult = match.execute();
        
    }
    
    @Command("foo <arg1> [optionalArg2]")
    public void fooCommand(
          CommandSender sender,
          @Argument("arg1") String stringArgument,
          @Argument("optionalArg2") Double optionalDoubleArgument
    ) {
        // do something
    }
  
}
```

### Creating custom argument-parsers
Example here for creating an argument parser which accepts online Bukkit-Players: 
```java
public class PlayerArgument<S> extends SimpleArgumentParser<S, Player> {

    public PlayerArgument() {
        super(false, false);
    }

    @Override
    public Player parse(S context, String name) throws CommandParseException {
        Player player = Bukkit.getPlayerExact(name);
        if (player == null) {
            throw new CommandParseException("'" + name + "' is not an online player!");
        }
        return player;
    }

    @Override
    public List<Suggestion> suggest(S context, InputReader input) {
        return Bukkit.getOnlinePlayers().stream()
                .map(player -> (Suggestion) new SimpleSuggestion(player.getName()))
                .toList();
    }

}
```
Then register the new Parser before creating your commands:
```java
BlueCommands<CommandSender> commandFactory = new BlueCommands<>();

// register the custom argument-parser
commandFactory.setArgumentParserForArgumentType(Player.class, new PlayerArgument<>());

this.commands = commandFactory.createCommand(this);
```

### Context-Resolver
With context-resolvers you can add other parameters to your command-method that should be resolved from the context.
```java
BlueCommands<CommandSender> commandFactory = new BlueCommands<>();
commandFactory.setContextResolverForType(Server.class, CommandSender::getServer);
```
Then in a command you can add a `Server` parameter which will be filled based on the current command-context:
```java
@Command("foo <arg1> [optionalArg2]")
public void fooCommand(
      Server server,
      @Argument("arg1") String stringArgument,
      @Argument("optionalArg2") Double optionalDoubleArgument
) {
    // ...
}
```

### Context-Predicates (e.g. Permissions)
Here an example to create a Permission annotation and use it to validate commands for a context.

Create the new Annotation:
```java 
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    String value();
}
```
Label your commands with this new annotation:
```java
@Command("foo <arg1> [optionalArg2]")
@Permission("foo.bar.bazz")
public void fooCommand(
      CommandSender sender,
      @Argument("arg1") String stringArgument,
      @Argument("optionalArg2") Double optionalDoubleArgument
) {
    // ...
}
```
Register a context-predicate for this Annotation:
```java
BlueCommands<CommandSender> commandFactory = new BlueCommands<>();
commandFactory.setAnnotationContextPredicate(Permission.class, (permission, commandSender) -> {
    return commandSender.hasPermission(permission.value());
});
```
When the annotation is present, the command will now only be available if the registered predicate returns `true`.

### Merging commands
You can merge multiple commands into one. E.g. if you have multiple objects that hold command-methods:
```java
BlueCommands<CommandSender> commandFactory = new BlueCommands<>();
Command<CommandSender, Object> root = new Command<>();
root.tryMerge(commandFactory.createCommand(object1));
root.tryMerge(commandFactory.createCommand(object2));
root.tryMerge(commandFactory.createCommand(object3));
```
