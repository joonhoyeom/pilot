package messageResponder;

import message.FileUpMessageBody;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class FileUpResponder extends MessageResponder {
	@Override
	public Object respond(Object messageBody) {
		FileUpMessageBody fileUpMessageBody = new FileUpMessageBody("", null);
		fileUpMessageBody = (FileUpMessageBody) fileUpMessageBody.deserialize((byte[])messageBody);

		String fileName = fileUpMessageBody.getFilePath();
		fileName = fileName.substring(fileName.lastIndexOf("\\")).replace(".", "_uploaded.").trim();

		Path path = Paths.get("."+fileName);
		try {
			OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
			if(fileUpMessageBody.getData() != null)
				os.write(fileUpMessageBody.getData());
			else
				os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
}
