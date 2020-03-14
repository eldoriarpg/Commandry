# Commandry

Commandry is a command library, which uses annotations and allows subcommands.

## Project Structure

The [core module](commandry-core) contains the code required to use the library.
In the future it is planned to support common APIs/Tools/Programs like Spigot or
JDA *directly*, with simple integrations.

## Usage

If you want to use the core library, you can add it to your project as a maven repository.
Currently, you'll need to install it to your local `.m2` directory using `mvn install`.

```xml
<dependency>
    <groupId>de.eldoria</groupId>
    <artifactId>commandry-core</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

Now you are ready to use it. To create a new command, simply create a class like this:

```java
public class MyCommandClass {
    
    @Command("greet") // 1
    public void greetSomeone(String who) { // 2
        System.out.printf("Hello %s!\n", who); // 3
    }
}
```
(1) Define a command by adding this annotation with the command name you want to use.

(2) Create a public method, its name isn't relevant. As you see here, it takes one parameter.
This is the argument you need to provide when calling the command

(3) Define what should happen when calling the command.

The resulting command in this case would be `greet <who>`, so calling `greet World` would
print out the line `Hello World!`.

### But how to *call* the command now?

First, you need to create a new instance of `Commandry`. This can simply be done with
```java
public class Main {
    public static void main(String[] args){
      Commandry<Void> commandry = new Commandry<>();
      commandry.registerCommand(MyCommandClass.class);
    }
}
```
`Void` is the type of context you want to accept. You'll see later on, what that means.
Now, you can run a command by calling `commandry.runCommand(null, "greet World");`.

### Context passing
If you want to use metadata in your command methods like who called the command, you can use
context passing. Therefore, you can replace `Void` with the type you want to have as context.
So, let's try that out:
```java
// our context class
public class Person {
    private String name;
    
    public Person(String name) {
        this.name = name;
    }
}
// our modified main class
public class Main {
    
    public static void main(String[] args){
      Commandry<Person> commandry = new Commandry<>();
      commandry.registerCommand(MyCommandClass.class);
    }
}
// our modified command class
public class MyCommandClass {
    
    @Command("greet")
    public void greetSomeone(Person greeter, String who) {
        System.out.printf("%s says: 'Hello %s!'\n", greeter.getName(), who);
    }
}
```

Now, running the command will require a valid `Person` object:
```java
Person person = new Person("SirYwell");
commandry.runCommand(person, "greet World");
```
The person object will be provided to your method, and calling the command would print the line
`SirYwell say: 'Hello World!'` now.

**Note**: You don't need the context parameter, but if you have one, you need to provide a valid
instance of it when calling `runCommand(...)`.

**Limitations**: 
- The context parameter must be the first, if you have one.
- If you have a context type which can be parsed too, you need to specify it as first parameter in your methods,
even if you don't need it. Otherwise, command execution may fail.

### Further features

#### Aliases
Using the `@Alias` annotation, you can specify aliases for your command.
If you want to have multiple aliases, separate them by comma.
Example:
```java
public class MyCommandClass {
    
    @Command("greet")
    @Alias("welcome,wlcm")
    public void greetSomeone(String who) {
        System.out.printf("Hello %s!\n", who);
    }
}
```
Now, `"greet World"` and `"welcome World"` will have the same result when being executed.

#### Async command execution
If you want to have your commands being executed asynchronous, simply wrap your `Commandry` instance
with `AsyncCommandry`.

#### Parsing
In most cases, string arguments aren't everything you want. You may want to use integers,
booleans and enum values in your command method.



