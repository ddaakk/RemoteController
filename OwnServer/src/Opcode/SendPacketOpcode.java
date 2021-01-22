package Opcode;

public enum SendPacketOpcode {
	MOUSE_POS(0x01);
	
	private final int value;

	private SendPacketOpcode(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
}
