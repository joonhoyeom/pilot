import java.util.Scanner;

public class Main {

	
	public static void main(String[] args) {
		Communicator communicator = new Communicator();
		
		communicator.communicatorRun();
		
		System.out.println("If you want to shut down, press \'y\'");    	
    	Scanner s = new Scanner(System.in);
    	String str;
    	do { 
    		str = s.next(); 
    	} while ( "y".equals(str) == false );
		
    	communicator.setStop();
    	try { Thread.sleep(300); } catch (InterruptedException e1) {}
    	
    	if(communicator.isAlive()){
    		System.out.println("Communicator threads are still running, it will be interrupted");
    		communicator.interrupt();
    	}
    	try { communicator.join(); } catch (InterruptedException e) { e.printStackTrace(); }
	}
}
