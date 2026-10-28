package CafeDBService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class Context {

    /*
	* exists() // checks if it exists in the context
	* isAccesible() //checks if its accesible
	* get() // gets if its accessible
	* importable() //returns if its importable
     */
    public static interface Item {

        public static enum AccessType {
            Public,
            Private
        }

        public Identifier getIdentifier();

        public AccessType getAccessType();

        public Optional<Item> exists(Identifier identifier);

        public Optional<Item> find(Identifier identifier);

        public static class Variable implements Item {

            Identifier identifier;
            AccessType accessType;
            Class<?> type;
            Object value;

            public Variable(Identifier identifier, AccessType accessType, Class<?> type, Object value) {
                assert type.isInstance(value);
                this.identifier = identifier;
                this.accessType = accessType;
                this.type = type;
                this.value = value;
            }

            @Override
            public Identifier getIdentifier() {
                return identifier;
            }

            @Override
            public AccessType getAccessType() {
                return accessType;
            }

            @Override
            public Optional<Item> exists(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    return Optional.of(this); 
                }else {
                    return Optional.empty();
                }
            }

            @Override
            public Optional<Item> find(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    if (accessType == AccessType.Public) {
                        return Optional.of(this);
                    }
                }
                return Optional.empty();
            }
        }

        public static class Type implements Item {

            Identifier identifier;
            AccessType accessType;
            Class<?> type;

            public Type(Identifier identifier, AccessType accessType, Class<?> type) {
                this.identifier = identifier;
                this.accessType = accessType;
                this.type = type;
            }

            @Override
            public Identifier getIdentifier() {
                return identifier;
            }

            @Override
            public AccessType getAccessType() {
                return accessType;
            }

            @Override
            public Optional<Item> exists(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    return Optional.of(this);
                }
                return Optional.empty();
            }

            @Override
            public Optional<Item> find(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    if (accessType == AccessType.Public) {
                        return Optional.of(this);
                    }
                }
                return Optional.empty();
            }
        }

        public static class Function implements Item {

            Identifier identifier;
            AccessType accessType;
			Task task;
            //Content content;

            public Function(Identifier identifier, AccessType accessType, Task task) {
                this.identifier = identifier;
                this.accessType = accessType;
				this.task = task;
            }

            @Override
            public Identifier getIdentifier() {
                return identifier;
            }

            @Override
            public AccessType getAccessType() {
                return accessType;
            }

            @Override
            public Optional<Item> exists(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    return Optional.of(this);
                }
                return Optional.empty();
                //return content.exists(identifier.popFirst());
            }

            @Override
            public Optional<Item> find(Identifier identifier) {
                if (identifier.identifier.isEmpty()) {
                    if (accessType == AccessType.Public) {
                        return Optional.of(this);
                    }
                }
                return Optional.empty();
            }
        }

        public static class StaticContext implements Item {

            Identifier identifier;
            AccessType accessType;
            Content content;
            Optional<StaticContext> parent;

            StaticContext(Identifier identifier){
                this.identifier = identifier;
            }

            @Override
            public Optional<Item> exists(Identifier toFind) {
                if (toFind.identifier.isEmpty()) {
                    return Optional.of(this);
                } else {
                    return content.exists(toFind.popFirst());
                    /*if (childs.containsKey(toFind.first())){
						return childs.get(toFind.first()).exists(toFind.popFirst());
					}*/
                    //return Optional.empty();
                }
            }

            @Override
            public Optional<Item> find(Identifier toFind) {
                if (accessType == AccessType.Public) {
                    if (toFind.identifier.isEmpty()) {
                        return Optional.of(this);
                    } else {
                        return content.exists(toFind.popFirst());
                    }
                }
				return Optional.empty();
            }

            @Override
            public Identifier getIdentifier() {
                return identifier;
            }

            @Override
            public AccessType getAccessType() {
                return accessType;
            }
        }
    }

    public static class Content {
        Identifier identifier;
        boolean isPublic;
        boolean areItemsImportable;
        Map<String, Item> items;
        List<Identifier> imports;//import a single item or a whole context

        //public Optional<

        public Content(Identifier identifier){
            this.identifier = identifier;
        }
        public Optional<Item> exists(Identifier toFind) {
            if (items.containsKey(toFind.first())) {
                Item item = items.get(toFind.first());
                toFind = toFind.popFirst();
                if (toFind.identifier.isEmpty()) {
                    return Optional.of(item); 
                }else {
                    return item.exists(toFind);
                }
            } else {
                return Optional.empty();
            }
        }

        public Identifier appendIdentifier(String name){
            Identifier newIdentifier = new Identifier(new ArrayList<>(identifier.identifier));
            newIdentifier.identifier.add(name);
            return newIdentifier;
        }

        public void declareVariable(String name, Class<?> type, Object value){
            assert type.isInstance(value);
            var variable = new Item.Variable(identifier.copyWith(name), Item.AccessType.Public, type, value);
            items.put(name, variable);
        }

        public Result<Result.EmptyOk> addItem(Identifier identifier, Item item){
            if (identifier.identifier.size() == 1){
                if (items.containsKey(identifier.last())){
                    return Result.error("item already exists");
                }else{
                    items.put(identifier.last(), item);
                    return Result.ok(null);
                }
            }else{
                if (items.containsKey(identifier.first())){
                    var foundItem = items.get(identifier.first());
                    if (Item.StaticContext.class.isInstance(foundItem)){
                        var context = (Item.StaticContext)foundItem;
                        context.content.addItem(identifier.popFirst(), item);
                        return Result.ok(null);
                    }else{
                        return Result.error(identifier.first() + " is not a module");
                    }
                }else{
                    return Result.error(identifier.first() + " not found");
                }
            }
        }
    }

    public static class CompileTimeContext{
        Item.StaticContext programContext;
        Identifier currentContext;
    }

    public static class RuntimeContext {

        Item.StaticContext programContext;
        Stack<Stack<Context.Content>> functionsContext;
        Identifier currentContext;

		private Optional<Item> findItem(Identifier identifier){
            var item_res = programContext.find(identifier);
            if (item_res.isPresent()) {
                return item_res;
            }
			while(true){
				var parent = currentContext.popLast();
				parent.identifier.addAll(identifier.identifier);
				item_res = programContext.find(parent);
				if (item_res.isPresent()){
					return item_res;
				}
			}			

		}
        private Optional<Task> findFunction(Identifier identifier) {
			var item = findItem(identifier);
			if (item.isEmpty())
				return Optional.empty();
			var function = (Item.Function)item.get();
			return Optional.of(function.task);
        }

        public Optional<Item.Variable> findObject(Identifier identifier) {
			var item = findItem(identifier);
			if (item.isEmpty())
				return Optional.empty();
			var variable = (Item.Variable)item.get();
			return Optional.of(variable);
        }

		public Optional<Item.Type> findType(Identifier identifier){
			var item = findItem(identifier);
			if (item.isEmpty())
				return Optional.empty();
			var type = (Item.Type)item.get();
			return Optional.of(type);
		}
    }

    public static class Identifier {

        List<String> identifier;

        public Identifier(String... args){
            this.identifier = List.of(args);
        }
        public Identifier(List<String> identifier) {
            this.identifier = identifier;
        }

        public Identifier copyWith(String name){
            var newIdentifier = new Identifier(identifier);
            newIdentifier.identifier.add(name);
            return newIdentifier;
        }

        public Identifier popFirst() {
            if (identifier.size() == 0) {
                return new Identifier(new ArrayList<>());
            }
            var popped = identifier.subList(1, identifier.size() - 1);
            return new Identifier(popped);
        }

        public String first() {
            return identifier.get(0);
        }

        public String last() {
            assert !identifier.isEmpty();
            return identifier.get(identifier.size() - 1);
        }

		public Identifier popLast(){
			assert !identifier.isEmpty();
            if (identifier.size() == 1) {
                return new Identifier(new ArrayList<>());
            }
            var popped = identifier.subList(0, identifier.size() - 2);
            return new Identifier(popped);
		}
    }

    public static class Import {

        List<List<String>> identifiers;
    }

    /*
    public static class GlobalContext {

        Optional<Function.Task> findFunction(String identifier, String module) {
            if (globalContext.functions.containsKey(identifier)) {
                return Optional.of(globalContext.functions.get(identifier));
            }
            for (var context : stackContext) {
                if (context.functions.containsKey(identifier)) {
                    return Optional.of(context.functions.get(identifier));
                }
            }
            List<String> imports = fileImports.get(module);
            for (String context : imports) {
                if (fileContext.get(context).functions.containsKey(identifier)) {
                    return Optional.of(fileContext.get(module).functions.get(identifier));
                }
            }
            return Optional.empty();
        }

    }
*/
    public static Result<String> readFile(String strPath) {
        try {
            Path path = Path.of(strPath);
            System.out.println(System.getProperty("user.dir"));
            if (!Files.exists(path)) {
                return Result.error("File: " + path + " not found");
            }
            return Result.ok(Files.readString(path));
        } catch (IOException e) {
            return Result.error("Failed to read file: " + strPath + ". Error: " + e.getMessage());
        }
    }

  
    @FunctionalInterface
    interface Task {
        Object run(List<Object> args, RuntimeContext context);
    }

    public static Item.StaticContext createGlobalContext() {
        Content content = new Content(new Identifier());
        
        //move this to a function
        Item.Function println = new Item.Function(content.appendIdentifier("println"), Item.AccessType.Public, (List<Object> args, RuntimeContext context) -> {
            if (args.size() != 1)
                throw new RuntimeException("Incorrect number of arguments");
            System.out.println(args.get(0));
            return null;
        });
        content.items.put(println.identifier.last(), println);

        Item.Function print = new Item.Function(content.appendIdentifier("print"), Item.AccessType.Public, (List<Object> args, RuntimeContext context) -> {
            if (args.size() != 1)
                throw new RuntimeException("Incorrect number of arguments");
            System.out.print(args.get(0));
            return null;
        });
        content.items.put(print.identifier.last(), print);

        Item.Function exec = new Item.Function(content.appendIdentifier("exec"), Item.AccessType.Public, (List<Object> args, RuntimeContext context) -> {
            System.out.println("executing exec: arg count: " + args.size());
            for (var arg : args) {
                System.out.print((String) arg + ' ');
            }
            System.out.println();
            String[] cmd = args.stream().map(String::valueOf).toArray(String[]::new);
            ProcessBuilder pb = new ProcessBuilder("cmd", "/c");
            pb.command().addAll(Arrays.asList(cmd));
            pb.redirectErrorStream(true);
            try {
                Process p = pb.start();
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                    String line;

                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }

                }

                int exitCode = p.waitFor();
                return output.toString();
            } catch (Exception e) {
                System.out.println("Failed to execute: " + e.getMessage());
                return null;
            }
        });

        content.items.put(exec.identifier.last(), exec);

        var import_func = new Item.Function(content.appendIdentifier("import"), Item.AccessType.Public, (List<Object> args, RuntimeContext context) -> {
            System.out.println(System.getProperty("user.dir"));
            Result<String> file = readFile((String) args.get(0));
            if (file.isError()) {
                throw new RuntimeException("Failed to read file" + file.getError());
            }
            var result = CafeDBService.parser(file.getValue());
            if (result.isError()) {
                System.out.println("Failed to parse");
                return null;
            }
            Context context = new Context();
            Program program = new Program(result.getValue());
            System.out.println("Successfully imported");
            return null;
        });
        content.items.put(import_func.identifier.last(), import_func);

        var int_type = new Item.Type(content.appendIdentifier("i32"), Item.AccessType.Public, Integer.class);
        var float_type = new Item.Type(content.appendIdentifier("f32"), Item.AccessType.Public, Float.class);
        var str_type = new Item.Type(content.appendIdentifier("str"), Item.AccessType.Public, String.class);
        var boolean_type = new Item.Type(content.appendIdentifier("bool"), Item.AccessType.Public, Boolean.class);
        var void_type = new Item.Type(content.appendIdentifier("void"), Item.AccessType.Public, Void.class);

        content.items.put(int_type.identifier.last(), int_type);
        content.items.put(float_type.identifier.last(), float_type);
        content.items.put(str_type.identifier.last(), str_type);
        content.items.put(boolean_type.identifier.last(), boolean_type);
        content.items.put(void_type.identifier.last(), void_type);

        Item.StaticContext globalContext = new Item.StaticContext(new Identifier());
        globalContext.content = content;
        return globalContext;
    }
