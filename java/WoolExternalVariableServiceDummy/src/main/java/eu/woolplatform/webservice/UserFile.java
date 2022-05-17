package eu.woolplatform.webservice;

import eu.woolplatform.utils.AppComponents;
import eu.woolplatform.utils.exception.ParseException;
import eu.woolplatform.utils.xml.AbstractSimpleSAXHandler;
import eu.woolplatform.utils.xml.SimpleSAXParser;
import org.slf4j.Logger;
import org.xml.sax.Attributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserFile {

	public static UserCredentials findUser(String username) {
		List<UserCredentials> users;
		try {
			users = read();
		} catch (ParseException | IOException ex) {
			throw new RuntimeException("Failed to read users.xml: " +
					ex.getMessage(), ex);
		}
		String lower = username.toLowerCase();
		for (UserCredentials user : users) {
			if (user.getUsername().toLowerCase().equals(lower))
				return user;
		}
		return null;
	}

	private static List<UserCredentials> read() throws ParseException,
			IOException {
		Configuration config = Configuration.getInstance();
		File dataDir = new File(config.get(Configuration.DATA_DIR));
		File usersFile = new File(dataDir, "users.xml");
		SimpleSAXParser<List<UserCredentials>> parser = new SimpleSAXParser<>(
				new XMLHandler());
		return parser.parse(usersFile);
	}

	private static class XMLHandler extends
			AbstractSimpleSAXHandler<List<UserCredentials>> {
		private List<UserCredentials> users = new ArrayList<>();

		@Override
		public void startElement(String name, Attributes atts,
				List<String> parents) throws ParseException {
			if (parents.size() == 0) {
				if (!name.equals("users")) {
					throw new ParseException(
							"Expected element \"users\", found: " + name);
				}
			} else if (parents.size() == 1) {
				if (!name.equals("user")) {
					throw new ParseException(
							"Expected element \"user\", found: " + name);
				}
				startUser(atts);
			}
		}

		private void startUser(Attributes atts) throws ParseException {
			String username = readAttribute(atts, "username").trim();
			if (username.length() == 0) {
				throw new ParseException(
						"Empty value in attribute \"username\"");
			}
			String password = readAttribute(atts, "password");
			if (password.length() == 0) {
				throw new ParseException(
						"Empty value in attribute \"password\"");
			}
			String role;
			try {
				role = readAttribute(atts, "role");

				if (!(role.equalsIgnoreCase(UserCredentials.USER_ROLE_USER) ||
						role.equalsIgnoreCase(UserCredentials.USER_ROLE_ADMIN))) {
					throw new ParseException(
							"Invalid specification for \"role\": " + role);
				}
			} catch (ParseException pe) {
				role = UserCredentials.USER_ROLE_USER;
				Logger logger = AppComponents.getLogger(UserFile.class.getSimpleName());
				logger.warn("Warning while reading users.xml file: User role not defined for user '"
						+username+"', assuming role '"+role+"'.");
			}
			users.add(new UserCredentials(username, password, role));
		}

		@Override
		public void endElement(String name, List<String> parents)
				throws ParseException {
		}

		@Override
		public void characters(String ch, List<String> parents)
				throws ParseException {
		}

		@Override
		public List<UserCredentials> getObject() {
			return users;
		}
	}
}
