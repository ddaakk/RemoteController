package Opcode;

public enum RecvPacketOpcode {
	
	CON(0x01),
	MOUSE_REQ(0x02),
	SERVER_EXIT(0x04),
	SHOW_MESSAGE(0x08),
	LEFT_CLICK(0x16),
	RIGHT_CLICK(0x32),
	SCROLL_UP(0x64),
	SCROLL_DOWN(0x128);
	
	private final int value;
	
	private RecvPacketOpcode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
