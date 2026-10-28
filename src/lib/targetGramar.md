# Grammar

This is how i would design my language, the current program is nowhere close.
Most of the syntax is borrowed from rust.

## Goals
Simple reliable code.
All code that can run at compile time will run at compile time.


### Language Types
i32
f32
str
bool

### Containers
List
Map
Set

### Utility

Result
Optional


## Syntax

### Declaration

```
let a: i32 = 10; //immutable a variable
let a = 10; //type assertion
let mut a = 10; //mutable variable
let cmp a = 10; //must be known at compile time
```

### Function declaration
```
fn hello_world(){
    print("hello world");
}

fn add(a: i32, b: i32) -> i32{
    return a + b;
}
```

Without return statement
```
fn add(a: i32, b: i32) -> i32{
    a + b
}
```
one line
```
fn add(a: i32, b: i32) -> i32 = a + b;
```
infer return type in one line
```
fn add(a: i32, b: i32) => a + b;

```
#### Anonymous functions and functions as parameters
Anonymous functions
```
fn main(){
    let callable = (a: i32, b: 32) -> i32 = a + b; 
    let callable_block = (a: i32, b: 32) -> i32{
        a + b;
    }
    let callable_infer = (a: i32, b: 32) => a + b;

    let with_type : (i32,i32) -> i32 = (a: i32, b: 32) => a + b;

    let names: List(str) = get_names();
    let names_maped: List(str) = names.iter()
        .map((name : str) => name + "_mapped";)
        .collect(List);
}
```

Function as argument
```
fn operator(a: i32, b: i32, operation: (i32, i32) -> i32) -> i32{
    operation(a, b)
}
```


## Enum

```
enum State{
    Started,
    Running,
    Paused,
    Finished
}

enum Source{
    Link(url)
    File(filepath)
    None
}
```

## Struct

```
struct Person{
    age: i32,
    name: i32,
    alive: bool
}
```

### Templated struct

Templeted types are just functions that return a type and run at compile time

```
struct CustomArray(T: type){
    data: List(T),
    custom_value: i32
}

fn main(){
    let names = CustomList(str)::new();
}
```

### Struct methods

Still deciding on syntax

```
struct CustomArray(T: type){
    data: List(T),
    initial_size: i32
}

impl CustomArray(T: type){
    pub fn new(initial_size: i32){
        let data = List(T)::new(initial_size);
        return CustomArray(T){
            .data = data,
            .initial_size = initial_size;
        }
    }

}
```

New is a static method because it has no self parameter.
All struct members must be initialized between the {}.
Member functions are made by passing self.

```
impl CustomArray(T: type){
    pub fn debug(self: &Self){
        for item in self.data{
            print(item);
        }
    }

    pub fn add(self: &mut Self, item: T){
        self.data.add(item);
    }
}

```
### Structs with State

A combination between a struct and a enum
```
struct Server <SetUp | Running | Paused | Finished <Success | Crashed> >{
    pub name: str,
    pub port: i32,

    Running | Clients {
        socket: i32,
        clients: mut Map(ID, Client),
        request_queue: mut List(Request)
    }

    Finished<Error> error : str;
}
```

We can declare the subset of states a struct can be in.
Members declared without a specifier can be accessed at any state.
A struct instance must always be in one state.
Protects developers from half initialized structs, keeps data that is not meant to be used in that moment hidden.
All struct member variables exists as if in a normal struct, their access and visibility is simply reduced.
Can easily create state machines

#### Functions access and state transitions

We initialize with a static new function and must return the struct in whichever state we wish.
First instance of a struct can be at any state

```
impl Server{
    pub fn new(name: str, port: i32) -> Result(Server<SetUp>, str){
        if port < 0 || port > 1000{
            return Err("Port must be in range [1..=1000]");
        }
        Ok(Server<SetUp>{
            .name = name,
            .port = port
        })
    }
}
```
You only need to initialize the variables that have newly be exposed when entering that specific state, in this case only name and port

The state of a struct can only be changed in a member mutable function.
Still deciding syntax
```
impl Server{
    pub fn start(self: mut Self<SetUp>) -> Result(()){
        //pseudo code
        let socket = open_socket(self.port)?;
        let mut clients = Map(ID, Clients)::new();
        let mut requests = List(Request)::new();
        
        self enter Self<Running>{
            .clients = clients,
            .requests = requests,
            .socket = socket
        };
        //from this point onwards self is of type Self<Running>
    }

    pub fn handle_connections(self: mut Self<Running>){
        //pseudo code
        let new_connections: List(Channel) = socket.new_connections();
        let new_clients = new_connections.iter()
            .map((channel: Channel) => Client::new(channel))
            .collect(List);

        let new_messages: List(str) = socket.read_all();
        let new_requests = new_messages.iter()
            .map((message: str) => Request::new(message))
            .collect(List);
        
        self.clients.add_list(new_clients);
        self.requests.add_list(new_requests);

        let result = function_that_can_fail();
        if result.is_error(){
            self enter Self<Finished<Crashed>>{
                .error = "failed at something"
            };
            return;
        }
    }

    pub fn status(self: Self<Running | Paused>){
        print("Port {self.port}");
        print("Name {self.name}");
        print("Client count {self.clients.size()});
        print("Request backlog count {self.requests.size()});
    }

    pub fn pause(self: mut Self<Running>){
        //stop handling connections
        self enter Self<Paused>;
    }

    pub fn resume(self: mut Self<Paused>){
        //resume connection handling
        self enter Self<Running>;
    }

    pub fn terminate(self: mut Self<Paused | Running>){
        for client in self.clients{
            client.close_connection();
        }
        self.socket.close();
        self enter Self<Finished<Success>>;
    }
}
```

