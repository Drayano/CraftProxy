package com.raphfrk.craftproxy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class Packet {
	
	HashMap<Byte,Class[]> packetTypes = new HashMap<Byte,Class[]>();
	
	Packet() {
		packetTypes.put((byte)0x00, new Class[] {});
		packetTypes.put((byte)0x01, new Class[] {Integer.class, String.class, String.class, Long.class, Byte.class});
		packetTypes.put((byte)0x02, new Class[] {String.class} );
		packetTypes.put((byte)0x03, new Class[] {String.class} );
		packetTypes.put((byte)0x04, new Class[] {Long.class} );
		packetTypes.put((byte)0x05, new Class[] {Integer.class, Short.class, Short.class} );
		packetTypes.put((byte)0x06, new Class[] {Integer.class, Integer.class, Integer.class});
		packetTypes.put((byte)0x07, new Class[] {Integer.class, Integer.class, Boolean.class});
		packetTypes.put((byte)0x08, new Class[] {Short.class });
		packetTypes.put((byte)0x09, new Class[] {});
		packetTypes.put((byte)0x0A, new Class[] {Boolean.class });
		packetTypes.put((byte)0x0B, new Class[] {Double.class, Double.class, Double.class, Double.class, Boolean.class});
		packetTypes.put((byte)0x0C, new Class[] {Float.class, Float.class, Boolean.class });
		packetTypes.put((byte)0x0D, new Class[] {Double.class, Double.class, Double.class, Double.class, Float.class, Float.class, Boolean.class});
		packetTypes.put((byte)0x0E, new Class[] {Byte.class, Integer.class, Byte.class, Integer.class, Byte.class});
		packetTypes.put((byte)0x0F, new Class[] {Integer.class, Byte.class, Integer.class, Byte.class, ItemElement.class});
		packetTypes.put((byte)0x10, new Class[] {Short.class});
		packetTypes.put((byte)0x12, new Class[] {Integer.class, Byte.class});
		packetTypes.put((byte)0x14, new Class[] {Integer.class, String.class, Integer.class, Integer.class, Integer.class, Byte.class, Byte.class, Short.class});
		packetTypes.put((byte)0x15, new Class[] {Integer.class, Short.class, Byte.class, Integer.class, Integer.class, Integer.class, Byte.class, Byte.class, Byte.class});
		packetTypes.put((byte)0x16, new Class[] {Integer.class, Integer.class});
		packetTypes.put((byte)0x17, new Class[] {Integer.class, Byte.class, Integer.class, Integer.class, Integer.class });
		packetTypes.put((byte)0x18, new Class[] {Integer.class, Byte.class, Integer.class, Integer.class, Integer.class, Byte.class, Byte.class });
		packetTypes.put((byte)0x1C, new Class[] {Integer.class, Short.class, Short.class, Short.class });
		packetTypes.put((byte)0x1D, new Class[] {Integer.class });
		packetTypes.put((byte)0x1E, new Class[] {Integer.class });
		packetTypes.put((byte)0x1F, new Class[] {Integer.class, Byte.class, Byte.class, Byte.class, });
		packetTypes.put((byte)0x20, new Class[] {Integer.class, Byte.class, Byte.class });
		packetTypes.put((byte)0x21, new Class[] {Integer.class, Byte.class, Byte.class, Byte.class, Byte.class, Byte.class });
		packetTypes.put((byte)0x22, new Class[] {Integer.class, Integer.class, Integer.class, Integer.class, Byte.class, Byte.class});
		packetTypes.put((byte)0x26, new Class[] {Integer.class, Byte.class});
		packetTypes.put((byte)0x27, new Class[] {Integer.class, Integer.class});
		packetTypes.put((byte)0x32, new Class[] {Integer.class, Integer.class, Boolean.class });
		packetTypes.put((byte)0x33, new Class[] {Integer.class, Short.class, Integer.class, Byte.class, Byte.class, Byte.class, IntSizedByteArray.class});
		packetTypes.put((byte)0x34, new Class[] {Integer.class, Integer.class, MultiBlockArray.class});
		packetTypes.put((byte)0x35, new Class[] {Integer.class, Byte.class, Integer.class, Byte.class, Byte.class });
		packetTypes.put((byte)0x3C, new Class[] {Double.class, Double.class, Float.class, IntSizedTripleByteArray.class});
		packetTypes.put((byte)0x64, new Class[] {Byte.class, Byte.class, String.class, Byte.class});
		packetTypes.put((byte)0x65, new Class[] {Byte.class});
		packetTypes.put((byte)0x66, new Class[] {Byte.class, Short.class, Byte.class, Short.class, ItemElement.class});
		packetTypes.put((byte)0x67, new Class[] {Byte.class, Short.class, ItemElement.class});
		packetTypes.put((byte)0x68, new Class[] {Byte.class, ItemArray.class});
		packetTypes.put((byte)0x69, new Class[] {Byte.class, Short.class, Short.class});
		packetTypes.put((byte)0x6A, new Class[] {Byte.class, Short.class, Boolean.class});
		packetTypes.put((byte)0x82, new Class[] {Integer.class, Short.class, Integer.class, String.class, String.class, String.class, String.class});
		packetTypes.put((byte)0xFF, new Class[] {String.class});
	}
	
	
	public byte packetId;
	
	public Class[] classes;
	public Object[] fields;
	
	boolean server;
	
	public boolean eof = false;
	public boolean valid = false;
	
	Packet( DataInputStream in, boolean server ) {
		this();
		
		this.server = server;
		
		try {
			packetId = in.readByte();
		} catch (EOFException e) {
			valid = false;
			eof = true;
			System.out.println( ((server)?("S->C"):"C->S") + " EOF reached");
			return;
		} catch (IOException e) {
			valid = false;
			eof = true;
			System.out.println( ((server)?("S->C"):"C->S") + " Unable to read packet ID");
			return;
		}
		
		classes = packetTypes.get(packetId);
		
		if( classes == null ) {
			System.out.println( ((server)?("S->C"):"C->S") + " Unknown packet Id: " + Integer.toHexString(packetId&0xFF) );
			valid = false;
			eof = false;
			return;
		} else {
			if( Verbose.isVerbose() ) {
				System.out.println( ((server)?("S->C"):"C->S") + " packet: " + Integer.toHexString(packetId&0xFF) );
			}
		}
		
		fields = new Object[classes.length];
		
		int pos = 0;
		
		for( Class current: classes ) {
			
			if( current.equals(String.class)) {
				fields[pos++] = Protocol.getString(in);
			} else if( current.equals(IntSizedByteArray.class)) {
				fields[pos++] = Protocol.getIntSizedByteArray(in);
			} else if( current.equals(IntSizedTripleByteArray.class)) {
				fields[pos++] = Protocol.getIntSizedTripleByteArray(in);
			} else if( current.equals(MultiBlockArray.class)) {
				fields[pos++] = Protocol.getMultiBlockArray(in);
			} else if( current.equals(ItemArray.class)) {
				fields[pos++] = Protocol.getItemArray(in);
			} else if( current.equals(ItemElement.class)) {
				fields[pos++] = Protocol.getItemElement(in);
			} else if( current.equals(Double.class)) {
				fields[pos++] = Protocol.getDouble(in);
			} else if( current.equals(Float.class)) {
				fields[pos++] = Protocol.getFloat(in);
			} else if( current.equals(Integer.class)) {
				fields[pos++] = Protocol.getInt(in);
			} else if( current.equals(Long.class)) {
				fields[pos++] = Protocol.getLong(in);
			} else if( current.equals(Short.class)) {
				fields[pos++] = Protocol.getShort(in);
			} else if( current.equals(Byte.class)) {
				fields[pos++] = Protocol.getByte(in);
			} else if( current.equals(Boolean.class)) {
				fields[pos++] = Protocol.getBoolean(in);
			} else {
				System.out.println( "Unable to handle field type: " + current.getName() );
				valid = false;
				eof = true;
				return;
			}

		}
		
		valid = true;
		eof = false;
		
	}
	
	boolean writeBytes(DataOutputStream out) {
		
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		
		try {
			out.writeByte(packetId);
			
			int pos = 0;
			for( Class current: classes ) {
				
				if( current.equals(String.class)) {
					bytes.addAll(Protocol.genString((String)fields[pos++]));
				} else if( current.equals(MultiBlockArray.class)) {
					bytes.addAll(Protocol.genMultiBlockArray((MultiBlockArray)fields[pos++]));
				} else if( current.equals(IntSizedByteArray.class)) {
					bytes.addAll(Protocol.genIntSizedByteArray((IntSizedByteArray)fields[pos++]));
				} else if( current.equals(IntSizedTripleByteArray.class)) {
					bytes.addAll(Protocol.genIntSizedTripleByteArray((IntSizedTripleByteArray)fields[pos++]));
				} else if( current.equals(ItemArray.class)) {
					bytes.addAll(Protocol.genItemArray((ItemArray)fields[pos++]));
				} else if( current.equals(ItemElement.class)) {
					bytes.addAll(Protocol.genItemElement((ItemElement)fields[pos++]));
				} else if( current.equals(Double.class)) {
					bytes.addAll(Protocol.genDouble((Double)fields[pos++]));
				} else if( current.equals(Float.class)) {
					bytes.addAll(Protocol.genFloat((Float)fields[pos++]));
				} else if( current.equals(Integer.class)) {
					bytes.addAll(Protocol.genInt((Integer)fields[pos++]));
				} else if( current.equals(Long.class)) {
					bytes.addAll(Protocol.genLong((Long)fields[pos++]));
				} else if( current.equals(Short.class)) {
					bytes.addAll(Protocol.genShort((Short)fields[pos++]));
				} else if( current.equals(Byte.class)) {
					bytes.addAll(Protocol.genByte((Byte)fields[pos++]));
				} else if( current.equals(Boolean.class)) {
					bytes.addAll(Protocol.genBoolean((Boolean)fields[pos++]));
				} else {
					System.out.println( "Unable to handle field type: " + current.getName() );
					return false;
				}

			}
			
			out.write(Protocol.tobytes(bytes));
			
			out.flush();
			
		} catch (IOException e) {
			System.out.println( "Unable to write bytes in writeBytes (Packet)");
			return false;
		}
		
		if( Verbose.isVerbose() ) {
			System.out.println( ((server)?("S->C"):"C->S") + " packet processed: " + Integer.toHexString(packetId&0xFF) );
		}

				
		return true;
		
		
		
	}
	
	boolean test() {
		
		ByteArrayOutputStream arrayOutStream = new ByteArrayOutputStream();

		DataOutputStream out = new DataOutputStream( arrayOutStream );
		
		writeBytes(out);
		
		try {
			out.flush();
		} catch (IOException e) {
			return false;
		}
		
		ByteArrayInputStream arrayInStream = new ByteArrayInputStream(arrayOutStream.toByteArray());

		DataInputStream in = new DataInputStream( arrayInStream );
		
		Packet newPacket = new Packet( in , server );
		
		boolean match = this.equals(newPacket);
		
		if( !match ) {
			System.out.println( "Error was on: " + ((server)?("S->C"):"C->S"));
		}
		
		return match;
		
		
	}
	
	

	@Override
	public boolean equals(Object obj) {

		if( obj == null ) {
			System.out.println( "Other packet is null");
			return false;
		} else if( !obj.getClass().equals(this.getClass())) {
			System.out.println( "Other packet is not actually a packet");
			return false;
		} else {
			
			Packet other = (Packet)obj;
			
			if( !other.valid ) {
				System.out.println( "Other packet not valid");
				return false;
			}
			
			if( this.packetId != other.packetId) {
				System.out.println( "Different Packet IDs");
				return false;
			}
			
			if( this.server != other.server ) {
				System.out.println( "Connection direction doesn't match");
				return false;
			}
			
			if( this.classes.length != other.classes.length ) {
				System.out.println( "Mismatch in number of classes");
				return false;
			}
			
			if( this.fields.length != other.fields.length ) {
				System.out.println( "Mismatch in number of fields");
				return false;
			}
			
			if( !Arrays.equals(this.classes, other.classes) ) {
				System.out.println( "Classes mismatch types of classes");
				return false;
			}
			
			
			if( !Arrays.equals(this.fields, other.fields) ) {
				System.out.println( "Mismatch in field array");
				
				System.out.println( "This packet:");
				for( Object current: this.fields ) {
					System.out.println(current);
				}
				
				System.out.println( "Other packet:");
				for( Object current: other.fields ) {
					System.out.println(current);
				}
				
				return false;
			}
			
			return true;
			
		}

	}
	
}