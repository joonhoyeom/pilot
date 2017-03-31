package messageResponder;

public class DirResResponder extends MessageResponder {
	@Override
	public Object respond(Object messageBody) {
		String dirEntry = new String((byte[])messageBody);
		System.out.println(dirEntry);
		return dirEntry;
	}
}