Member functions can be in multiple states in which case only access to members shared by all will be visible, or a match statement can be done to know which the current state actually is.

Errors can be handled with result or an internal error state.

While returning a simple Result is the clearest and most readable, binding the critical error to its state allows for uniform state machines


## CONTROL FLOW

#### Watch
Can be used on structs with states to create state machines

```
fn main(){
    let mut server = Server::new("Simple server", 5000)?;
    let finished = watch mut server{
        Server<Setup>(set_up) => set_up.start();,
        Server<Running>(running) => {
            loop{
                server.handle_connections();
                server.status();
                
                if some_reason{
                    server.pause();
                }

                if exit_condition{
                    server.terminate();
                }
            }
        }
        Server<Paused>(paused) => {
            while !can_resume{
                //wait
            }
            paused.resume();
        }
        Server<Finished<_>>(finished) => yield finished;
    };

    //type of finished is Server<Finished<Success | Crashed>>

    match finished{
        Server<Finished<Success>>(success) => {
            print("Successfully terminated");
        },
        Server<Finished<Crashed>>(crash) => {
            print("Server crashed, reason: {crash.error}");
        }
    }
}
```
The watch control flow receives a variable that will match, upon completion it will once again do a match indefinitely in a loop.
All cases must be handled within flow.

The loop breaks when a branch yields a value.

This method would give an error at any unhandled case meaning adding a new state would trigger compilations errors every time.

We can then match the final possible states to know how it ended.


#### Loop

An infite loop
```
loop{
    print("Looping");
}
```

#### While

```
while condition{
    print("Looping");
    if contition_to_end{
        break;
    }
}
```

#### For

There is no c style (i = 0; i< 10; i++)
Only with ranges

```
let names: List(str) = get_names();

for name in names{
    print(name);
}

for i in [0..10]{
    print(i);
}

for i in [0..=10]{
    print(i);
}

for (i, name) in names.enumerate(){
    print("{i} = {name}");
}
```

All loops can be assigned a name with the keyword as to allow for nested break continue and yield

```
let mut entities: List(Entity);
let result: Result(()) = loop as GameLoop{
    for entity in entities{
        if let Err(err) = entity.update(){
            yield GameLoop(err);
        }
    }
}

```

## Contracts or checks

Allow to make rules and be applied to types or parameters both at runtime and compile time.


#### Basic NotZero
We use th to create rules
They must return a Result
```
th NonZero(num: i32) -> Result(i32){
    if num == 0{
        Err("Num must be non zero")
    }
    Ok(num)
}
```
We can then apply it to the parameter of a function

```
fn div(a: i32, b: NonZero i32) -> Result(i32){
    Ok(a / b)
}
```

The th NonZero will run before any code of the function is executed and it will early return any errors if encountered. The final variable b will just be of type i32 inside the function.
This means that div must be Result of something, forced by the rule.

We can force this check to be done at compile time by adding cmp
```
fn div(a: i32, b: cmp NonZero i32) -> i32{
    a / b
}
```
Note that NonZero doesn't need to change, just adding cmp will force the compiler to know the value of b at compile time. If it cannot be known that same error will instead propagate during compilation and be presented as a compile time error.

Because the error happens at compile time we no longer need the Result for the function

Multiple th can be combined
And a th can have its own parameters ruled, making composition possible

```
th NonZero(num: i32){
    if num == 0{
        Err("Must be non Zero")
    }
    Ok(num)
}

th BiggerThan0(num: NonZero i32){
    if num < 0{
        Err("Must be positive")
    }
    Ok(num)
}

th MultipleOf5(num: BiggerThan0 i32){
    if num % 5 != 0{
        return Err("Must be multiple of 5")
    }
    Ok(num)
}

```

th can have their own parameters

```
th MultipleOf(num: BiggerThan0 i32, multiple: i32){
    if num % multiple != 0{
        Err("Must be multiple of {multiple}");
    }
    Ok(num)
}

fn print_number(num: cmp MultipleOf(5) i32){
    print("num is multiple of 5");
}
```

this could also be applied to the return value
```
th Positive(num: i32){
    if num < 0{
        Err("Number must be positive");
    }
    Ok(num)
}
fn random_positive() -> Result(Positive i32){
    let random_num : i32 = std::random::next_int();
    Ok(random_num)
}
```
In this case it will check at the end and turn the Ok into err
This is subject to change
