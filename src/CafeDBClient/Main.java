package CafeDBClient;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			CafeDBClient client = new CafeDBClient(8080);
			client.run();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
	}

}
