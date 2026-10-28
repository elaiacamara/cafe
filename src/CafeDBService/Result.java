package CafeDBService;

import java.util.Optional;

public class Result<T> {
	public static class EmptyOk{}
	public static class Error{
		String location;
		String errorMessage;
		Optional<Error> childError;
	
		public Error(String location, String errorMessage, Optional<Error> childError) {
			this.location = location;
			this.errorMessage = errorMessage;
			this.childError = childError;
		}
		
		public Error error(String message) {
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		    String location = caller.getFileName() + ":" + caller.getLineNumber();
			var err = new Error(location, message, Optional.of(this));
			return err;
		}
		
		public static Error make_error(String message) {
			StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		    String location = caller.getFileName() + ":" + caller.getLineNumber();
			var err = new Error(location, message, Optional.empty());
			return err;
		}
	}
	
	
	private Optional<Error> error;
	private Optional<T> value;
	
	public static <T> Result<T> ok(T value) {
		return new Result<T>(value);
	}
	public static <T> Result<T> error(String message){
		return new Result<T>(Error.make_error(message));
	}
	
	public Result(T value) {
		this.error = Optional.empty();
		this.value = Optional.of(value);
	}
	public Result(Error error) {
		this.error = Optional.of(error);
		this.value = Optional.empty();
	}
	
	public boolean isOk() {
		assert error.isEmpty() != value.isEmpty() : "missconfigured Result";
		return value.isPresent();
	}
	public boolean isError() {
		assert error.isEmpty() != value.isEmpty() : "missconfigured Result";
		return error.isPresent();
	}
	public Error getError() {
		return error.get();
	}
	public T getValue() {
		return value.get();
	}
}