/*
    void addChild(String identifier, Context context) {
        childContexts.put(identifier, context);
    }

    Object callFunction(String functionName, List<Object> args) {
        if (functions.containsKey(functionName)) {
            return functions.get(functionName).run(args);
        } else {
            System.out.println("function not found");
        }
        return null;
    }

    Result<Result.EmptyOk> declareVariable(String identifier, Class<?> type, Object value) {
        if (variables.containsKey(identifier)) {
            return Result.error("identifier in use");
        } else {
            if (!type.isInstance(value)) {
                return Result.error("Variable: " + identifier + " is not instance of " + type.toString());
            }
            variables.put(identifier, value);
            System.out.println("Variable` declared: [" + identifier + "] = " + value);
            return Result.ok(new Result.EmptyOk());
        }
    }

    Optional<Object> getVariable(String identifier) {
        System.out.println(variables);
        System.out.println("Searching variable: " + identifier);
        if (variables.containsKey(identifier)) {
            System.out.println("variable: [" + identifier + "] = " + variables.get(identifier));
            return Optional.of(variables.get(identifier));
        }
        return Optional.empty();
    }

    Result<Result.EmptyOk> reasignVariable(String identifier, Object value) {
        if (variables.containsKey(identifier)) {
            Class<?> variableType = variables.get(identifier).getClass();
            Class<?> valueType = value.getClass();
            if (!variables.get(identifier).getClass().isInstance(value)) {
                return Result.error("Value type missmatch: " + identifier + " is of type: " + variables.get(identifier).getClass().toString()
                        + ". Value: " + value.toString() + " is of type: " + value.getClass().toString());
            }
            variables.put(identifier, value);
            return Result.ok(new Result.EmptyOk());
        }
        return Result.error("Variable " + identifier + " not found");
    }

    Optional<Class<?>> getType(String type) {
        if (types.containsKey(type)) {
            return Optional.of(types.get(type));
        }
        return Optional.empty();
    }
    */
}
