package hermes.store;

import javax.jms.Destination;

public class MessageStoreFolder implements Destination{
	private String name ;
	public MessageStoreFolder(String name) {
		this.name = name ;
	}
	public String getName() {
		return name ;
	}
}
